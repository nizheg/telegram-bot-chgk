package me.nizheg.telegram.bot.chgk.web;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import me.nizheg.telegram.bot.chgk.dto.AttachedPicture;
import me.nizheg.telegram.bot.chgk.dto.Category;
import me.nizheg.telegram.bot.chgk.dto.LightTask;
import me.nizheg.telegram.bot.chgk.exception.OperationForbiddenException;
import me.nizheg.telegram.bot.chgk.service.CategoryService;
import me.nizheg.telegram.bot.chgk.service.PictureService;
import me.nizheg.telegram.bot.chgk.service.TaskService;

@RestController
@RequestMapping("api/task")
public class TaskController {

    private final Log logger = LogFactory.getLog(getClass());
    private final TaskService taskService;
    private final CategoryService categoryService;
    private final PictureService pictureService;

    public TaskController(
            TaskService taskService,
            CategoryService categoryService,
            PictureService pictureService) {
        this.taskService = taskService;
        this.categoryService = categoryService;
        this.pictureService = pictureService;
    }

    @RequestMapping(method = RequestMethod.POST)
    public LightTask create(@RequestBody LightTask task) {
        return taskService.create(task);
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    public LightTask read(@PathVariable Long id) {
        logger.debug("get task " + id);
        return taskService.read(id);
    }

    @RequestMapping(method = RequestMethod.GET)
    public List<LightTask> readAll(@RequestParam(required = false) LightTask.Status status) {
        logger.debug("get tasks");
        if (status != null) {
            return taskService.getByStatus(status);
        }
        return taskService.getCollection();
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.PUT)
    public LightTask update(@PathVariable long id, @RequestBody LightTask task) {
        logger.debug("update task " + id);
        task.setId(id);
        return taskService.update(task);
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
    public void delete(@PathVariable Long id) {
        logger.debug("delete task " + id);
        taskService.delete(id);
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.PATCH)
    public LightTask patch(
            @PathVariable Long id,
            @RequestBody StatusWrapper statusWrapper,
            Authentication authentication) {
        LightTask.Status status = statusWrapper.getStatus();
        logger.debug("change status of task " + id + " to " + status);
        if (status == LightTask.Status.PUBLISH_READY && taskService.read(id).getStatus() != LightTask.Status.PUBLISHED
                || userHasAuthority(authentication, "manage_task_status")) {
            return taskService.changeStatus(id, status);
        } else {
            throw new OperationForbiddenException("Operation forbidden");
        }

    }

    private static boolean userHasAuthority(Authentication authentication, String authority) {
        return authentication.isAuthenticated() &&
                authentication.getAuthorities().stream().anyMatch(a -> authority.equals(a.getAuthority()));
    }

    @RequestMapping(value = "/{id}/category", method = RequestMethod.POST)
    public List<Category> addCategory(@PathVariable Long id, @RequestParam String categoryId) {
        logger.debug("add category " + categoryId + " to task " + id);
        taskService.addCategory(id, categoryId);
        return categoryService.getByTask(id);
    }

    @RequestMapping(value = "/{id}/category", method = RequestMethod.GET)
    public List<Category> getCategories(@PathVariable Long id) {
        logger.debug("get categories of task " + id);
        return categoryService.getByTask(id);
    }

    @RequestMapping(value = "/{id}/category/{categoryId}", method = RequestMethod.DELETE)
    public List<Category> removeCategory(@PathVariable Long id, @PathVariable String categoryId) {
        logger.debug("remove category " + categoryId + " from task " + id);
        taskService.removeCategory(id, categoryId);
        return categoryService.getByTask(id);
    }

    @RequestMapping(value = "/{taskId}/taskPicture", method = RequestMethod.POST)
    public void attachPictureToTaskText(@PathVariable Long taskId, @RequestBody AttachedPicture attachedPicture) {
        logger.debug("attach picture " + attachedPicture.getId() + " to task " + taskId);
        pictureService.savePictureToTaskTextAtPosition(attachedPicture.getId(), taskId, attachedPicture.getPosition());
    }

    @RequestMapping(value = "/{taskId}/taskPicture", method = RequestMethod.GET)
    public List<AttachedPicture> getTaskTextPictures(@PathVariable Long taskId) {
        logger.debug("get pictures of task " + taskId);
        return pictureService.getPicturesOfTaskText(taskId);
    }

    @RequestMapping(value = "/{taskId}/taskPicture/{pictureId}", method = RequestMethod.PUT)
    public void updatePictureOfTaskText(
            @PathVariable Long taskId,
            @PathVariable Long pictureId,
            @RequestBody AttachedPicture attachedPicture) {
        logger.debug("update picture " + pictureId + " of task " + taskId);
        attachedPicture.setId(pictureId);
        pictureService.updatePictureOfTaskText(attachedPicture, taskId);
    }

    @RequestMapping(value = "/{taskId}/taskPicture/{pictureId}", method = RequestMethod.DELETE)
    public void deletePictureFromTaskText(@PathVariable Long taskId, @PathVariable Long pictureId) {
        logger.debug("delete picture " + pictureId + " from task " + taskId);
        pictureService.deletePictureFromTaskText(taskId, pictureId);
    }

    @RequestMapping(value = "/{taskId}/commentPicture", method = RequestMethod.POST)
    public void attachPictureToTaskComment(@PathVariable Long taskId, @RequestBody AttachedPicture attachedPicture) {
        logger.debug("attach picture " + attachedPicture.getId() + " to task comment " + taskId);
        pictureService.savePictureToTaskCommentAtPosition(attachedPicture.getId(), taskId,
                attachedPicture.getPosition());
    }

    @RequestMapping(value = "/{taskId}/commentPicture", method = RequestMethod.GET)
    public List<AttachedPicture> getTaskCommentPictures(@PathVariable Long taskId) {
        logger.debug("get pictures of task comment " + taskId);
        return pictureService.getPicturesOfTaskComment(taskId);
    }

    @RequestMapping(value = "/{taskId}/commentPicture/{pictureId}", method = RequestMethod.PUT)
    public void updatePictureOfTaskComment(
            @PathVariable Long taskId,
            @PathVariable Long pictureId,
            @RequestBody AttachedPicture attachedPicture) {
        logger.debug("update picture " + pictureId + " of task comment " + taskId);
        attachedPicture.setId(pictureId);
        pictureService.updatePictureOfTaskComment(attachedPicture, taskId);
    }

    @RequestMapping(value = "/{taskId}/commentPicture/{pictureId}", method = RequestMethod.DELETE)
    public void deletePictureFromTaskComment(@PathVariable Long taskId, @PathVariable Long pictureId) {
        logger.debug("delete picture " + pictureId + " from task comment " + taskId);
        pictureService.deletePictureFromTaskComment(taskId, pictureId);
    }


    public static class StatusWrapper {

        private LightTask.Status status;

        public LightTask.Status getStatus() {
            return status;
        }

        public void setStatus(LightTask.Status status) {
            this.status = status;
        }
    }
}
