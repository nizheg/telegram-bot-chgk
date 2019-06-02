package me.nizheg.telegram.bot.chgk.service;

import java.util.List;

import lombok.NonNull;
import me.nizheg.telegram.bot.chgk.dto.Category;

/**

 *
 * @author Nikolay Zhegalin
 */
public interface CategoryService {

    Category create(Category category);

    Category read(String id);

    Category update(Category category);

    void delete(String id);

    boolean isExists(String id);

    @NonNull
    List<Category> getByTask(Long taskId);

    List<Category> getCollection();
}
