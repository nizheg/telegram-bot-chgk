package me.nizheg.telegram.bot.chgk.repository;

import java.util.List;

import lombok.NonNull;
import me.nizheg.telegram.bot.chgk.dto.Category;

/**

 *
 * @author Nikolay Zhegalin
 */
public interface CategoryDao {
    Category create(Category category);

    Category read(String id);

    Category update(Category category);

    void delete(String id);

    @NonNull
    List<Category> getByTask(Long taskId);

    List<Category> getCollection();

    boolean isExists(String id);
}
