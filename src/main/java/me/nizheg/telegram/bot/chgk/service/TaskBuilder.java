package me.nizheg.telegram.bot.chgk.service;

import me.nizheg.telegram.bot.chgk.dto.composite.Task;

public interface TaskBuilder {

    TaskBuilder tourIdAndNumber(long tourId, int number);

    TaskBuilder questionText(String taskText);

    TaskBuilder questionComment(String taskComment);

    TaskBuilder questionAnswerAndPassCriteria(String answerText, String passCriteria);

    TaskBuilder questionComplexity(String complexity);

    Task build();
}
