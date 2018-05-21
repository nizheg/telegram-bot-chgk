package me.nizheg.telegram.bot.chgk.repository.impl;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;

import javax.annotation.Nonnull;
import javax.sql.DataSource;

import me.nizheg.telegram.bot.chgk.dto.Role;
import me.nizheg.telegram.bot.chgk.exception.DuplicationException;
import me.nizheg.telegram.bot.chgk.repository.UserRoleDao;

@Repository
public class JdbcUserRoleDao implements UserRoleDao {

    private final JdbcTemplate template;
    private final RoleMapper roleMapper = new RoleMapper();

    public JdbcUserRoleDao(DataSource dataSource) {
        this.template = new JdbcTemplate(dataSource);
    }

    private static class RoleMapper implements RowMapper<Role> {

        @Override
        public Role mapRow(@Nonnull ResultSet rs, int rowNum) throws SQLException {
            String roleName = rs.getString("role_name");
            return Role.valueOf(roleName);
        }
    }

    @Override
    public boolean userHasRole(Long telegramUserId, Role role) {
        return 0 < template
                .queryForObject(
                        "select count(telegram_user_id) from user_role where telegram_user_id = ? and role_name=? and (expiration_time >= ? or expiration_time is null)",
                        Long.class, telegramUserId, role.name(), new Date());
    }

    @Override
    public List<Role> readRolesOfUser(Long telegramUserId) {
        return template.query(
                "select role_name from user_role where telegram_user_id = ? and (expiration_time >= ? or expiration_time is null)",
                roleMapper,
                telegramUserId, new Date());
    }

    @Override
    public void assignRole(Role role, Long telegramUserId) throws DuplicationException {
        assignRoleTillTime(role, telegramUserId, null);
    }

    @Override
    public void assignRoleTillTime(Role role, Long telegramUserId, Date date) throws DuplicationException {
        try {
            template.update("insert into user_role(telegram_user_id, role_name, expiration_time) values (?,?,?)",
                    telegramUserId, role.name(), date);
        } catch (DuplicateKeyException ex) {
            throw new DuplicationException(telegramUserId + role.name());
        }
    }

    @Override
    public void revokeRole(Role role, Long telegramUserId) {
        template.update("delete from user_role where telegram_user_id = ? and role_name = ?", telegramUserId,
                role.name());
    }

    @Override
    public void deleteExpiredRoles() {
        template.update("delete from user_role where expiration_time <= ?", new Date());
    }

}
