package me.nizheg.telegram.bot.chgk.repository.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.sql.DataSource;

import me.nizheg.telegram.bot.chgk.dto.Category;
import me.nizheg.telegram.bot.chgk.dto.LightTask;
import me.nizheg.telegram.bot.chgk.dto.UsageStat;
import me.nizheg.telegram.bot.chgk.repository.TaskDao;

import static me.nizheg.telegram.bot.chgk.dto.LightTask.Status.PUBLISHED;

@Repository
public class JdbcTaskDao implements TaskDao {

    private final Log logger = LogFactory.getLog(getClass());
    private final JdbcTemplate template;
    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;
    private final SimpleJdbcInsert taskInsert;
    private final TaskMapper taskMapper = new TaskMapper();
    private final UsageStatMapper usageStatMapper = new UsageStatMapper();
    private final TransactionTemplate newTransaction;

    public JdbcTaskDao(DataSource dataSource, PlatformTransactionManager txManager) {
        this.template = new JdbcTemplate(dataSource);
        namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
        this.taskInsert = new SimpleJdbcInsert(dataSource).withTableName("task").usingGeneratedKeyColumns("id");
        newTransaction = new TransactionTemplate(txManager);
        newTransaction.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
    }

    @Override
    public LightTask create(LightTask task) {
        Map<String, Object> parameters = new HashMap<>(6);
        parameters.put("task_text", task.getText());
        parameters.put("imported_task_text", task.getImportedText());
        parameters.put("comment", task.getComment());
        parameters.put("status", task.getStatus().name());
        parameters.put("tour_id", task.getTourId());
        parameters.put("number_in_tour", task.getNumberInTour());
        long id = this.taskInsert.executeAndReturnKey(parameters).longValue();
        task.setId(id);
        return task;
    }

    @Override
    @CheckForNull
    public LightTask getById(Long id) {
        try {
            return template.queryForObject("select * from task where id = ?", taskMapper, id);
        } catch (IncorrectResultSizeDataAccessException ex) {
            logger.error("Error during retrieve task " + id, ex);
            return null;
        }
    }

    @Override
    public LightTask update(LightTask task) {
        template.update(
                "update task set task_text = ?, imported_task_text = ?, comment = ?, status=?, tour_id = ?, number_in_tour = ? where id = ?",
                task.getText(), task.getImportedText(), task.getComment(), task.getStatus().name(), task.getTourId(),
                task.getNumberInTour(), task.getId());
        return task;
    }

    @Override
    public void delete(Long id) {
        template.update("delete from task where id = ?", id);
    }

    @Override
    public List<LightTask> getCollection() {
        return template.query("select * from task order by id", taskMapper);
    }

    @Override
    public void setUsedByChat(final Long taskId, final Long chatId) {
        try {
            template.update("insert into used_task(task_id, chat_id) values(?, ?)", taskId, chatId);
            template.update("delete from used_task_archive where chat_id = ? and task_id = ?", chatId, taskId);
        } catch (DataIntegrityViolationException ex) {
            if (logger.isWarnEnabled()) {
                logger.warn("Task " + taskId + " is used yet for chat " + chatId + ". Updating usage time of it.");
            }
            // postgres don't allow do it in the same transaction
            newTransaction.execute(new TransactionCallbackWithoutResult() {
                @Override
                protected void doInTransactionWithoutResult(@Nonnull TransactionStatus transactionStatus) {
                    template.update("update used_task set using_time = ? where chat_id = ? and task_id = ?", new Date(),
                            chatId, taskId);
                    template.update("delete from used_task_archive where chat_id = ? and task_id = ?", chatId, taskId);
                }
            });
        }
    }

    @Override
    @CheckForNull
    public LightTask getLastUsedTask(Long chatId) {
        try {
            return template.queryForObject(
                    "select * from task where id = (select task_id from used_task where chat_id = ? order by using_time desc limit 1)",
                    taskMapper, chatId);
        } catch (IncorrectResultSizeDataAccessException ex) {
            return null;
        }
    }

    @Override
    @CheckForNull
    public OffsetDateTime getUsageTime(Long taskId, Long chatId) {
        try {
            return template.queryForObject("select using_time from used_task where chat_id = ? and task_id = ?",
                    OffsetDateTime.class, chatId, taskId);
        } catch (IncorrectResultSizeDataAccessException ex) {
            return null;
        }
    }

