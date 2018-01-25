package me.nizheg.telegram.bot.chgk.util;

import java.util.Arrays;
import java.util.List;

import me.nizheg.telegram.bot.api.model.InlineKeyboardMarkup;
import me.nizheg.telegram.bot.api.model.ParseMode;
import me.nizheg.telegram.bot.api.model.ReplyMarkup;
import me.nizheg.telegram.bot.api.service.TelegramApiClient;
import me.nizheg.telegram.bot.api.service.param.Message;
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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author Nikolay Zhegalin
 */
@Component
public class NextTaskSender implements NextTaskOperation {

    @Autowired
    private TelegramApiClient telegramApiClient;
    @Autowired
    private TaskSender taskSender;
    @Autowired
    private AnswerSender answerSender;
    @Autowired
    private RatingHelper ratingHelper;
    @Autowired
    private TourList tourList;
    @Autowired
    private BotInfo botInfo;

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
                        messageBuilder.append("\n<i>Cчёт Знатоки против Бота: </i>" + createScoreMessage(tournamentStat));
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
                        replyMarkup = TelegramApiUtil.createInlineButtonMarkup("Ответ", "answer " + nextTask.getId(), "Дальше", "next");
                    } else {
                        replyMarkup = TelegramApiUtil.createInlineButtonMarkup("Подсказка", "hint " + nextTask.getId(), "Дальше", "next");
                    }
                }
                StringBuilder messageBuilder = new StringBuilder();
                messageBuilder.append(Emoji.BLACK_QUESTION_MARK_ORNAMENT + "<b>Внимание, вопрос!</b>  ");
                if (chatGame instanceof AutoChatGame) {
                    messageBuilder.append(Emoji.HOURGLASS_WITH_FLOWING_SAND + "<b>" + ((AutoChatGame) chatGame).getTimeout() / 60 + "</b>");
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
            replyMarkup.setInlineKeyboard(Arrays.asList(ratingHelper.createRatingButtons(task.getId())));
            answerSender.sendAnswerOfTask(new StringBuilder("<b>Ответ к предыдущему вопросу:</b>\n"), task, chat.getId(), replyMarkup);
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
