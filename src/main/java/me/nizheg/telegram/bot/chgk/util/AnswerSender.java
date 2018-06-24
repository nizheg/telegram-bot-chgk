package me.nizheg.telegram.bot.chgk.util;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import me.nizheg.telegram.bot.api.model.InlineKeyboardButton;
import me.nizheg.telegram.bot.api.model.InlineKeyboardMarkup;
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
        sendAnswer(task, true, chatId);
    }

    public void sendAnswer(Task task, boolean isWithButtons, Long chatId) {
        InlineKeyboardMarkup replyMarkup = null;
        if (isWithButtons) {
            replyMarkup = new InlineKeyboardMarkup();
            List<List<InlineKeyboardButton>> buttonGroup = new ArrayList<>();
            buttonGroup.add(ratingHelper.createRatingButtons(task.getId()));
            InlineKeyboardButton nextButton = new InlineKeyboardButton();
            nextButton.setText("Дальше");
            nextButton.setCallbackData("next");
            buttonGroup.add(Collections.singletonList(nextButton));
            replyMarkup.setInlineKeyboard(buttonGroup);
        }
        StringBuilder messageBuilder = new StringBuilder("<b>Ответ:</b>\n");
        sendAnswerOfTask(messageBuilder, task, chatId, replyMarkup);
    }

    public void sendAnswerOfTask(StringBuilder resultBuilder, Task task, Long chatId, ReplyMarkup replyMarkup) {
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
        taskSender.sendTaskComment(resultBuilder, task, chatId, replyMarkup);
    }

}
