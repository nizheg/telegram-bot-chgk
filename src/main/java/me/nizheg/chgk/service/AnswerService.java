package me.nizheg.chgk.service;

import me.nizheg.chgk.dto.Answer;

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
