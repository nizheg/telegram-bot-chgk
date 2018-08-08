package me.nizheg.telegram.bot.chgk.command;

import org.apache.commons.lang3.StringUtils;

import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import lombok.NonNull;
import me.nizheg.telegram.bot.api.model.InlineKeyboardButton;
import me.nizheg.telegram.bot.api.model.InlineKeyboardMarkup;
import me.nizheg.telegram.bot.api.model.ParseMode;
import me.nizheg.telegram.bot.api.model.User;
import me.nizheg.telegram.bot.api.service.TelegramApiClient;
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
import me.nizheg.telegram.bot.chgk.service.ChatService;
import me.nizheg.telegram.bot.chgk.service.TelegramUserService;
import me.nizheg.telegram.bot.chgk.util.BotInfo;
import me.nizheg.telegram.bot.chgk.util.DateUtils;
import me.nizheg.telegram.bot.chgk.util.RatingHelper;
import me.nizheg.telegram.bot.chgk.util.TaskSender;
import me.nizheg.telegram.bot.command.ChatCommand;
import me.nizheg.telegram.bot.command.CommandContext;
import me.nizheg.telegram.bot.command.CommandException;
import me.nizheg.telegram.bot.service.CommandsHolder;
import me.nizheg.telegram.util.Emoji;

/**
 * @author Nikolay Zhegalin
 */
public class DefaultCommand extends ChatCommand {

    private final ChatService chatService;
    private final ChatGameService chatGameService;
    private final TaskSender taskSender;
    private final TelegramUserService telegramUserService;
    private final RatingHelper ratingHelper;
    private final BotInfo botInfo;
    private final Clock clock;

    public DefaultCommand(
            @NonNull TelegramApiClient telegramApiClient,
            @NonNull ChatService chatService,
            @NonNull ChatGameService chatGameService,
            @NonNull TaskSender taskSender,
            @NonNull TelegramUserService telegramUserService,
            @NonNull RatingHelper ratingHelper,
            @NonNull BotInfo botInfo,
            @NonNull Clock clock) {
        super(telegramApiClient);
        this.chatService = chatService;
        this.chatGameService = chatGameService;
        this.taskSender = taskSender;
        this.telegramUserService = telegramUserService;
        this.ratingHelper = ratingHelper;
        this.botInfo = botInfo;
        this.clock = clock;
    }

    public DefaultCommand(
            @NonNull Supplier<TelegramApiClient> telegramApiClientSupplier,
            @NonNull ChatService chatService,
            @NonNull ChatGameService chatGameService,
            @NonNull TaskSender taskSender,
            @NonNull TelegramUserService telegramUserService,
            @NonNull RatingHelper ratingHelper,
            @NonNull BotInfo botInfo,
            @NonNull Clock clock) {
        super(telegramApiClientSupplier);
        this.chatService = chatService;
        this.chatGameService = chatGameService;
        this.taskSender = taskSender;
        this.telegramUserService = telegramUserService;
        this.ratingHelper = ratingHelper;
        this.botInfo = botInfo;
        this.clock = clock;
    }

    @Override
    public void execute(CommandContext ctx) throws CommandException {
        String text = ctx.getText();
        if (StringUtils.isBlank(text)) {
            return;
        }
        Long chatId = ctx.getChatId();
        boolean isChatActive = chatService.isChatActive(chatId);
        ChatGame chatGame = null;
        if (isChatActive) {
            chatGame = chatGameService.getGame(new Chat(ctx.getChat()));
        }
        if (chatGame == null) {
            return;
        }
        User user = ctx.getFrom();
        UserAnswerResult userAnswerResult = chatGame.userAnswer(new UserAnswer(text, user));
        Task currentTask = userAnswerResult.getCurrentTask();
        if (currentTask == null) {
            throw new NoTaskException();
        }

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

            OffsetDateTime usageTime = userAnswerResult.getUsageTime();
            resultBuilder.append("\n" + Emoji.HOURGLASS + " Время, потраченное на вопрос: ")
                    .append(printDiffTillNow(usageTime));
            InlineKeyboardMarkup replyMarkup = new InlineKeyboardMarkup();
            List<List<InlineKeyboardButton>> buttonGroup = new ArrayList<>();
            buttonGroup.add(ratingHelper.createRatingButtons(currentTask.getId()));
            InlineKeyboardButton nextButton = new InlineKeyboardButton();
            nextButton.setText("Дальше");
            nextButton.setCallbackData("next " + currentTask.getId());
            buttonGroup.add(Collections.singletonList(nextButton));
            replyMarkup.setInlineKeyboard(buttonGroup);
            taskSender.sendTaskComment(resultBuilder, currentTask, chatId, replyMarkup);
        } else {
            getTelegramApiClient().sendMessage(new Message(
                    "\"<b>" + TelegramHtmlUtil.escape(text) + "</b>\" - это неверный ответ.", chatId, ParseMode.HTML));
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

    private String printDiffTillNow(OffsetDateTime date) {
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
