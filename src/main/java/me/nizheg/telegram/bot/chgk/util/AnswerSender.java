package me.nizheg.telegram.bot.chgk.util;

import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

import me.nizheg.telegram.bot.api.model.AtomicResponse;
import me.nizheg.telegram.bot.api.model.Callback;
import me.nizheg.telegram.bot.api.model.InlineKeyboardButton;
import me.nizheg.telegram.bot.api.model.InlineKeyboardMarkup;
import me.nizheg.telegram.bot.api.model.Message;
import me.nizheg.telegram.bot.api.model.ReplyMarkup;
import me.nizheg.telegram.bot.chgk.domain.AnswerOperation;
import me.nizheg.telegram.bot.chgk.dto.Answer;
import me.nizheg.telegram.bot.chgk.dto.composite.Task;

/**
 * @author Nikolay Zhegalin
 */
@Component
public class AnswerSender implements AnswerOperation {

    private final TaskSender taskSender;
    private final RatingHelper ratingHelper;

    public AnswerSender(TaskSender taskSender, RatingHelper ratingHelper) {
        this.taskSender = taskSender;
        this.ratingHelper = ratingHelper;
    }

    @Override
    public void sendAnswerWithRatingAndNextButtons(Task task, long chatId) {
        sendAnswer(task, true, chatId, (errorResponse, httpStatus) -> {
        });
    }

    public void sendAnswer(Task task, boolean isWithButtons, Long chatId, Callback<AtomicResponse<Message>> callback) {
        InlineKeyboardMarkup replyMarkup = null;
        if (isWithButtons) {
            long taskId = task.getId();
            replyMarkup = InlineKeyboardMarkup.column(
                    ratingHelper.createRatingButtons(taskId),
                    Collections.singletonList(InlineKeyboardButton.callbackDataButton("Дальше", "next " + taskId))
            );
        }
        StringBuilder messageBuilder = new StringBuilder("<b>Ответ:</b>\n");
        sendAnswerOfTask(messageBuilder, task, chatId, replyMarkup, callback);
    }

    public void sendAnswerOfTask(
            StringBuilder resultBuilder,
            Task task,
            Long chatId,
            ReplyMarkup replyMarkup,
            Callback<AtomicResponse<Message>> callback) {
        if (task == null) {
            return;
        }
        List<Answer> answers = task.getAnswers();
        if (!answers.isEmpty()) {
            for (Answer answer : answers) {
                resultBuilder.append(answer.getText()).append("/");
            }
        }
        if (resultBuilder.length() > 0) {
            resultBuilder.deleteCharAt(resultBuilder.length() - 1);
        }
        taskSender.sendTaskComment(resultBuilder, task, chatId, replyMarkup, callback);
    }

}
