package me.nizheg.telegram.bot.chgk.repository.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

import javax.sql.DataSource;

import me.nizheg.telegram.bot.chgk.dto.TaskRating;
import me.nizheg.telegram.bot.chgk.repository.TaskRatingDao;

/**
 * @author Nikolay Zhegalin
 */
@Repository
public class JdbcTaskRatingDao implements TaskRatingDao {

    private final Log logger = LogFactory.getLog(getClass());
    private final JdbcTemplate template;
    private final TransactionTemplate newTransaction;

    public JdbcTaskRatingDao(DataSource dataSource, PlatformTransactionManager txManager) {
        this.template = new JdbcTemplate(dataSource);
        newTransaction = new TransactionTemplate(txManager);
        newTransaction.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void setRatingOfTaskByUser(final long taskId, final long telegramUserId, final int value) {
        try {
            template.update("INSERT INTO task_rating(task_id, telegram_user_id, value) VALUES(?, ?, ?)", taskId,
                    telegramUserId, value);
        } catch (DataIntegrityViolationException ex) {
            if (logger.isDebugEnabled()) {
                logger.debug("Task " + taskId + " is voted yet for user " + telegramUserId + ". Updating of it.", ex);
            }
            // postgres don't allow do it in the same transaction
            newTransaction.execute(new TransactionCallbackWithoutResult() {
                @Override
                protected void doInTransactionWithoutResult(TransactionStatus transactionStatus) {
                    template.update("UPDATE task_rating SET value = ? WHERE task_id = ? AND telegram_user_id = ?",
                            value, taskId, telegramUserId);
                }
            });
        }

    }

    @Override
    public TaskRating getTaskRatingByTaskId(final Long taskId) {
        return template.queryForObject("WITH tr AS (SELECT value FROM task_rating WHERE task_id = ?)\n" + //
                "SELECT tru.c AS up_count, trd.c AS down_count FROM\n" + //
                "(SELECT count(*) FROM tr WHERE value > 0) tru(c),\n" + //
                "(SELECT count(*) FROM tr WHERE value < 0) trd(c)", (rs, rowNum) -> {
                    TaskRating taskRating = new TaskRating();
                    taskRating.setTaskId(taskId);
                    taskRating.setLikesCount(rs.getLong("up_count"));
                    taskRating.setDislikesCount(rs.getLong("down_count"));
                    return taskRating;
                }, taskId);
    }
}
