package me.nizheg.chgk.repository;

import me.nizheg.chgk.dto.TaskRating;

/**
 * @author Nikolay Zhegalin
 */
public interface TaskRatingDao {

    void setRatingOfTaskByUser(long taskId, long telegramUserId, int i);

    TaskRating getTaskRatingByTaskId(Long taskId);
}
