package me.nizheg.chgk.repository.impl;

import me.nizheg.chgk.dto.Property;
import me.nizheg.chgk.repository.PropertyDao;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * //todo add comments
 *
 * @author Nikolay Zhegalin
 */
@Repository
public class JdbcPropertyDao implements PropertyDao {

    private JdbcTemplate template;
    private PropertyMapper propertyMapper = new PropertyMapper();
    private SimpleJdbcInsert propertyInsert;

    public void setDataSource(DataSource dataSource) {
        this.template = new JdbcTemplate(dataSource);
        this.propertyInsert = new SimpleJdbcInsert(dataSource).withTableName("property").usingGeneratedKeyColumns("id");
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

    @Override
    public Property create(Property property) {
        Map<String, Object> parameters = new HashMap<String, Object>(3);
        parameters.put("key", property.getKey());
        parameters.put("value", property.getValue());
        parameters.put("chat_id", property.getChatId());
        parameters.put("change_time", new Date());
        this.propertyInsert.execute(parameters);
        return property;
    }

    @Override
    public Property read(String key) {
        return template.queryForObject("select * from property where key = ? and chat_id is null order by change_time desc limit 1", propertyMapper, key);
    }

    @Override
    public Property readByKeyAndChatId(String key, Long chatId) {
        return template.queryForObject("select * from property where key = ? and chat_id = ? order by change_time desc limit 1", propertyMapper, key, chatId);
    }

    @Override
    public List<Long> readChatIdsByKeyAndValue(String key, String value) {
        return template.queryForList("select distinct chat_id from property where key = ? and value = ?", Long.class, key, value);
    }

    @Override
    public Property update(Property property) {
        if (property.getChatId() == null) {
            template.update("update property set value = ?, change_time = ? where key = ? and chat_id is null", property.getValue(), new Date(),
                    property.getKey());
        } else {
            template.update("update property set value = ?, change_time = ? where key = ? and chat_id = ?", property.getValue(), new Date(), property.getKey(),
                    property.getChatId());
        }
        return property;
    }

    @Override
    public void delete(Property property) {
        if (property.getChatId() == null) {
            template.update("delete from property where key = ? and chat_id is null", property.getKey());
        } else {
            template.update("delete from property where key = ? and chat_id = ?", property.getKey(), property.getChatId());
        }
    }

    @Override
    public boolean isExist(String key) {
        return 0 < template.queryForObject("select count(key) from property where key = ? and chat_id is null", Long.class, key);
    }

    @Override
    public boolean isExist(String key, Long chatId) {
        return 0 < template.queryForObject("select count(key) from property where key = ? and chat_id = ?", Long.class, key, chatId);
    }

    @Override
    public void copyProperties(Long fromChatId, Long toChatId) {
        template.update("insert into property(key, value, chat_id, change_time) select s.key, s.value, ?, ? from property s where chat_id = ?", toChatId,
                new Date(), fromChatId);
    }
}
