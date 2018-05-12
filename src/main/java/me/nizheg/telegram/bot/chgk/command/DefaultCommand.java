package me.nizheg.telegram.bot.chgk.command;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import me.nizheg.telegram.bot.api.model.InlineKeyboardButton;
import me.nizheg.telegram.bot.api.model.InlineKeyboardMarkup;
import me.nizheg.telegram.bot.api.model.ParseMode;
import me.nizheg.telegram.bot.api.model.User;
import me.nizheg.telegram.bot.api.service.TelegramApiClient;
import me.nizheg.telegram.bot.api.util.TelegramHtmlUtil;
import me.nizheg.telegram.bot.chgk.domain.AutoChatGame;
import me.nizheg.telegram.bot.chgk.domain.ChatGame;
import me.nizheg.telegram.bot.chgk.domain.UserAnswer;
import me.nizheg.telegram.bot.chgk.domain.UserAnswerResult;
import me.nizheg.telegram.bot.chgk.dto.Chat;
import me.nizheg.telegram.bot.chgk.dto.TelegramUser;
import me.nizheg.telegram.bot.chgk.dto.composite.Task;
import me.nizheg.telegram.bot.chgk.exception.NoTaskException;
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

 *
 * @author Nikolay Zhegalin
 */
public class DefaultCommand extends ChatCommand {

    private final ChatService chatService;
    private final TaskSender taskSender;
    private final TelegramUserService telegramUserService;
    private final RatingHelper ratingHelper;
    private final BotInfo botInfo;

    public DefaultCommand(
            TelegramApiClient telegramApiClient,
            ChatService chatService,
            TaskSender taskSender,
            TelegramUserService telegramUserService,
            RatingHelper ratingHelper,
            BotInfo botInfo) {
        super(telegramApiClient);
        this.chatService = chatService;
        this.taskSender = taskSender;
        this.telegramUserService = telegramUserService;
        this.ratingHelper = ratingHelper;
        this.botInfo = botInfo;
    }

    public DefaultCommand(
            Supplier<TelegramApiClient> telegramApiClientSupplier,
            ChatService chatService,
            TaskSender taskSender,
            TelegramUserService telegramUserService,
            RatingHelper ratingHelper,
            BotInfo botInfo) {
        super(telegramApiClientSupplier);
        this.chatService = chatService;
        this.taskSender = taskSender;
        this.telegramUserService = telegramUserService;
        this.ratingHelper = ratingHelper;
        this.botInfo = botInfo;
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
            chatGame = chatService.getGame(new Chat(ctx.getChat()));
        }
        if (chatGame == null) {
            return;
        }
        User user = ctx.getFrom();
        UserAnswerResult userAnswerResult = chatGame.userAnswer(new UserAnswer(text, user));
        Task currentTask = userAnswerResult.getCurrentTask();
        if (currentTask == null) {
            throw new NoTaskException(ctx.getChatId());
        }

        if (userAnswerResult.isCorrect()) {
            StringBuilder resultBuilder = new StringBuilder();
            long firstAnsweredUserId = userAnswerResult.getFirstAnsweredUser();
            String exactAnswer = userAnswerResult.getExactAnswer();

            if (firstAnsweredUserId == user.getId()) {
                resultBuilder.append(createCompliment(user));
                if (exactAnswer != null) {
                    resultBuilder.append("\"<i>" + TelegramHtmlUtil.escape(text)
                            + "</i>\" не совсем точный ответ, но я его засчитываю.\nПравильный ответ: <b>"
                            + exactAnswer + "</b>");
                } else {
                    resultBuilder.append(
                            "\"<b>" + TelegramHtmlUtil.escape(text) + "</b>\" - это абсолютно верный ответ.");
                }
            } else if (firstAnsweredUserId == botInfo.getBotUser().getId()) {
                if (exactAnswer != null) {
                    resultBuilder.append("\"<i>" + TelegramHtmlUtil.escape(text)
                            + "</i>\" не совсем точный ответ, и я уже сообщал правильный: <b>"
                            + exactAnswer + "</b>");
                } else {
                    resultBuilder.append("\"<b>" + TelegramHtmlUtil.escape(text)
                            + "</b>\" - это абсолютно верный ответ, но я уже сообщал вам его.");
                }
            } else {
                TelegramUser firstAnsweredUser = telegramUserService.getTelegramUser(firstAnsweredUserId);
                if (exactAnswer != null) {
                    resultBuilder.append(
                            "\"<i>" + TelegramHtmlUtil.escape(text) + "</i>\" не совсем точный ответ, и <b>"
                                    + TelegramHtmlUtil.escape(firstAnsweredUser.getFirstname())
                                    + "</b> уже ответил(а) на этот вопрос ранее.\nПравильный ответ: <b>"
                                    + exactAnswer + "</b>");
                } else {
                    resultBuilder.append(
                            "\"<b>" + TelegramHtmlUtil.escape(text) + "</b>\" - это абсолютно верный ответ, но <b>"
                                    + TelegramHtmlUtil.escape(firstAnsweredUser.getFirstname())
                                    + "</b> был(а) быстрее.");
                }
            }

            Date usageTime = userAnswerResult.getUsageTime();
            resultBuilder.append(
                    "\n" + Emoji.HOURGLASS + " Время, потраченное на вопрос: " + printDiffTillNow(usageTime));
            InlineKeyboardMarkup replyMarkup = new InlineKeyboardMarkup();
            List<List<InlineKeyboardButton>> buttonGroup = new ArrayList<>();
            buttonGroup.add(ratingHelper.createRatingButtons(currentTask.getId()));
            if (!(chatGame instanceof AutoChatGame)) {
                InlineKeyboardButton nextButton = new InlineKeyboardButton();
                nextButton.setText("Дальше");
                nextButton.setCallbackData("next");
                buttonGroup.add(Collections.singletonList(nextButton));
            }
            replyMarkup.setInlineKeyboard(buttonGroup);
            taskSender.sendTaskComment(resultBuilder, currentTask, chatId, replyMarkup);
        } else {
            getTelegramApiClient().sendMessage(new me.nizheg.telegram.bot.api.service.param.Message(
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

    private String printDiffTillNow(Date date) {
        if (date == null) {
            return "0";
        }
        Map<TimeUnit, Long> diffMap = DateUtils.computeDiffTillNow(date);
        StringBuilder resultBuilder = new StringBuilder();
        for (Map.Entry<TimeUnit, Long> timeUnitEntry : diffMap.entrySet()) {
            if (timeUnitEntry.getValue() > 0) {
                resultBuilder.append(" " + timeUnitEntry.getValue() + " " + timeUnitToString(timeUnitEntry.getKey()));
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
