package me.nizheg.telegram.bot.chgk.service.impl;

import me.nizheg.telegram.bot.chgk.dto.Answer;
import me.nizheg.telegram.bot.chgk.repository.AnswerDao;
import me.nizheg.telegram.bot.chgk.service.AnswerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * //todo add comments
 *
 * @author Nikolay Zhegalin
 */
@Service
public class AnswerServiceImpl implements AnswerService {

    @Autowired
    private AnswerDao answerDao;

    @Override
    public Answer create(Answer answer) {
        return answerDao.create(answer);
    }

    @Override
    public Answer read(Long id) {
        return answerDao.read(id);
    }

    @Override
    public Answer update(Answer answer) {
        return answerDao.update(answer);
    }

    @Override
    public void delete(Long id) {
        answerDao.delete(id);
    }

    @Override
    public List<Answer> getByTask(Long taskId) {
        return answerDao.getByTask(taskId);
    }

    @Override
    public List<Answer> getCollection() {
        return answerDao.getCollection();
    }
}
