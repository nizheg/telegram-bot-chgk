package me.nizheg.chgk.repository.impl;

import me.nizheg.chgk.dto.ChatError;
import me.nizheg.chgk.repository.ChatErrorDao;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
 * //todo add comments
 *
 * @author Nikolay Zhegalin
 */
@Repository
public class JdbcChatErrorDao implements ChatErrorDao {

    private static final String TABLE_NAME = "chat_error";
    private static final String COLUMN_LABEL_CHAT_ID = "chat_id";
    private static final String COLUMN_LABEL_CODE = "error_code";
    private static final String COLUMN_LABEL_DESCRIPTION = "error_description";
    private static final String COLUMN_LABEL_TIME = "event_time";

    @SuppressWarnings("unused")
    private JdbcTemplate template;
    private SimpleJdbcInsert chatErrorInsert;
    @SuppressWarnings("unused")
    private ChatErrorMapper chatErrorMapper = new ChatErrorMapper();

    public void setDataSource(DataSource dataSource) {
        this.template = new JdbcTemplate(dataSource);
        this.chatErrorInsert = new SimpleJdbcInsert(dataSource).withTableName(TABLE_NAME).usingGeneratedKeyColumns("id");
    }

    private static class ChatErrorMapper implements RowMapper<ChatError> {

        @Override
        public ChatError mapRow(ResultSet rs, int rowNum) throws SQLException {
            ChatError chatError = new ChatError();
            chatError.setChatId(rs.getLong(COLUMN_LABEL_CHAT_ID));
            chatError.setCode(rs.getString(COLUMN_LABEL_CODE));
            chatError.setDescription(rs.getString(COLUMN_LABEL_DESCRIPTION));
            chatError.setTime(rs.getDate(COLUMN_LABEL_TIME));
            return chatError;
        }
    }

    @Override
    public ChatError create(ChatError chatError) {
        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put(COLUMN_LABEL_CHAT_ID, chatError.getChatId());
        parameters.put(COLUMN_LABEL_CODE, chatError.getCode());
        parameters.put(COLUMN_LABEL_DESCRIPTION, chatError.getDescription());
        parameters.put(COLUMN_LABEL_TIME, chatError.getTime());
        chatErrorInsert.execute(parameters);
        return chatError;
    }

}
