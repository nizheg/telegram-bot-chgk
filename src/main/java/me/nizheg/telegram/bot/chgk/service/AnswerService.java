package me.nizheg.telegram.bot.chgk.service;

import me.nizheg.telegram.bot.chgk.dto.Answer;

import java.util.List;

/**
 * //todo add comments
 *
 * @author Nikolay Zhegalin
 */
public interface AnswerService {

    Answer create(Answer answer);

    Answer read(Long id);

    Answer update(Answer answer);

    void delete(Long id);

    List<Answer> getByTask(Long taskId);

    List<Answer> getCollection();
}
