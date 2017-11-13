package me.nizheg.telegram.bot.chgk.web;

import me.nizheg.telegram.bot.chgk.dto.Category;
import me.nizheg.telegram.bot.chgk.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * //todo add comments
 *
 * @author Nikolay Zhegalin
 */
@RestController
@RequestMapping("api/category")
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    @RequestMapping(method = RequestMethod.POST)
    public Category create(@RequestBody Category category) {
        return categoryService.create(category);
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    public Category get(@PathVariable String id) {
        return categoryService.read(id);
    }

    @RequestMapping(method = RequestMethod.GET)
    public List<Category> getAll() {
        return categoryService.getCollection();
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.PUT)
    public Category update(@PathVariable String id, @RequestBody Category category) {
        category.setId(id);
        return categoryService.update(category);
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
    public void delete(@PathVariable String id) {
        categoryService.delete(id);
    }
}
