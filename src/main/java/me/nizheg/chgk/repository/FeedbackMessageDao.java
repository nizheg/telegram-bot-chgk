package me.nizheg.chgk.repository;

import me.nizheg.chgk.dto.FeedbackMessage;

import java.util.Date;

/**
 * //todo add comments
 *
 * @author Nikolay Zhegalin
 */
public interface FeedbackMessageDao {
    FeedbackMessage create(FeedbackMessage feedbackMessage);

    FeedbackMessage read(Long id);

    int countForUserFromDate(Long telegramUserId, Date date);
}
