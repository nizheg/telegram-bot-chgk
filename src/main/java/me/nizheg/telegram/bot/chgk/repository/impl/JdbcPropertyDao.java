package me.nizheg.telegram.bot.chgk.repository.impl;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import me.nizheg.telegram.bot.chgk.dto.Property;
import me.nizheg.telegram.bot.chgk.repository.PropertyDao;

/**

 *
 * @author Nikolay Zhegalin
 */
@Repository
public class JdbcPropertyDao implements PropertyDao {

    private final JdbcTemplate template;
    private final PropertyMapper propertyMapper = new PropertyMapper();
    private final SimpleJdbcInsert propertyInsert;

    public JdbcPropertyDao(DataSource dataSource) {
        this.template = new JdbcTemplate(dataSource);
        this.propertyInsert = new SimpleJdbcInsert(dataSource).withTableName("property").usingGeneratedKeyColumns("id");
    }

    @Override
    public Property create(Property property) {
        Map<String, Object> parameters = new HashMap<>(3);
        parameters.put("key", property.getKey());
        parameters.put("value", property.getValue());
        parameters.put("chat_id", property.getChatId());
        parameters.put("change_time", new Date());
        this.propertyInsert.execute(parameters);
        return property;
    }

    @Override
    public Property read(String key) {
        return template.queryForObject(
                "SELECT * FROM property WHERE key = ? AND chat_id IS NULL ORDER BY change_time DESC LIMIT 1",
                propertyMapper, key);
    }

    @Override
    public Property readByKeyAndChatId(String key, Long chatId) {
        return template.queryForObject(
                "SELECT * FROM property WHERE key = ? AND chat_id = ? ORDER BY change_time DESC LIMIT 1",
                propertyMapper, key, chatId);
    }

    @Override
    public List<Long> readChatIdsByKeyAndValue(String key, String value) {
        return template.queryForList("SELECT DISTINCT chat_id FROM property WHERE key = ? AND value = ?", Long.class,
                key, value);
    }

    @Override
    public Property update(Property property) {
        if (property.getChatId() == null) {
            template.update("UPDATE property SET value = ?, change_time = ? WHERE key = ? AND chat_id IS NULL",
                    property.getValue(), new Date(),
                    property.getKey());
        } else {
            template.update("UPDATE property SET value = ?, change_time = ? WHERE key = ? AND chat_id = ?",
                    property.getValue(), new Date(), property.getKey(),
                    property.getChatId());
        }
        return property;
    }

    @Override
    public void delete(Property property) {
        if (property.getChatId() == null) {
            template.update("DELETE FROM property WHERE key = ? AND chat_id IS NULL", property.getKey());
        } else {
            template.update("DELETE FROM property WHERE key = ? AND chat_id = ?", property.getKey(),
                    property.getChatId());
        }
    }

    @Override
    public boolean isExist(String key) {
        return 0 < template.queryForObject("SELECT count(key) FROM property WHERE key = ? AND chat_id IS NULL",
                Long.class, key);
    }

    @Override
    public boolean isExist(String key, Long chatId) {
        return 0 < template.queryForObject("SELECT count(key) FROM property WHERE key = ? AND chat_id = ?", Long.class,
                key, chatId);
    }

    @Override
    public void copyProperties(Long fromChatId, Long toChatId) {
        template.update(
                "INSERT INTO property(key, value, chat_id, change_time) SELECT s.key, s.value, ?, ? FROM property s WHERE chat_id = ?",
                toChatId,
                new Date(), fromChatId);
    }

    private static class PropertyMapper implements RowMapper<Property> {

        @Override
        public Property mapRow(ResultSet rs, int rowNum) throws SQLException {
            String key = rs.getString("key");
            String value = rs.getString("value");
            Property property = new Property();
            property.setKey(key);
            property.setValue(value);
            return property;
        }
    }
}
