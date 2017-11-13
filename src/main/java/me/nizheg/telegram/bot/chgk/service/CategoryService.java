package me.nizheg.telegram.bot.chgk.service;

import me.nizheg.telegram.bot.chgk.dto.Category;

import java.util.List;

/**
 * //todo add comments
 *
 * @author Nikolay Zhegalin
 */
public interface CategoryService {

    Category create(Category category);

    Category read(String id);

    Category update(Category category);

    void delete(String id);

    boolean isExists(String id);

    List<Category> getByTask(Long taskId);

    List<Category> getCollection();
}
