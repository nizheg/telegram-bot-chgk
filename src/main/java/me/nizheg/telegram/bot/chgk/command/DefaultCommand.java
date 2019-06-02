package me.nizheg.telegram.bot.chgk.command;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import javax.annotation.Nullable;

import lombok.NonNull;
import me.nizheg.telegram.bot.api.model.InlineKeyboardButton;
import me.nizheg.telegram.bot.api.model.InlineKeyboardMarkup;
import me.nizheg.telegram.bot.api.model.ParseMode;
import me.nizheg.telegram.bot.api.model.User;
import me.nizheg.telegram.bot.api.service.TelegramApiClient;
import me.nizheg.telegram.bot.api.service.param.ChatId;
import me.nizheg.telegram.bot.api.service.param.Message;
import me.nizheg.telegram.bot.api.util.TelegramHtmlUtil;
import me.nizheg.telegram.bot.chgk.command.exception.NoTaskException;
import me.nizheg.telegram.bot.chgk.domain.ChatGame;
import me.nizheg.telegram.bot.chgk.domain.UserAnswer;
import me.nizheg.telegram.bot.chgk.domain.UserAnswerResult;
import me.nizheg.telegram.bot.chgk.dto.Chat;
import me.nizheg.telegram.bot.chgk.dto.TelegramUser;
import me.nizheg.telegram.bot.chgk.dto.composite.Task;
import me.nizheg.telegram.bot.chgk.service.ChatGameService;
import me.nizheg.telegram.bot.chgk.service.TelegramUserService;
import me.nizheg.telegram.bot.chgk.util.DateUtils;
import me.nizheg.telegram.bot.chgk.util.RatingHelper;
import me.nizheg.telegram.bot.chgk.util.TaskSender;
import me.nizheg.telegram.bot.command.ChatCommand;
import me.nizheg.telegram.bot.command.CommandContext;
import me.nizheg.telegram.bot.command.CommandException;
import me.nizheg.telegram.bot.service.CommandsHolder;
import me.nizheg.telegram.bot.starter.service.preconditions.MessageWithText;
import me.nizheg.telegram.bot.starter.util.BotInfo;
import me.nizheg.telegram.util.Emoji;

import static java.util.Collections.singletonList;

/**
 * @author Nikolay Zhegalin
 */
@UserInChannel
@MessageWithText
@ChatActive(notifyUser = false)
@Component
public class DefaultCommand extends ChatCommand {

    private final ChatGameService chatGameService;
    private final TaskSender taskSender;
    private final TelegramUserService telegramUserService;
    private final RatingHelper ratingHelper;
    private final BotInfo botInfo;
    private final Clock clock;

    public DefaultCommand(
            @NonNull Supplier<TelegramApiClient> telegramApiClientSupplier,
            @NonNull ChatGameService chatGameService,
            @NonNull TaskSender taskSender,
            @NonNull TelegramUserService telegramUserService,
            @NonNull RatingHelper ratingHelper,
            @NonNull BotInfo botInfo,
            @NonNull Clock clock) {
        super(telegramApiClientSupplier);
        this.chatGameService = chatGameService;
        this.taskSender = taskSender;
        this.telegramUserService = telegramUserService;
        this.ratingHelper = ratingHelper;
        this.botInfo = botInfo;
        this.clock = clock;
    }

    @Override
    public int getPriority() {
        return 140;
    }

