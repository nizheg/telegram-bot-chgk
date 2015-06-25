package me.nizheg.chgk.repository.impl;

import me.nizheg.chgk.dto.Answer;
import me.nizheg.chgk.repository.AnswerDao;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class JdbcAnswerDao implements AnswerDao {

    private JdbcTemplate template;
    private SimpleJdbcInsert answerInsert;
    private AnswerMapper answerMapper = new AnswerMapper();

    public void setDataSource(DataSource dataSource) {
        this.template = new JdbcTemplate(dataSource);
        this.answerInsert = new SimpleJdbcInsert(dataSource).withTableName("answer").usingGeneratedKeyColumns("id");
    }

    private static class AnswerMapper implements RowMapper<Answer> {
        @Override
        public Answer mapRow(ResultSet rs, int rowNum) throws SQLException {
            String value = rs.getString("answer_text");
            Long id = rs.getLong("id");
            Long taskId = rs.getLong("task_id");
            String type = rs.getString("type");
            Answer answer = new Answer();
            answer.setId(id);
            answer.setTaskId(taskId);
            answer.setText(value);
            if (type != null) {
                answer.setType(Answer.Type.valueOf(type));
            }
            return answer;
        }
    }

    @Override
    public Answer create(Answer answer) {
        Map<String, Object> parameters = new HashMap<String, Object>(3);
        parameters.put("answer_text", answer.getText());
        parameters.put("task_id", answer.getTaskId());
        parameters.put("type", answer.getType().name());
        long id = answerInsert.executeAndReturnKey(parameters).longValue();
        answer.setId(id);
        return answer;
    }

    @Override
    public Answer read(Long id) {
        return template.queryForObject("select id, answer_text, task_id, type from answer where id = ?", new Object[] { id }, answerMapper);
    }

    @Override
    public Answer update(Answer answer) {
        template.update("update answer set answer_text = ?, task_id = ?, type = ? where id = ?", answer.getText(), answer.getTaskId(), answer.getType().name(),
                answer.getId());
        return answer;
    }

    @Override
    public void delete(Long id) {
        template.update("delete from answer where id = ?", id);
    }

    @Override
    public List<Answer> getByTask(Long taskId) {
        return template.query("select id, answer_text, task_id, type from answer where task_id = ? order by id", new Object[] { taskId }, answerMapper);
    }

    @Override
    public List<Answer> getCollection() {
        return template.query("select id, answer_text, task_id, type from answer order by id", answerMapper);
    }
}
