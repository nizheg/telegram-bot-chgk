package me.nizheg.telegram.bot.chgk.util;

import org.springframework.stereotype.Component;

import java.util.List;

import me.nizheg.telegram.bot.api.model.ReplyMarkup;
import me.nizheg.telegram.bot.chgk.dto.Answer;
import me.nizheg.telegram.bot.chgk.dto.composite.Task;

/**
 * @author Nikolay Zhegalin
 */
@Component
public class AnswerSender {

    private final TaskSender taskSender;

    public AnswerSender(TaskSender taskSender) {this.taskSender = taskSender;}

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

    public void sendAnswerOfTask(Task task, Long chatId, ReplyMarkup replyMarkup) {
        sendAnswerOfTask(new StringBuilder(), task, chatId, replyMarkup);
    }

}
