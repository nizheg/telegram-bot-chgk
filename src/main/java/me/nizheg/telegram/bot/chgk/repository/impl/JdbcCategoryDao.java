package me.nizheg.telegram.bot.chgk.repository.impl;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import javax.annotation.Nonnull;
import javax.sql.DataSource;

import me.nizheg.telegram.bot.chgk.dto.Category;
import me.nizheg.telegram.bot.chgk.repository.CategoryDao;

/**
 * @author Nikolay Zhegalin
 */
@Repository
public class JdbcCategoryDao implements CategoryDao {

    private final JdbcTemplate template;
    private final CategoryMapper categoryMapper = new CategoryMapper();

    public JdbcCategoryDao(DataSource dataSource) {
        this.template = new JdbcTemplate(dataSource);
    }

    @Override
    public Category create(Category category) {
        template.update("insert into category(id, category_name, position) values (?, ?, ?)", category.getId(),
                category.getName(), category.getPosition());
        return category;
    }

    @Override
    public Category read(String id) {
        return template.queryForObject("select * from category where id = ?", categoryMapper, id);
    }

    @Override
    public Category update(Category category) {
        template.update("update category set category_name = ?, position = ? where id = ?", category.getName(),
                category.getPosition(), category.getId());
        return category;
    }

    @Override
    public void delete(String id) {
        template.update("delete from category where id = ?", id);
    }

    @Override
    public List<Category> getByTask(Long taskId) {
        return template.query(
                "select c.* from task_category tc inner join category c on c.id = tc.category_id where tc.task_id = ? order by c.position",
                categoryMapper, taskId);
    }

    @Override
    public List<Category> getCollection() {
        return template.query("select * from category order by position", categoryMapper);
    }

    @Override
    public boolean isExists(String id) {
        return 0 < template.queryForObject("select count(id) from category where id = ?", Long.class, id);
    }

    private static class CategoryMapper implements RowMapper<Category> {

        @Override
        public Category mapRow(@Nonnull ResultSet rs, int rowNum) throws SQLException {
            String name = rs.getString("category_name");
            String id = rs.getString("id");
            int position = rs.getInt("position");
            Category category = new Category();
            category.setId(id);
            category.setName(name);
            category.setPosition(position);
            return category;
        }
    }
}
