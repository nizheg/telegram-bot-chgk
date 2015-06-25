package me.nizheg.chgk.command;

import java.util.Collections;
import java.util.List;

import me.nizheg.chgk.dto.TelegramUser;
import me.nizheg.chgk.dto.composite.StatEntry;
import me.nizheg.chgk.repository.param.StatSearchParams;
import me.nizheg.chgk.service.AnswerLogService;
import me.nizheg.chgk.util.BotInfo;
import me.nizheg.telegram.bot.service.command.ChatCommand;
import me.nizheg.telegram.bot.service.command.CommandContext;
import me.nizheg.telegram.bot.service.command.CommandException;
import me.nizheg.telegram.model.InlineKeyboardButton;
import me.nizheg.telegram.model.InlineKeyboardMarkup;
import me.nizheg.telegram.model.ParseMode;
import me.nizheg.telegram.service.TelegramApiClient;
import me.nizheg.telegram.service.TelegramApiException;
import me.nizheg.telegram.service.param.ChatId;
import me.nizheg.telegram.service.param.EditedMessage;
import me.nizheg.telegram.service.param.Message;
import me.nizheg.telegram.util.Emoji;
import me.nizheg.telegram.util.TelegramHtmlUtil;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

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
            telegramApiClient.sendMessage(new Message("<i>В этом чате ещё никто ничего не отгадал.</i>", ctx.getChatId(), ParseMode.HTML));
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
                    telegramApiClient.editMessageText(editedMessage);
                } catch (TelegramApiException ex) {
                    logger.error("Unable to handle callback " + ctx.getCallbackQueryId(), ex);
                }
            } else {
                Message message = new Message(createTop10Message(statForChat), ctx.getChatId(), ParseMode.HTML, true);
                message.setDisableNotification(true);
                message.setReplyMarkup(markup);
                telegramApiClient.sendMessage(message);
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
                telegramApiClient.editMessageText(editedMessage);
            } catch (TelegramApiException ex) {
                logger.error("Unable to handle callback " + ctx.getCallbackQueryId(), ex);
            }
        } else {
            Message message = new Message(createScoreMessage(statForChat), ctx.getChatId(), ParseMode.HTML, true);
            message.setDisableNotification(true);
            message.setReplyMarkup(markup);
            telegramApiClient.sendMessage(message);
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
