package me.nizheg.telegram.bot.chgk.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

import me.nizheg.telegram.bot.chgk.dto.Category;
import me.nizheg.telegram.bot.chgk.repository.CategoryDao;
import me.nizheg.telegram.bot.chgk.service.CategoryService;

/**

 *
 * @author Nikolay Zhegalin
 */
@Service
public class CategoryServiceImpl implements CategoryService {

    @Autowired
    private CategoryDao categoryDao;

    @Override
    public Category create(Category category) {
        return categoryDao.create(category);
    }

    @Override
    public Category read(String id) {
        return categoryDao.read(id);
    }

    @Override
    public Category update(Category category) {
        return categoryDao.update(category);
    }

    @Override
    public void delete(String id) {
        categoryDao.delete(id);
    }

    @Override
    public boolean isExists(String id) {
        return categoryDao.isExists(id);
    }

    @Override
    public List<Category> getByTask(Long taskId) {
        return categoryDao.getByTask(taskId);
    }

    @Override
    public List<Category> getCollection() {
        return categoryDao.getCollection();
    }
}
