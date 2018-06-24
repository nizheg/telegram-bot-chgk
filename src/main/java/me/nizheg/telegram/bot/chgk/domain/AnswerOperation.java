package me.nizheg.telegram.bot.chgk.domain;

import me.nizheg.telegram.bot.chgk.dto.composite.Task;

/**
 * @author Nikolay Zhegalin
 */
public interface AnswerOperation {
    void sendAnswerWithRatingAndNextButtons(Task task, long chatId);
}
