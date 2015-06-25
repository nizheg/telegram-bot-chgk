package me.nizheg.chgk.service.impl;

import me.nizheg.chgk.dto.TaskRating;
import me.nizheg.chgk.repository.TaskRatingDao;
import me.nizheg.chgk.service.TaskRatingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Nikolay Zhegalin
 */
@Service
@Transactional
public class TaskRatingServiceImpl implements TaskRatingService {

    @Autowired
    private TaskRatingDao taskRatingDao;

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
