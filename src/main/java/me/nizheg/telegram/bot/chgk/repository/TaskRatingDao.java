package me.nizheg.telegram.bot.chgk.repository;

import me.nizheg.telegram.bot.chgk.dto.TaskRating;

/**
 * @author Nikolay Zhegalin
 */
public interface TaskRatingDao {

    void setRatingOfTaskByUser(long taskId, long telegramUserId, int i);

    TaskRating getTaskRatingByTaskId(Long taskId);
}
