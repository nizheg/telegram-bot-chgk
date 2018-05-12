package me.nizheg.telegram.bot.chgk.repository.impl;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.sql.DataSource;

import me.nizheg.telegram.bot.chgk.dto.AnswerLog;
import me.nizheg.telegram.bot.chgk.dto.TelegramUser;
import me.nizheg.telegram.bot.chgk.dto.composite.StatEntry;
import me.nizheg.telegram.bot.chgk.exception.DuplicationException;
import me.nizheg.telegram.bot.chgk.repository.AnswerLogDao;
import me.nizheg.telegram.bot.chgk.repository.param.StatSearchParams;

@Repository
public class JdbcAnswerLogDao implements AnswerLogDao {

    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;
    private final JdbcTemplate template;
    private final SimpleJdbcInsert answerLogInsert;
    private final AnswerLogMapper answerLogMapper = new AnswerLogMapper();
    private final StatEntryMapper statEntryMapper = new StatEntryMapper();

    public JdbcAnswerLogDao(DataSource dataSource) {
        this.template = new JdbcTemplate(dataSource);
        namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
        this.answerLogInsert = new SimpleJdbcInsert(dataSource).withTableName("answer_log")
                .usingGeneratedKeyColumns("id");
    }

    @Override
    public AnswerLog create(AnswerLog answerLog) {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("telegram_user_id", answerLog.getTelegramUserId());
        parameters.put("chat_id", answerLog.getChatId());
        parameters.put("task_id", answerLog.getTaskId());
        parameters.put("answer_time", answerLog.getTime());
        try {
            long id = answerLogInsert.executeAndReturnKey(parameters).longValue();
            answerLog.setId(id);
        } catch (DuplicateKeyException ex) {
            throw new DuplicationException(answerLog.getChatId() + "," + answerLog.getTaskId());
        }
        return answerLog;
    }

    @Override
    @CheckForNull
    public AnswerLog getByTaskAndChat(Long taskId, Long chatId) {
        try {
            return template.queryForObject("select * from answer_log where chat_id = ? and task_id = ?",
                    new Object[] {chatId, taskId}, answerLogMapper);
        } catch (IncorrectResultSizeDataAccessException ex) {
            return null;
        }
    }

    @Override
    public boolean isExistByTaskAndChat(Long taskId, Long chatId) {
        return template.queryForObject("select exists(select 1 from answer_log where chat_id = ? and task_id = ?)",
                Boolean.class, chatId, taskId);
    }

    @Override
    public List<StatEntry> getStatForChat(Long chatId, StatSearchParams params) {
        MapSqlParameterSource parameters = new MapSqlParameterSource();
        parameters.addValue("chatId", chatId);
        StringBuilder sqlBuilder = new StringBuilder();
        sqlBuilder.append("select tu.id, tu.username, tu.firstname, tu.lastname, count(task_id) as c\n");
        sqlBuilder.append("from answer_log\n");
        sqlBuilder.append("inner join telegram_user tu on tu.id = telegram_user_id\n");
        sqlBuilder.append("where chat_id = :chatId\n");
        if (params.getExcludeUserIds() != null && !params.getExcludeUserIds().isEmpty()) {
            sqlBuilder.append("and tu.id not in (:excludeIds)\n");
            parameters.addValue("excludeIds", params.getExcludeUserIds());
        }
        sqlBuilder.append("group by tu.id, tu.username, tu.firstname, tu.lastname\n");
        sqlBuilder.append("order by c desc, max(answer_time)\n");
        if (params.getLimit() != null) {
            sqlBuilder.append("limit :lim\n");
            parameters.addValue("lim", params.getLimit());
        }
        return namedParameterJdbcTemplate.query(sqlBuilder.toString(), parameters, statEntryMapper);
    }

    @Override
    public List<StatEntry> getStatForChatUser(Long chatId, Long userId, String othersUsername) {
        return getStatForChatUserForTournament(chatId, userId, othersUsername, null);
    }

    @Override
    public List<StatEntry> getStatForChatUserForTournament(
            Long chatId,
            Long userId,
            String othersUsername,
            Long tournamentId) {
        MapSqlParameterSource parameters = new MapSqlParameterSource();
        parameters.addValue("chatId", chatId);
        parameters.addValue("userId", userId);
        StringBuilder sqlBuilder = new StringBuilder();
        if (tournamentId != null) {
            sqlBuilder.append(
                    "with t(id) as (select id from task where task.tour_id in (select id from tour where parent_id = :tournamentId))");
            parameters.addValue("tournamentId", tournamentId);
        }
        sqlBuilder.append("select tu.id, tu.username, tu.firstname, tu.lastname, count(task_id) as c\n");
        sqlBuilder.append("from answer_log\n");
        sqlBuilder.append("inner join telegram_user tu on tu.id = telegram_user_id\n");
        sqlBuilder.append("where chat_id = :chatId and telegram_user_id = :userId\n");
        if (tournamentId != null) {
            sqlBuilder.append("and task_id in (select id from t)\n");
        }
        sqlBuilder.append("group by tu.id, tu.username, tu.firstname, tu.lastname\n");
        if (othersUsername != null) {
            parameters.addValue("othersUsername", othersUsername);
            sqlBuilder.append("union\n");
            sqlBuilder.append("select -1, :othersUsername, '', '', count(task_id)\n");
            sqlBuilder.append("from answer_log\n");
            sqlBuilder.append("where chat_id = :chatId and telegram_user_id <> :userId\n");
            if (tournamentId != null) {
                sqlBuilder.append("and task_id in (select id from t)\n");
            }
        }
        return namedParameterJdbcTemplate.query(sqlBuilder.toString(), parameters, statEntryMapper);
    }

    @Override
    public void copy(Long fromChatId, Long toChatId) {
        template.update("insert into answer_log(telegram_user_id, chat_id, task_id, answer_time)\n" + //
                        "select al.telegram_user_id, ?, al.task_id, al.answer_time from answer_log al\n" + //
                        "where al.chat_id = ? \n" + //
                        "and not exists (select 1 from answer_log al2 where al2.chat_id = ? and al2.task_id = al.task_id)",
                toChatId, fromChatId, toChatId);

    }

    private static class AnswerLogMapper implements RowMapper<AnswerLog> {

        @Override
        public AnswerLog mapRow(@Nonnull ResultSet rs, int rowNum) throws SQLException {
            long id = rs.getLong("id");
            long telegramUserId = rs.getLong("telegram_user_id");
            long chatId = rs.getLong("chat_id");
            long taskId = rs.getLong("task_id");
            Date answerTime = rs.getDate("answer_time");
            AnswerLog answerLog = new AnswerLog();
            answerLog.setId(id);
            answerLog.setTelegramUserId(telegramUserId);
            answerLog.setChatId(chatId);
            answerLog.setTaskId(taskId);
            answerLog.setTime(answerTime);
            return answerLog;
        }
    }

    private static class StatEntryMapper implements RowMapper<StatEntry> {

        final JdbcTelegramUserDao.TelegramUserMapper telegramUserMapper = new JdbcTelegramUserDao.TelegramUserMapper();

        @Override
        public StatEntry mapRow(@Nonnull ResultSet rs, int rowNum) throws SQLException {
            long count = rs.getLong("c");
            TelegramUser telegramUser = telegramUserMapper.mapRow(rs, rowNum);
            StatEntry statEntry = new StatEntry();
            statEntry.setCount(count);
            statEntry.setTelegramUser(telegramUser);
            return statEntry;
        }
    }
}
