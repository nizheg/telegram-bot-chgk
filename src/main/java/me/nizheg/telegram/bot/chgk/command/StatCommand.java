package me.nizheg.telegram.bot.chgk.command;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;

import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

import javax.annotation.Nonnull;

import me.nizheg.telegram.bot.api.model.InlineKeyboardButton;
import me.nizheg.telegram.bot.api.model.InlineKeyboardMarkup;
import me.nizheg.telegram.bot.api.model.ParseMode;
import me.nizheg.telegram.bot.api.service.TelegramApiClient;
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
import me.nizheg.telegram.util.Emoji;

/**
 * @author Nikolay Zhegalin
 */
public class StatCommand extends ChatCommand {

    private final AnswerLogService answerLogService;
    private final BotInfo botInfo;

    public StatCommand(
            @Nonnull TelegramApiClient telegramApiClient,
            @Nonnull AnswerLogService answerLogService,
            @Nonnull BotInfo botInfo) {
        super(telegramApiClient);
        Validate.notNull(answerLogService, "answerLogService should be defined");
        Validate.notNull(botInfo, "botInfo should be defined");
        this.answerLogService = answerLogService;
        this.botInfo = botInfo;
    }

    public StatCommand(
            @Nonnull Supplier<TelegramApiClient> telegramApiClientSupplier,
            @Nonnull AnswerLogService answerLogService,
            @Nonnull BotInfo botInfo) {
        super(telegramApiClientSupplier);
        Validate.notNull(answerLogService, "answerLogService should be defined");
        Validate.notNull(botInfo, "botInfo should be defined");
        this.answerLogService = answerLogService;
        this.botInfo = botInfo;
    }

    @Override
    public void execute(CommandContext ctx) {
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
            getTelegramApiClient().sendMessage(
                    new Message("<i>В этом чате ещё никто ничего не отгадал.</i>", ctx.getChatId(), ParseMode.HTML));
        } else {
            InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
            InlineKeyboardButton scoreButton = new InlineKeyboardButton();
            scoreButton.setCallbackData("stat " + Mode.SCORE.name());
            scoreButton.setText("Cчёт Знатоки против Бота");
            markup.setInlineKeyboard(Collections.singletonList((Collections.singletonList(scoreButton))));
            if (ctx.isCallbackQuery() && ctx.getReplyToBotMessage() != null) {
                EditedMessage editedMessage = new EditedMessage(new ChatId(ctx.getChatId()),
                        ctx.getReplyToBotMessage().getMessageId(),
                        createTop10Message(statForChat));
                editedMessage.setDisableWebPagePreview(true);
                editedMessage.setParseMode(ParseMode.HTML);
                editedMessage.setReplyMarkup(markup);
                getTelegramApiClient().editMessageText(editedMessage);
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
            resultBuilder.append("\n")
                    .append(i)
                    .append(". ")
                    .append(createUserName(statEntry.getTelegramUser()))
                    .append("\t")
                    .append(statEntry.getCount());
            i++;
        }
        resultBuilder.append("\n");
        return resultBuilder.toString();
    }

    private void createScoreMessage(CommandContext ctx) {
        List<StatEntry> statForChat = answerLogService.getStatForChatUser(ctx.getChatId(), botInfo.getBotUser().getId(),
                "users");
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        InlineKeyboardButton testButton = new InlineKeyboardButton();
        testButton.setCallbackData("stat " + Mode.TOP10.name());
        testButton.setText("Топ-10 чата");
        markup.setInlineKeyboard(Collections.singletonList(Collections.singletonList(testButton)));
        if (ctx.isCallbackQuery() && ctx.getReplyToBotMessage() != null) {
            EditedMessage editedMessage = new EditedMessage(new ChatId(ctx.getChatId()),
                    ctx.getReplyToBotMessage().getMessageId(),
                    createScoreMessage(statForChat) );
            editedMessage.setDisableWebPagePreview(true);
            editedMessage.setParseMode(ParseMode.HTML);
            editedMessage.setReplyMarkup(markup);
            getTelegramApiClient().editMessageText(editedMessage);
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
                + (StringUtils.isBlank(telegramUser.getLastname()) ? "" :
                " " + TelegramHtmlUtil.escape(telegramUser.getLastname()))
                + (StringUtils.isBlank(telegramUser.getUsername()) ? "</b>" :
                "</b> @" + TelegramHtmlUtil.escape(telegramUser.getUsername()));
    }

    @Override
    public String getCommandName() {
        return "stat";
    }

    @Override
    public String getDescription() {
        return "/stat - статистика чата";
    }

    private enum Mode {
        TOP10, SCORE
    }
}
