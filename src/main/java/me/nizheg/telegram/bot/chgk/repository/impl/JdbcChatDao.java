package me.nizheg.telegram.bot.chgk.repository.impl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import me.nizheg.telegram.bot.chgk.dto.Chat;
import me.nizheg.telegram.bot.chgk.exception.DuplicationException;
import me.nizheg.telegram.bot.chgk.repository.ChatDao;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;

/**
 * //todo add comments
 *
 * @author Nikolay Zhegalin
 */
@Repository
public class JdbcChatDao implements ChatDao {

    private final ChatMapper chatMapper = new ChatMapper();
    private JdbcTemplate template;
    private SimpleJdbcInsert chatInsert;

    public void setDataSource(DataSource dataSource) {
        this.template = new JdbcTemplate(dataSource);
        this.chatInsert = new SimpleJdbcInsert(dataSource).withTableName("chat");
    }

    @Override
    public Chat create(Chat chat) throws DuplicationException {
        Map<String, Object> parameters = new HashMap<String, Object>(6);
        parameters.put("id", chat.getId());
        parameters.put("private", chat.isPrivate());
        parameters.put("title", chat.getTitle());
        parameters.put("username", chat.getUserName());
        parameters.put("firstname", chat.getFirstName());
        parameters.put("lastname", chat.getLastName());
        try {
            chatInsert.execute(parameters);
        } catch (DuplicateKeyException ex) {
            throw new DuplicationException(String.valueOf(chat.getId()));
        }
        return chat;
    }

    @Override
    public Chat read(long id) {
        try {
            return template.queryForObject("select * from chat where id = ?", chatMapper, id);
        } catch (IncorrectResultSizeDataAccessException ex) {
            return null;
        }
    }

    @Override
    public Chat update(Chat chat) {
        template.update("update chat set private=?, title=?, username=?, firstname=?, lastname=?  where id=?", chat.isPrivate(), chat.getTitle(),
                chat.getUserName(), chat.getFirstName(), chat.getLastName(), chat.getId());
        return chat;
    }

    @Override
    public void delete(long id) {
        template.update("delete from chat where id = ?", id);
    }

    @Override
    public boolean isExist(long id) {
        return template.queryForObject("select exists(select 1 from chat where id = ?)", Boolean.class, id);
    }

    @Override
    public List<Chat> getChatsWithScheduledOperation() {
        return template.query("select distinct chat.* from scheduled_operation so inner join chat on chat.id = so.chat_id", chatMapper);
    }

    private static class ChatMapper implements RowMapper<Chat> {
        @Override
        public Chat mapRow(ResultSet rs, int rowNum) throws SQLException {
            Long id = rs.getLong("id");
            String username = rs.getString("username");
            boolean isPrivate = rs.getBoolean("private");
            String firstname = rs.getString("firstname");
            String lastname = rs.getString("lastname");
            String title = rs.getString("title");
            Chat chat = new Chat(id, isPrivate);
            chat.setTitle(title);
            chat.setUserName(username);
            chat.setFirstName(firstname);
            chat.setLastName(lastname);
            return chat;
        }
    }

}
