package me.nizheg.telegram.bot.chgk.repository.impl;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

import me.nizheg.telegram.bot.chgk.dto.TelegramUser;
import me.nizheg.telegram.bot.chgk.exception.DuplicationException;
import me.nizheg.telegram.bot.chgk.repository.TelegramUserDao;

/**

 *
 * @author Nikolay Zhegalin
 */
@Repository
public class JdbcTelegramUserDao implements TelegramUserDao {

    private final JdbcTemplate template;
    private final SimpleJdbcInsert telegramUserInsert;
    private final TelegramUserMapper telegramUserMapper = new TelegramUserMapper();

    public JdbcTelegramUserDao(DataSource dataSource) {
        this.template = new JdbcTemplate(dataSource);
        this.telegramUserInsert = new SimpleJdbcInsert(dataSource).withTableName("telegram_user");
    }

    static class TelegramUserMapper implements RowMapper<TelegramUser> {
        @Override
        public TelegramUser mapRow(ResultSet rs, int rowNum) throws SQLException {
            Long id = rs.getLong("id");
            String username = rs.getString("username");
            String firstname = rs.getString("firstname");
            String lastname = rs.getString("lastname");
            TelegramUser telegramUser = new TelegramUser();
            telegramUser.setId(id);
            telegramUser.setUsername(username);
            telegramUser.setFirstname(firstname);
            telegramUser.setLastname(lastname);
            return telegramUser;
        }
    }

    @Override
    public TelegramUser read(Long id) {
        try {
            return template.queryForObject("select * from telegram_user where id = ?", telegramUserMapper, id);
        } catch (IncorrectResultSizeDataAccessException ex) {
            return null;
        }
    }

    @Override
    public boolean isExist(Long id) {
        return 0 < template.queryForObject("select count(id) from telegram_user where id = ?", Long.class, id);
    }

    @Override
    public TelegramUser create(TelegramUser telegramUser) throws DuplicationException {
        Map<String, Object> parameters = new HashMap<>(4);
        parameters.put("username", telegramUser.getUsername());
        parameters.put("firstname", telegramUser.getFirstname());
        parameters.put("lastname", telegramUser.getLastname());
        parameters.put("id", telegramUser.getId());
        try {
            telegramUserInsert.execute(parameters);
        } catch (DuplicateKeyException ex) {
            throw new DuplicationException(String.valueOf(telegramUser.getId()));
        }
        return telegramUser;
    }

    @Override
    public TelegramUser update(TelegramUser telegramUser) {
        template.update("update telegram_user set username=?, firstname=?, lastname=? where id=?", telegramUser.getUsername(), telegramUser.getFirstname(),
                telegramUser.getLastname(), telegramUser.getId());
        return telegramUser;
    }

    @Override
    public TelegramUser getByUsername(String username) {
        try {
            return template.queryForObject("select * from telegram_user where username=?", telegramUserMapper, username);
        } catch (IncorrectResultSizeDataAccessException ex) {
            return null;
        }
    }

}
