package me.nizheg.telegram.bot.chgk.service.impl;

import org.springframework.stereotype.Service;

import java.util.List;

import lombok.NonNull;
import me.nizheg.telegram.bot.chgk.dto.Category;
import me.nizheg.telegram.bot.chgk.repository.CategoryDao;
import me.nizheg.telegram.bot.chgk.service.CategoryService;

/**
 * @author Nikolay Zhegalin
 */
@Service
public class CategoryServiceImpl implements CategoryService {

    private final CategoryDao categoryDao;

    public CategoryServiceImpl(CategoryDao categoryDao) {
        this.categoryDao = categoryDao;
    }

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

    @NonNull
    @Override
    public List<Category> getByTask(Long taskId) {
        return categoryDao.getByTask(taskId);
    }

    @Override
    public List<Category> getCollection() {
        return categoryDao.getCollection();
    }
}
