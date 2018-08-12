package me.nizheg.telegram.bot.chgk.util;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import me.nizheg.telegram.bot.api.model.InlineKeyboardMarkup;
import me.nizheg.telegram.bot.api.model.ParseMode;
import me.nizheg.telegram.bot.api.model.ReplyMarkup;
import me.nizheg.telegram.bot.api.service.TelegramApiClient;
import me.nizheg.telegram.bot.api.service.param.Message;
import me.nizheg.telegram.bot.chgk.domain.AutoChatGame;
import me.nizheg.telegram.bot.chgk.domain.ChatGame;
import me.nizheg.telegram.bot.chgk.domain.NextTaskResult;
import me.nizheg.telegram.bot.chgk.dto.Chat;
import me.nizheg.telegram.bot.chgk.dto.composite.StatEntry;
import me.nizheg.telegram.bot.chgk.dto.composite.Task;
import me.nizheg.telegram.bot.chgk.exception.CurrentTaskIsOtherException;
import me.nizheg.telegram.bot.chgk.exception.GameException;
import me.nizheg.telegram.bot.chgk.exception.TournamentIsNotSelectedException;
import me.nizheg.telegram.util.Emoji;
import me.nizheg.telegram.util.TelegramApiUtil;

/**
 * @author Nikolay Zhegalin
 */
public class NextTaskSender {

    private final Supplier<TelegramApiClient> telegramApiClientSupplier;
    private final TaskSender taskSender;
    private final AnswerSender answerSender;
    private final RatingHelper ratingHelper;
    private final TourList tourList;
    private final BotInfo botInfo;

    public NextTaskSender(
            Supplier<TelegramApiClient> telegramApiClientSupplier,
            TaskSender taskSender,
            AnswerSender answerSender,
            RatingHelper ratingHelper,
            TourList tourList,
            BotInfo botInfo) {
        this.telegramApiClientSupplier = telegramApiClientSupplier;
        this.taskSender = taskSender;
        this.answerSender = answerSender;
        this.ratingHelper = ratingHelper;
        this.tourList = tourList;
        this.botInfo = botInfo;
    }

    private TelegramApiClient getTelegramApiClient() {
        return telegramApiClientSupplier.get();
    }

    public void sendNextTask(ChatGame chatGame, @Nullable Long currentTaskId) throws GameException {
        Long chatId = chatGame.getChatId();
        try {
            NextTaskResult nextTaskResult;
            if (currentTaskId != null) {
                Optional<NextTaskResult> nextTaskResultOptional = chatGame.nextTaskIfEquals(currentTaskId);
                if (nextTaskResultOptional.isPresent()) {
                    nextTaskResult = nextTaskResultOptional.get();
                } else {
                    throw new CurrentTaskIsOtherException();
                }
            } else {
                nextTaskResult = chatGame.nextTask();
            }
            nextTaskResult.getUnansweredTask()
                    .ifPresent(unansweredTask -> sendAnswerOfPreviousTask(chatGame.getChat(), unansweredTask));
            Optional<Task> nextTaskOptional = nextTaskResult.getNextTask();
            if (!nextTaskOptional.isPresent()) {
                String messageText;
                if (nextTaskResult.isTournament()) {
                    StringBuilder messageBuilder = new StringBuilder(Emoji.GLOWING_STAR + " <b>Турнир пройден.</b>");
                    List<StatEntry> tournamentStat = nextTaskResult.getTournamentStat();
                    if (tournamentStat != null && !tournamentStat.isEmpty()) {
                        messageBuilder.append("\n<i>Счёт Знатоки против Бота: </i>")
                                .append(createScoreMessage(tournamentStat));
                    }
                    messageText = messageBuilder.toString();
                } else {
                    messageText = "<i>Новых вопросов пока что больше нет!</i>";
                }
                Message message = new Message(messageText, chatId, ParseMode.HTML);
                message.setReplyMarkup(TelegramApiUtil.createInlineButtonMarkup("Выбрать категорию", "category"));
                getTelegramApiClient().sendMessage(message);
            } else {
                ReplyMarkup replyMarkup;
                Task nextTask = nextTaskOptional.get();
                Long taskId = nextTask.getId();
                if (chatGame.getChat().isPrivate()) {
                    replyMarkup = TelegramApiUtil.createInlineButtonMarkup("Ответ", "answer " + taskId,
                            "Дальше", "next " + taskId);
                } else {
                    replyMarkup = TelegramApiUtil.createInlineButtonMarkup("Подсказка", "hint " + taskId,
                            "Дальше", "next " + taskId);
                }
                StringBuilder messageBuilder = new StringBuilder();
                messageBuilder.append(Emoji.BLACK_QUESTION_MARK_ORNAMENT + "<b>Внимание, вопрос!</b>  ");
                if (chatGame instanceof AutoChatGame) {
                    messageBuilder.append(Emoji.HOURGLASS_WITH_FLOWING_SAND + "<b>")
                            .append(((AutoChatGame) chatGame).getTimeout() / 60)
                            .append("</b>");
                }
                messageBuilder.append("\n");
                taskSender.sendTaskText(messageBuilder, nextTask, chatId, replyMarkup);
            }
        } catch (TournamentIsNotSelectedException e) {
            Message message = tourList.getTournamentsListOfChat(chatId, 0);
            getTelegramApiClient().sendMessage(message);
        }
    }

    private void sendAnswerOfPreviousTask(@Nonnull Chat chat, @Nonnull Task task) {
        InlineKeyboardMarkup replyMarkup = new InlineKeyboardMarkup();
        replyMarkup.setInlineKeyboard(Collections.singletonList(ratingHelper.createRatingButtons(task.getId())));
        answerSender.sendAnswerOfTask(new StringBuilder("<b>Ответ к предыдущему вопросу:</b>\n"), task,
                chat.getId(), replyMarkup, (errorResponse, httpStatus) -> {
                });
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
