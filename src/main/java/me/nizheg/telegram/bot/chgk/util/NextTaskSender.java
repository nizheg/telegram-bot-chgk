package me.nizheg.telegram.bot.chgk.util;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

import me.nizheg.telegram.bot.api.model.InlineKeyboardMarkup;
import me.nizheg.telegram.bot.api.model.ParseMode;
import me.nizheg.telegram.bot.api.model.ReplyMarkup;
import me.nizheg.telegram.bot.api.service.TelegramApiClient;
import me.nizheg.telegram.bot.api.service.param.Message;
import me.nizheg.telegram.bot.chgk.config.AppConfig;
import me.nizheg.telegram.bot.chgk.domain.AutoChatGame;
import me.nizheg.telegram.bot.chgk.domain.ChatGame;
import me.nizheg.telegram.bot.chgk.domain.NextTaskOperation;
import me.nizheg.telegram.bot.chgk.domain.NextTaskResult;
import me.nizheg.telegram.bot.chgk.dto.Chat;
import me.nizheg.telegram.bot.chgk.dto.composite.StatEntry;
import me.nizheg.telegram.bot.chgk.dto.composite.Task;
import me.nizheg.telegram.bot.chgk.exception.GameException;
import me.nizheg.telegram.bot.chgk.exception.TournamentIsNotSelectedException;
import me.nizheg.telegram.util.Emoji;
import me.nizheg.telegram.util.TelegramApiUtil;

/**
 * @author Nikolay Zhegalin
 */
@Component
@Scope(AppConfig.SCOPE_THREAD)
public class NextTaskSender implements NextTaskOperation {

    private final TelegramApiClient telegramApiClient;
    private final TaskSender taskSender;
    private final AnswerSender answerSender;
    private final RatingHelper ratingHelper;
    private final TourList tourList;
    private final BotInfo botInfo;

    public NextTaskSender(
            TelegramApiClient asyncTelegramApiClient,
            TaskSender taskSender,
            AnswerSender answerSender,
            RatingHelper ratingHelper,
            TourList tourList,
            BotInfo botInfo) {
        this.telegramApiClient = asyncTelegramApiClient;
        this.taskSender = taskSender;
        this.answerSender = answerSender;
        this.ratingHelper = ratingHelper;
        this.tourList = tourList;
        this.botInfo = botInfo;
    }

    @Override
    public void sendNextTask(ChatGame chatGame) {
        Long chatId = chatGame.getChatId();
        try {
            NextTaskResult nextTaskResult = chatGame.nextTask();
            sendAnswerOfPreviousTask(chatGame.getChat(), nextTaskResult.getUnansweredTask());
            Task nextTask = nextTaskResult.getNextTask();
            if (nextTask == null) {
                String messageText;
                if (nextTaskResult.isTournament()) {
                    StringBuilder messageBuilder = new StringBuilder(Emoji.GLOWING_STAR + " <b>Турнир пройден.</b>");
                    List<StatEntry> tournamentStat = nextTaskResult.getTournamentStat();
                    if (tournamentStat != null && !tournamentStat.isEmpty()) {
                        messageBuilder.append(
                                "\n<i>Cчёт Знатоки против Бота: </i>" + createScoreMessage(tournamentStat));
                    }
                    messageText = messageBuilder.toString();
                } else {
                    messageText = "<i>Новых вопросов пока что больше нет!</i>";
                }
                Message message = new Message(messageText, chatId, ParseMode.HTML);
                message.setReplyMarkup(TelegramApiUtil.createInlineButtonMarkup("Выбрать категорию", "category"));
                telegramApiClient.sendMessage(message);
            } else {
                ReplyMarkup replyMarkup = null;
                if (!(chatGame instanceof AutoChatGame)) {
                    if (chatGame.getChat().isPrivate()) {
                        replyMarkup = TelegramApiUtil.createInlineButtonMarkup("Ответ", "answer " + nextTask.getId(),
                                "Дальше", "next");
                    } else {
                        replyMarkup = TelegramApiUtil.createInlineButtonMarkup("Подсказка", "hint " + nextTask.getId(),
                                "Дальше", "next");
                    }
                }
                StringBuilder messageBuilder = new StringBuilder();
                messageBuilder.append(Emoji.BLACK_QUESTION_MARK_ORNAMENT + "<b>Внимание, вопрос!</b>  ");
                if (chatGame instanceof AutoChatGame) {
                    messageBuilder.append(
                            Emoji.HOURGLASS_WITH_FLOWING_SAND + "<b>" + ((AutoChatGame) chatGame).getTimeout() / 60
                                    + "</b>");
                }
                messageBuilder.append("\n");
                taskSender.sendTaskText(messageBuilder, nextTask, chatId, replyMarkup);
            }

        } catch (TournamentIsNotSelectedException e) {
            Message message = tourList.getTournamentsListOfChat(chatId, 0);
            telegramApiClient.sendMessage(message);
        } catch (GameException e) {
            telegramApiClient.sendMessage(new Message("<i>" + e.getMessage() + "</i>", chatId, ParseMode.HTML));
        }
    }

    @Override
    public void sendAnswerOfPreviousTask(Chat chat, Task task) {
        if (task != null) {
            InlineKeyboardMarkup replyMarkup = new InlineKeyboardMarkup();
            replyMarkup.setInlineKeyboard(Collections.singletonList(ratingHelper.createRatingButtons(task.getId())));
            answerSender.sendAnswerOfTask(new StringBuilder("<b>Ответ к предыдущему вопросу:</b>\n"), task,
                    chat.getId(), replyMarkup);
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
        return usersScore + ":" + botScore;
    }
}
