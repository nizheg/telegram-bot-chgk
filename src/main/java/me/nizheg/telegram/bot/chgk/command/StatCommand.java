package me.nizheg.telegram.bot.chgk.command;

import me.nizheg.telegram.bot.api.model.InlineKeyboardButton;
import me.nizheg.telegram.bot.api.model.InlineKeyboardMarkup;
import me.nizheg.telegram.bot.api.model.ParseMode;
import me.nizheg.telegram.bot.api.service.TelegramApiClient;
import me.nizheg.telegram.bot.api.service.TelegramApiException;
import me.nizheg.telegram.bot.api.service.param.ChatId;
import me.nizheg.telegram.bot.api.service.param.EditedMessage;
import me.nizheg.telegram.bot.api.service.param.Message;
import me.nizheg.telegram.bot.api.util.TelegramHtmlUtil;
import me.nizheg.telegram.bot.chgk.dto.TelegramUser;
import me.nizheg.telegram.bot.chgk.dto.composite.StatEntry;
import me.nizheg.telegram.bot.chgk.repository.param.StatSearchParams;
import me.nizheg.telegram.bot.chgk.service.AnswerLogService;
import me.nizheg.telegram.bot.chgk.util.BotInfo;
import me.nizheg.telegram.bot.command.ChatCommand;
import me.nizheg.telegram.bot.command.CommandContext;
import me.nizheg.telegram.bot.command.CommandException;
import me.nizheg.telegram.util.Emoji;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collections;
import java.util.List;

/**
 * //todo add comments
 *
 * @author Nikolay Zhegalin
 */
public class StatCommand extends ChatCommand {

    @Autowired
    private AnswerLogService answerLogService;
    @Autowired
    private BotInfo botInfo;

    public StatCommand(TelegramApiClient telegramApiClient) {
        super(telegramApiClient);
    }

    @Override
    public void execute(CommandContext ctx) throws CommandException {
        Mode mode;
        try {
            mode = Mode.valueOf(ctx.getText());
        } catch (IllegalArgumentException ex) {
            mode = Mode.TOP10;
        }

        switch (mode) {
            case TOP10:
                createTop10Message(ctx);
                break;
            case SCORE:
                createScoreMessage(ctx);
                break;
        }

    }

    private void createTop10Message(CommandContext ctx) {
        StatSearchParams params = new StatSearchParams();
        params.setExcludeUserIds(Collections.singletonList(botInfo.getBotUser().getId()));
        params.setLimit(10);
        List<StatEntry> statForChat = answerLogService.getStatForChat(ctx.getChatId(), params);
        if (statForChat.isEmpty()) {
            getTelegramApiClient().sendMessage(new Message("<i>В этом чате ещё никто ничего не отгадал.</i>", ctx.getChatId(), ParseMode.HTML));
        } else {
            InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
            InlineKeyboardButton scoreButton = new InlineKeyboardButton();
            scoreButton.setCallbackData("stat " + Mode.SCORE.name());
            scoreButton.setText("Cчёт Знатоки против Бота");
            markup.setInlineKeyboard(Collections.singletonList((Collections.singletonList(scoreButton))));
            if (ctx.getCallbackQueryId() != null && ctx.getReplyToBotMessage() != null) {
                EditedMessage editedMessage = new EditedMessage();
                editedMessage.setChatId(new ChatId(ctx.getChatId()));
                editedMessage.setText(createTop10Message(statForChat));
                editedMessage.setDisableWebPagePreview(true);
                editedMessage.setMessageId(ctx.getReplyToBotMessage().getMessageId());
                editedMessage.setParseMode(ParseMode.HTML);
                editedMessage.setReplyMarkup(markup);
                try {
                    getTelegramApiClient().editMessageText(editedMessage);
                } catch (TelegramApiException ex) {
                    logger.error("Unable to handle callback " + ctx.getCallbackQueryId(), ex);
                }
            } else {
                Message message = new Message(createTop10Message(statForChat), ctx.getChatId(), ParseMode.HTML, true);
                message.setDisableNotification(true);
                message.setReplyMarkup(markup);
                getTelegramApiClient().sendMessage(message);
            }
        }

    }

    private String createTop10Message(List<StatEntry> statForChat) {
        StringBuilder resultBuilder = new StringBuilder(Emoji.GLOWING_STAR + " <b>Топ-10 знатоков чата</b>");
        int i = 1;
        for (StatEntry statEntry : statForChat) {
            resultBuilder.append("\n").append(i).append(". ").append(createUserName(statEntry.getTelegramUser())).append("\t").append(statEntry.getCount());
            i++;
        }
        resultBuilder.append("\n");
        return resultBuilder.toString();
    }

    private void createScoreMessage(CommandContext ctx) {
        List<StatEntry> statForChat = answerLogService.getStatForChatUser(ctx.getChatId(), botInfo.getBotUser().getId(), "users");
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        InlineKeyboardButton testButton = new InlineKeyboardButton();
        testButton.setCallbackData("stat " + Mode.TOP10.name());
        testButton.setText("Топ-10 чата");
        markup.setInlineKeyboard(Collections.singletonList(Collections.singletonList(testButton)));
        if (ctx.getCallbackQueryId() != null && ctx.getReplyToBotMessage() != null) {
            EditedMessage editedMessage = new EditedMessage();
            editedMessage.setChatId(new ChatId(ctx.getChatId()));
            editedMessage.setText(createScoreMessage(statForChat));
            editedMessage.setDisableWebPagePreview(true);
            editedMessage.setMessageId(ctx.getReplyToBotMessage().getMessageId());
            editedMessage.setParseMode(ParseMode.HTML);
            editedMessage.setReplyMarkup(markup);
            try {
                getTelegramApiClient().editMessageText(editedMessage);
            } catch (TelegramApiException ex) {
                logger.error("Unable to handle callback " + ctx.getCallbackQueryId(), ex);
            }
        } else {
            Message message = new Message(createScoreMessage(statForChat), ctx.getChatId(), ParseMode.HTML, true);
            message.setDisableNotification(true);
            message.setReplyMarkup(markup);
            getTelegramApiClient().sendMessage(message);
        }

    }

    private String createScoreMessage(List<StatEntry> statForChat) {
        StatEntry usersStat = null;
        StatEntry botStat = null;
        for (StatEntry statEntry : statForChat) {
            if (statEntry.getTelegramUser().getUsername().equals(botInfo.getBotUser().getUsername())) {
                botStat = statEntry;
            } else {
                usersStat = statEntry;
            }
        }
        long usersScore = usersStat == null ? 0 : usersStat.getCount();
        long botScore = botStat == null ? 0 : botStat.getCount();
        return Emoji.GLOWING_STAR + " <b>Cчёт Знатоки против Бота</b>\n" + usersScore + ":" + botScore;
    }

    private String createUserName(TelegramUser telegramUser) {
        return "<b>" + TelegramHtmlUtil.escape(telegramUser.getFirstname())
                + (StringUtils.isBlank(telegramUser.getLastname()) ? "" : " " + TelegramHtmlUtil.escape(telegramUser.getLastname()))
                + (StringUtils.isBlank(telegramUser.getUsername()) ? "</b>" : "</b> @" + TelegramHtmlUtil.escape(telegramUser.getUsername()));
    }

    @Override
    public String getCommandName() {
        return "stat";
    }

    @Override
    public String getDescription() {
        return "/stat - статистика чата";
    }

    private static enum Mode {
        TOP10, SCORE
    }
}