    @Override
    @CheckForNull
    public LightTask getUnusedByChat(Long chatId, Category category, TaskSort sort) {
        StringBuilder queryBuilder = new StringBuilder();
        Map<String, Object> parameters = new HashMap<>();
        queryBuilder.append("select * from task where id = (select id from task\n");
        if (category != null) {
            queryBuilder.append("inner join task_category tc on tc.task_id = task.id and category_id = :category \n");
            parameters.put("category", category.getId());
        }
        queryBuilder.append("left join used_task on used_task.task_id = task.id\n");
        queryBuilder.append("left join task_priority on task_priority.task_id = task.id\n");
        queryBuilder.append("where status = :status\n" +
                "and not exists (select 1 from used_task ut where ut.chat_id = :chatId and ut.task_id = task.id )\n" +
                "and not exists (select 1 from used_task_archive ut where ut.chat_id = :chatId"
                + " and ut.task_id = task.id)\n");
        if (sort == TaskSort.PRIORITY) {
            queryBuilder.append("group by id, priority\n" +
                    "order by coalesce(priority, 100) desc, max(using_time) nulls first, count(chat_id)\n");
        } else {
            queryBuilder.append("group by id, priority\n" +
                    "order by coalesce(priority, 100) >= 100 desc, max(using_time) nulls first, count(chat_id)\n");
        }
        queryBuilder.append("limit 1)");
        parameters.put("status", PUBLISHED.name());
        parameters.put("chatId", chatId);
        List<LightTask> tasks = namedParameterJdbcTemplate.query(queryBuilder.toString(), parameters, taskMapper);
        if (tasks.isEmpty()) {
            return null;
        }
        return tasks.get(0);
    }

    @Override
    @CheckForNull
    public LightTask getNextTaskInTournament(long tournamentId, int currentTourNumber, int currentNumberInTour) {
        MapSqlParameterSource parameters = new MapSqlParameterSource();
        parameters.addValue("status", PUBLISHED.name());
        parameters.addValue("tournamentId", tournamentId);
        parameters.addValue("currentNumberInTour", currentNumberInTour);
        parameters.addValue("currentTourNumber", currentTourNumber);
        List<LightTask> tasks = namedParameterJdbcTemplate.query("select * from task \n" + //
                "inner join tour on task.tour_id = tour.id\n" + //
                "where \n" + //
                "\ttour.parent_id = :tournamentId\n" + //
                "\tand task.status = :status\n" + //
                "\tand (task.number_in_tour = :currentNumberInTour + 1 and tour.number = :currentTourNumber or tour.number > :currentTourNumber)\n"
                + //
                "order by tour.number, number_in_tour\n" + //
                "limit 1", parameters, taskMapper);
        if (tasks.isEmpty()) {
            return null;
        }
        return tasks.get(0);
    }

    @Override
    public List<LightTask> getByStatus(LightTask.Status status) {
        return template.query("select * from task where status = ? order by id", taskMapper, status.name());
    }

    @Override
    public List<LightTask> getByText(String text) {
        return template.query("select * from task where " + getTaskTextCondition(), taskMapper, text, text);
    }

    @Override
    public UsageStat getUsageStatForChatByChatId(long chatId, Category category) {
        StringBuilder queryBuilder = new StringBuilder();
        MapSqlParameterSource parameters = new MapSqlParameterSource();
        parameters.addValue("status", PUBLISHED.name());
        parameters.addValue("chatId", chatId);
        queryBuilder.append("with used_task_all as (\n");
        queryBuilder.append(getUsedTaskCompositeQuery());
        queryBuilder.append(")\n");
        queryBuilder.append("select count(distinct task.id) as ct, count(distinct ut.task_id) as cu\n");
        queryBuilder.append("from task\n");
        if (category != null) {
            queryBuilder.append(
                    "inner join task_category tc on tc.task_id = task.id and tc.category_id = :categoryId\n");
            parameters.addValue("categoryId", category.getId());
        }
        queryBuilder.append("left join used_task_all ut on ut.task_id = task.id\n");
        queryBuilder.append("where task.status = :status");
        return namedParameterJdbcTemplate.queryForObject(queryBuilder.toString(), parameters, usageStatMapper);
    }

    @Override
    public UsageStat getUsageStatForChatByTournament(long chatId, long tournamentId) {
        StringBuilder queryBuilder = new StringBuilder();
        MapSqlParameterSource parameters = new MapSqlParameterSource();
        parameters.addValue("status", PUBLISHED.name());
        parameters.addValue("tournamentId", tournamentId);
        parameters.addValue("chatId", chatId);
        queryBuilder.append("with used_task_all as (\n");
        queryBuilder.append(getUsedTaskCompositeQuery());
        queryBuilder.append(")\n");
        queryBuilder.append("select count(distinct task.id) as ct, count(distinct ut.task_id) as cu\n");
        queryBuilder.append("from task\n");
        queryBuilder.append("left join used_task_all ut on ut.task_id = task.id\n");
        queryBuilder.append("where tour_id in (select id from tour where parent_id = :tournamentId)\n");
        queryBuilder.append("and task.status = :status");
        return namedParameterJdbcTemplate.queryForObject(queryBuilder.toString(), parameters, usageStatMapper);
    }

    private String getUsedTaskCompositeQuery() {
        return "select * from used_task ut where ut.chat_id = :chatId\n"//
                + "union \n" //
                + "select * from used_task_archive uta where uta.chat_id = :chatId";
    }

    @Override
    public boolean isExist(String text) {
        return template.queryForObject("select exists (select 1 from task where " + getTaskTextCondition() + ")",
                Boolean.class, text, text);
    }

    private String getTaskTextCondition() {
        return "regexp_replace(task_text, '\\s', '', 'g') = regexp_replace(?, '\\s', '', 'g') \n" + //
                "or regexp_replace(imported_task_text, '\\s', '', 'g') = regexp_replace(?, '\\s', '', 'g')";
    }

