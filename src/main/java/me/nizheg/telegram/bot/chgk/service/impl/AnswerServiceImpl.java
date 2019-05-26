package me.nizheg.telegram.bot.chgk.service.impl;

import org.springframework.stereotype.Service;

import java.util.List;

import lombok.RequiredArgsConstructor;
import me.nizheg.telegram.bot.chgk.dto.Answer;
import me.nizheg.telegram.bot.chgk.dto.LightTask;
import me.nizheg.telegram.bot.chgk.exception.OperationForbiddenException;
import me.nizheg.telegram.bot.chgk.repository.AnswerDao;
import me.nizheg.telegram.bot.chgk.repository.TaskDao;
import me.nizheg.telegram.bot.chgk.service.AnswerService;

/**
 * @author Nikolay Zhegalin
 */
@Service
@RequiredArgsConstructor
public class AnswerServiceImpl implements AnswerService {

    private final AnswerDao answerDao;
    private final TaskDao taskDao;

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
        checkPermissions(answer.getId());
        return answerDao.update(answer);
    }

    @Override
    public void delete(Long id) {
        checkPermissions(id);
        answerDao.delete(id);
    }

    private void checkPermissions(Long answerId) {
        Answer savedAnswer = answerDao.read(answerId);
        LightTask savedTask = taskDao.getById(savedAnswer.getTaskId());
        if (savedTask != null && savedTask.getStatus() == LightTask.Status.PUBLISHED) {
            throw new OperationForbiddenException("It is forbidden to change answers of task in PUBLISHED status");
        }
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
