package me.nizheg.telegram.bot.chgk.repository.impl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

import javax.sql.DataSource;

import me.nizheg.telegram.bot.chgk.dto.ScheduledOperation;
import me.nizheg.telegram.bot.chgk.exception.DuplicationException;
import me.nizheg.telegram.bot.chgk.repository.ScheduledOperationDao;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

/**
 * @author Nikolay Zhegalin
 */
public class JdbcScheduledOperationDao implements ScheduledOperationDao {
    private JdbcTemplate template;

    private ScheduledOperationMapper mapper = new ScheduledOperationMapper();

    public void setDataSource(DataSource dataSource) {
        this.template = new JdbcTemplate(dataSource);
    }

    @Override
    public ScheduledOperation create(ScheduledOperation operation) throws DuplicationException {
        try {
            template.update("insert into scheduled_operation (chat_id, operation_id, scheduling_time) values (?,?,?)", operation.getChatId(),
                    operation.getOperationId(), operation.getTime());
        } catch (DuplicateKeyException ex) {
            throw new DuplicationException(operation.getChatId() + "," + operation.getOperationId());
        }
        return operation;
    }

    @Override
    public ScheduledOperation getByChatId(long chatId) {
        try {
            return template.queryForObject("select * from scheduled_operation where chat_id = ?", mapper, chatId);
        } catch (IncorrectResultSizeDataAccessException ex) {
            if (ex.getActualSize() > 1) {
                throw new IllegalStateException("Chat id should be unique");
            }
            return null;
        }
    }

    @Override
    public void deleteByChatId(long chatId) {
        template.update("delete from scheduled_operation where chat_id = ?", chatId);
    }

    private static class ScheduledOperationMapper implements RowMapper<ScheduledOperation> {
        @Override
        public ScheduledOperation mapRow(ResultSet rs, int rowNum) throws SQLException {
            long chatId = rs.getLong("chat_id");
            String operationId = rs.getString("operation_id");
            Date time = rs.getTimestamp("scheduling_time");
            ScheduledOperation operation = new ScheduledOperation();
            operation.setChatId(chatId);
            operation.setOperationId(operationId);
            operation.setTime(time);
            return operation;
        }
    }

}