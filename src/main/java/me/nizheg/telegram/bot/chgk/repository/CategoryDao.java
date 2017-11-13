package me.nizheg.telegram.bot.chgk.repository;

import me.nizheg.telegram.bot.chgk.dto.Category;

import java.util.List;

/**
 * //todo add comments
 *
 * @author Nikolay Zhegalin
 */
public interface CategoryDao {
    Category create(Category category);

    Category read(String id);

    Category update(Category category);

    void delete(String id);

    List<Category> getByTask(Long taskId);

    List<Category> getCollection();

    boolean isExists(String id);
}
