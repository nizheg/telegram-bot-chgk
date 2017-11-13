package me.nizheg.telegram.bot.chgk.repository.impl;

import java.sql.ResultSet;
import java.sql.SQLException;

import javax.sql.DataSource;

import me.nizheg.telegram.bot.chgk.dto.TaskRating;
import me.nizheg.telegram.bot.chgk.repository.TaskRatingDao;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * @author Nikolay Zhegalin
 */
@Repository
public class JdbcTaskRatingDao implements TaskRatingDao {

    private Log logger = LogFactory.getLog(getClass());
    private JdbcTemplate template;
    private TransactionTemplate newTransaction;

    public void setDataSource(DataSource dataSource) {
        this.template = new JdbcTemplate(dataSource);
    }

    @Autowired
    public void setTransactionPlatformManager(PlatformTransactionManager txManager) {
        newTransaction = new TransactionTemplate(txManager);
        newTransaction.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void setRatingOfTaskByUser(final long taskId, final long telegramUserId, final int value) {
        try {
            template.update("insert into task_rating(task_id, telegram_user_id, value) values(?, ?, ?)", taskId, telegramUserId, value);
        } catch (DataIntegrityViolationException ex) {
            if (logger.isDebugEnabled()) {
                logger.debug("Task " + taskId + " is voted yet for user " + telegramUserId + ". Updating of it.", ex);
            }
            // postgres don't allow do it in the same transaction
            newTransaction.execute(new TransactionCallbackWithoutResult() {
                @Override
                protected void doInTransactionWithoutResult(TransactionStatus transactionStatus) {
                    template.update("update task_rating set value = ? where task_id = ? and telegram_user_id = ?", value, taskId, telegramUserId);
                }
            });
        }

    }

    @Override
    public TaskRating getTaskRatingByTaskId(final Long taskId) {
        return template.queryForObject("with tr as (select value from task_rating where task_id = ?)\n" + //
                "select tru.c as up_count, trd.c as down_count from\n" + //
                "(select count(*) from tr where value > 0) tru(c),\n" + //
                "(select count(*) from tr where value < 0) trd(c)", new RowMapper<TaskRating>() {
            @Override
            public TaskRating mapRow(ResultSet rs, int rowNum) throws SQLException {
                TaskRating taskRating = new TaskRating();
                taskRating.setTaskId(taskId);
                taskRating.setLikesCount(rs.getLong("up_count"));
                taskRating.setDislikesCount(rs.getLong("down_count"));
                return taskRating;
            }
        }, taskId);
    }
}
