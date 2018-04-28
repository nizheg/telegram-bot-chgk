package me.nizheg.telegram.bot.chgk.repository.impl;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import javax.sql.DataSource;

import me.nizheg.telegram.bot.chgk.dto.ChatMapping;
import me.nizheg.telegram.bot.chgk.repository.ChatMappingDao;

/**
 * //todo add comments
 *
 * @author Nikolay Zhegalin
 */
@Repository
public class JdbcChatMappingDao implements ChatMappingDao {

    private final JdbcTemplate template;
    private final ChatMappingMapper chatMappingMapper = new ChatMappingMapper();

    public JdbcChatMappingDao(DataSource dataSource) {
        this.template = new JdbcTemplate(dataSource);
    }

    private static class ChatMappingMapper implements RowMapper<ChatMapping> {
        @Override
        public ChatMapping mapRow(ResultSet rs, int rowNum) throws SQLException {
            Long groupId = rs.getLong("group_id");
            Long superGroupId = rs.getLong("supergroup_id");
            ChatMapping chatMapping = new ChatMapping();
            chatMapping.setGroupId(groupId);
            chatMapping.setSuperGroupId(superGroupId);
            return chatMapping;
        }
    }

    @Override
    public ChatMapping create(ChatMapping chatMapping) {
        template.update("insert into chat_mapping(group_id, supergroup_id) values (?, ?)", chatMapping.getGroupId(), chatMapping.getSuperGroupId());
        return chatMapping;
    }

    @Override
    public List<ChatMapping> getByGroupId(Long groupId) {
        return template.query("select * from chat_mapping where group_id = ?", chatMappingMapper, groupId);
    }

    @Override
    public List<ChatMapping> getBySuperGroupId(Long superGroupId) {
        return template.query("select * from chat_mapping where supergroup_id = ?", chatMappingMapper, superGroupId);
    }

    @Override
    public boolean isExistsForGroup(String groupId) {
        return 0 < template.queryForObject("select count(group_id) from chat_mapping where group_id = ?", Long.class, groupId);
    }

    @Override
    public boolean isExistsForSuperGroup(String superGroupId) {
        return 0 < template.queryForObject("select count(supergroup_id) from chat_mapping where supergroup_id = ?", Long.class, superGroupId);
    }
}