    @Override
    public void addCategory(Long taskId, String categoryId) {
        template.update("insert into task_category(task_id, category_id) values (?, ?)", taskId, categoryId);
    }

    @Override
    public void removeCategory(Long taskId, String categoryId) {
        template.update("delete from task_category where task_id = ? and category_id = ?", taskId, categoryId);
    }

    @Override
    public void copyUsedTasks(Long fromChatId, Long toChatId) {
        template.update("insert into used_task(task_id, chat_id, using_time)\n" + //
                        "select ut.task_id, ?, ut.using_time from used_task ut \n" + //
                        "where ut.chat_id = ? \n" + //
                        "and not exists (select 1 from used_task ut2 where ut2.chat_id = ? and ut2.task_id = ut.task_id)",
                toChatId, fromChatId, toChatId);
        template.update("insert into used_task_archive(task_id, chat_id, using_time)\n" + //
                        "select ut.task_id, ?, ut.using_time from used_task_archive ut \n" + //
                        "where ut.chat_id = ? \n" + //
                        "and not exists (select 1 from used_task_archive ut2 where ut2.chat_id = ? and ut2.task_id = ut.task_id)",
                toChatId, fromChatId, toChatId);
    }

    @Override
    public void deleteUsedTasks(Long chatId) {
        template.update("delete from used_task where chat_id = ?", chatId);
        template.update("delete from used_task_archive where chat_id = ?", chatId);
    }

    @Override
    public List<LightTask> getByTour(long id) {
        return template.query("select * from task where tour_id = ? order by number_in_tour", taskMapper, id);
    }

    @Override
    public void updateStatus(List<Long> taskIds, LightTask.Status fromStatus, LightTask.Status toStatus) {
        MapSqlParameterSource parameters = new MapSqlParameterSource();
        parameters.addValue("fromStatus", fromStatus.name());
        parameters.addValue("toStatus", toStatus.name());
        parameters.addValue("taskIds", taskIds);
        namedParameterJdbcTemplate.update(
                "update task set status = :toStatus where id in (:taskIds) and status=:fromStatus", parameters);

    }

    @Override
    public int archiveTasks() {
        template.execute("lock table used_task_archive, used_task, task");
        String archiveSql = "insert into used_task_archive\n"
                + "select task_id, chat_id, using_time from used_task ut\n"
                + "except\n"
                + "select ut1.task_id, ut1.chat_id, ut1.using_time\n"
                + "from used_task ut1\n"
                + "inner join ("
                + " select id, max(using_time) "
                + " from task "
                + " left join used_task on used_task.task_id = task.id "
                + " where status =:published group by id) "
                + " as t1(id, using_time) on t1.id = ut1.task_id and t1.using_time = ut1.using_time\n"
                + "except\n"
                + "select ut2.task_id, ut2.chat_id, ut2.using_time\n"
                + "from used_task ut2\n"
                + "inner join ("
                + " select chat_id, max(using_time) "
                + " from used_task "
                + " group by chat_id) "
                + " as t2(chat_id, using_time) on ut2.chat_id = t2.chat_id and ut2.using_time = t2.using_time\n"
                + "except \n"
                + "select task_id, chat_id, using_time\n"
                + "from used_task_archive";
        MapSqlParameterSource parameters = new MapSqlParameterSource();
        parameters.addValue("published", PUBLISHED.name());
        int archived = namedParameterJdbcTemplate.update(archiveSql, parameters);
        String removeMovedRecords = "delete from used_task ut "
                + "where exists ("
                + " select 1 from used_task_archive uta "
                + " where uta.task_id = ut.task_id "
                + "     and uta.chat_id = ut.chat_id "
                + "     and uta.using_time = ut.using_time)";
        int removed = template.update(removeMovedRecords);
        if (archived != removed) {
            throw new IllegalStateException("Archived and removed count is not the same");
        }
        return removed;
    }

    private static class UsageStatMapper implements RowMapper<UsageStat> {

        @Override
        public UsageStat mapRow(@Nonnull ResultSet rs, int rowNum) throws SQLException {
            UsageStat stat = new UsageStat();
            stat.setCount(rs.getLong("ct"));
            stat.setUsedCount(rs.getLong("cu"));
            return stat;
        }
    }

    private static class TaskMapper implements RowMapper<LightTask> {

        @Override
        public LightTask mapRow(@Nonnull ResultSet rs, int rowNum) throws SQLException {
            long id = rs.getLong("id");
            String text = rs.getString("task_text");
            String importedText = rs.getString("imported_task_text");
            String comment = rs.getString("comment");
            String status = rs.getString("status");
            long tourId = rs.getLong("tour_id");
            int numberInTour = rs.getInt("number_in_tour");
            LightTask task = new LightTask();
            task.setId(id);
            task.setText(text);
            task.setImportedText(importedText);
            task.setComment(comment);
            task.setStatus(LightTask.Status.valueOf(status));
            task.setTourId(tourId == 0 ? null : tourId);
            task.setNumberInTour(numberInTour == 0 ? null : numberInTour);
            return task;
        }
    }
}
