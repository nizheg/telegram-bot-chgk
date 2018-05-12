package me.nizheg.telegram.bot.chgk.service.impl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import me.nizheg.telegram.bot.chgk.dto.TaskRating;
import me.nizheg.telegram.bot.chgk.repository.TaskRatingDao;
import me.nizheg.telegram.bot.chgk.service.TaskRatingService;

/**
 * @author Nikolay Zhegalin
 */
@Service
@Transactional
public class TaskRatingServiceImpl implements TaskRatingService {

    private final TaskRatingDao taskRatingDao;

    public TaskRatingServiceImpl(TaskRatingDao taskRatingDao) {this.taskRatingDao = taskRatingDao;}

    @Override
    public TaskRating upTaskRatingByUser(Long taskId, Long telegramUserId) {
        taskRatingDao.setRatingOfTaskByUser(taskId, telegramUserId, 1);
        return getTaskRatingByTaskId(taskId);
    }

    @Override
    public TaskRating downTaskRatingByUser(Long taskId, Long telegramUserId) {
        taskRatingDao.setRatingOfTaskByUser(taskId, telegramUserId, -1);
        return getTaskRatingByTaskId(taskId);
    }

    @Override
    public TaskRating getTaskRatingByTaskId(Long taskId) {
        return taskRatingDao.getTaskRatingByTaskId(taskId);
    }
}
