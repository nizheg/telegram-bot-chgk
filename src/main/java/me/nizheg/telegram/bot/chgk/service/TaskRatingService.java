package me.nizheg.telegram.bot.chgk.service;

import me.nizheg.telegram.bot.chgk.dto.TaskRating;

/**
 * @author Nikolay Zhegalin
 */
public interface TaskRatingService {

    TaskRating upTaskRatingByUser(Long taskId, Long telegramUserId);

    TaskRating downTaskRatingByUser(Long taskId, Long telegramUserId);

    TaskRating getTaskRatingByTaskId(Long taskId);
}
