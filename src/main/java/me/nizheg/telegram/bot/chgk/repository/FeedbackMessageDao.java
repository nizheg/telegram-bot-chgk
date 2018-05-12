package me.nizheg.telegram.bot.chgk.repository;

import java.util.Date;

import me.nizheg.telegram.bot.chgk.dto.FeedbackMessage;

/**

 *
 * @author Nikolay Zhegalin
 */
public interface FeedbackMessageDao {
    FeedbackMessage create(FeedbackMessage feedbackMessage);

    FeedbackMessage read(Long id);

    int countForUserFromDate(Long telegramUserId, Date date);
}
