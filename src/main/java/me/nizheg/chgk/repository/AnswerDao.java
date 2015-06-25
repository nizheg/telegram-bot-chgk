package me.nizheg.chgk.repository;

import me.nizheg.chgk.dto.Answer;

import java.util.List;

public interface AnswerDao {
	Answer create(Answer answer);

	Answer read(Long id);

	Answer update(Answer answer);

	void delete(Long id);

	List<Answer> getByTask(Long taskId);

	List<Answer> getCollection();

}
