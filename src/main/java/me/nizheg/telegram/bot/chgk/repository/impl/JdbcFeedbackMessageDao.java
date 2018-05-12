package me.nizheg.telegram.bot.chgk.repository.impl;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.sql.DataSource;

import me.nizheg.telegram.bot.chgk.dto.FeedbackMessage;
import me.nizheg.telegram.bot.chgk.repository.FeedbackMessageDao;

@Repository
public class JdbcFeedbackMessageDao implements FeedbackMessageDao {

    private final JdbcTemplate template;
    private final SimpleJdbcInsert feedbackMessageInsert;
    private final FeedbackMessageMapper feedbackMessageMapper = new FeedbackMessageMapper();

    public JdbcFeedbackMessageDao(DataSource dataSource) {
        this.template = new JdbcTemplate(dataSource);
        this.feedbackMessageInsert = new SimpleJdbcInsert(dataSource).withTableName("feedback_message")
                .usingGeneratedKeyColumns("id");
    }

    private static class FeedbackMessageMapper implements RowMapper<FeedbackMessage> {

        @Override
        public FeedbackMessage mapRow(@Nonnull ResultSet rs, int rowNum) throws SQLException {
            String message = rs.getString("message");
            Long id = rs.getLong("id");
            Long telegramUserId = rs.getLong("telegram_user_id");
            Date time = rs.getTimestamp("message_time");
            FeedbackMessage feedbackMessage = new FeedbackMessage();
            feedbackMessage.setId(id);
            feedbackMessage.setMessage(message);
            feedbackMessage.setTelegramUserId(telegramUserId);
            feedbackMessage.setTime(time);
            return feedbackMessage;
        }
    }

    @Override
    public FeedbackMessage create(FeedbackMessage feedbackMessage) {
        Map<String, Object> parameters = new HashMap<>(3);
        parameters.put("message", feedbackMessage.getMessage());
        parameters.put("telegram_user_id", feedbackMessage.getTelegramUserId());
        parameters.put("message_time", feedbackMessage.getTime());
        long id = feedbackMessageInsert.executeAndReturnKey(parameters).longValue();
        feedbackMessage.setId(id);
        return feedbackMessage;
    }

    @Override
    public FeedbackMessage read(Long id) {
        return template.queryForObject("select id, message, telegram_user_id from feedback_message where id = ?",
                new Object[] {id}, feedbackMessageMapper);
    }

    @Override
    public int countForUserFromDate(Long telegramUserId, Date date) {
        return template.queryForObject(
                "select count(id) from feedback_message where telegram_user_id = ? and message_time > ?", Integer.class,
                telegramUserId,
                date);
    }
}