    @Override
    public void execute(CommandContext ctx) throws CommandException {
        String text = ctx.getText();
        Long chatId = ctx.getChatId();
        ChatGame chatGame = chatGameService.getGame(new Chat(ctx.getChat()));
        User user = ctx.getFrom();
        UserAnswerResult userAnswerResult = chatGame.userAnswer(new UserAnswer(text, user));
        Task currentTask = userAnswerResult.getCurrentTask().orElseThrow(NoTaskException::new);

        if (userAnswerResult.isCorrect()) {
            StringBuilder resultBuilder = new StringBuilder();
            long firstAnsweredUserId = userAnswerResult.getFirstAnsweredUser().orElse(user.getId());
            Optional<String> exactAnswerOptional = userAnswerResult.getExactAnswer();

            if (firstAnsweredUserId == user.getId()) {
                resultBuilder.append(createCompliment(user));
                if (exactAnswerOptional.isPresent()) {
                    resultBuilder.append("\"<i>")
                            .append(TelegramHtmlUtil.escape(text))
                            .append("</i>\" не совсем точный ответ, но я его засчитываю.\nПравильный ответ: <b>")
                            .append(exactAnswerOptional.get())
                            .append("</b>");
                } else {
                    resultBuilder.append("\"<b>")
                            .append(TelegramHtmlUtil.escape(text))
                            .append("</b>\" - это абсолютно верный ответ.");
                }
            } else if (firstAnsweredUserId == botInfo.getBotUser().getId()) {
                if (exactAnswerOptional.isPresent()) {
                    resultBuilder.append("\"<i>")
                            .append(TelegramHtmlUtil.escape(text))
                            .append("</i>\" не совсем точный ответ, и я уже сообщал правильный: <b>")
                            .append(exactAnswerOptional.get())
                            .append("</b>");
                } else {
                    resultBuilder.append("\"<b>")
                            .append(TelegramHtmlUtil.escape(text))
                            .append("</b>\" - это абсолютно верный ответ, но я уже сообщал вам его.");
                }
            } else {
                TelegramUser firstAnsweredUser = telegramUserService.getTelegramUser(firstAnsweredUserId);
                if (exactAnswerOptional.isPresent()) {
                    resultBuilder.append("\"<i>")
                            .append(TelegramHtmlUtil.escape(text))
                            .append("</i>\" не совсем точный ответ, и <b>")
                            .append(TelegramHtmlUtil.escape(firstAnsweredUser.getFirstname()))
                            .append("</b> уже ответил(а) на этот вопрос ранее.\nПравильный ответ: <b>")
                            .append(exactAnswerOptional.get())
                            .append("</b>");
                } else {
                    resultBuilder.append("\"<b>")
                            .append(TelegramHtmlUtil.escape(text))
                            .append("</b>\" - это абсолютно верный ответ, но <b>")
                            .append(TelegramHtmlUtil.escape(firstAnsweredUser.getFirstname()))
                            .append("</b> был(а) быстрее.");
                }
            }

            OffsetDateTime usageTime = userAnswerResult.getUsageTime().orElse(null);
            resultBuilder.append("\n" + Emoji.HOURGLASS + " Время, потраченное на вопрос: ")
                    .append(printDiffTillNow(usageTime));
            InlineKeyboardMarkup replyMarkup = InlineKeyboardMarkup.column(
                    ratingHelper.createRatingButtons(currentTask.getId()),
                    singletonList(InlineKeyboardButton.callbackDataButton("Дальше", "next " + currentTask.getId()))
            );
            taskSender.sendTaskComment(resultBuilder, currentTask, chatId, replyMarkup,
                    (errorResponse, httpStatus) -> {
                    });
        } else {
            getTelegramApiClient().sendMessage(
                    Message.safeMessageBuilder()
                            .text("\"<b>" + TelegramHtmlUtil.escape(text) + "</b>\" - это неверный ответ.")
                            .chatId(new ChatId(chatId))
                            .parseMode(ParseMode.HTML)
                            .build());
        }

    }

    private String createCompliment(User user) {
        String compliment = "";
        if (user != null && StringUtils.isNotBlank(user.getFirstName())) {
            compliment =
                    Emoji.GLOWING_STAR + " Молодец, <b>" + TelegramHtmlUtil.escape(user.getFirstName()) + "</b>!\n";
        }
        return compliment;
    }

    private String printDiffTillNow(@Nullable OffsetDateTime date) {
        if (date == null) {
            return "0";
        }
        Map<TimeUnit, Long> diffMap = DateUtils.computeDiff(date, OffsetDateTime.now(clock));
        StringBuilder resultBuilder = new StringBuilder();
        for (Map.Entry<TimeUnit, Long> timeUnitEntry : diffMap.entrySet()) {
            if (timeUnitEntry.getValue() > 0) {
                resultBuilder.append(" ")
                        .append(timeUnitEntry.getValue())
                        .append(" ")
                        .append(timeUnitToString(timeUnitEntry.getKey()));
            }
        }
        return resultBuilder.toString();
    }

    private String timeUnitToString(TimeUnit timeUnit) {
        switch (timeUnit) {
            case DAYS:
                return "дн.";
            case HOURS:
                return "ч.";
            case MINUTES:
                return "мин.";
            case SECONDS:
                return "с.";
            case MILLISECONDS:
                return "мс.";
            default:
                return "";
        }
    }

    @Override
    public String getCommandName() {
        return CommandsHolder.COMMAND_NAME_DEFAULT_COMMAND;
    }

    @Override
    public String getDescription() {
        return "В чатах отвечать на вопрос можно либо начиная ответ с /, либо reply на вопрос";
    }
}
