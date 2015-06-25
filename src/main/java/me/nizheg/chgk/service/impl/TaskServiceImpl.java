package me.nizheg.chgk.service.impl;

import java.util.Date;
import java.util.List;

import me.nizheg.chgk.dto.Category;
import me.nizheg.chgk.dto.LightTask;
import me.nizheg.chgk.dto.LightTour;
import me.nizheg.chgk.dto.UsageStat;
import me.nizheg.chgk.dto.composite.Task;
import me.nizheg.chgk.dto.composite.Tournament;
import me.nizheg.chgk.exception.DuplicationException;
import me.nizheg.chgk.repository.TaskDao;
import me.nizheg.chgk.service.AnswerService;
import me.nizheg.chgk.service.PictureService;
import me.nizheg.chgk.service.TaskService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * //todo add comments
 *
 * @author Nikolay Zhegalin
 */
@Service
public class TaskServiceImpl implements TaskService {

    @Autowired
    private TaskDao taskDao;
    @Autowired
    private AnswerService answerService;
    @Autowired
    private PictureService pictureService;

    @Transactional
    @Override
    public LightTask create(LightTask task) {
        task.setStatus(LightTask.Status.NEW);
        if (taskDao.isExist(task.getText())) {
            throw new DuplicationException("Task with such text is exist");
        }
        return taskDao.create(task);
    }

    @Override
    public LightTask read(Long id) {
        return taskDao.getById(id);
    }

    @Override
    public LightTask update(LightTask task) {
        if (task.getStatus() == null) {
            task.setStatus(LightTask.Status.NEW);
        }
        return taskDao.update(task);
    }

    @Transactional
    @Override
    public void delete(Long id) {
        LightTask task = taskDao.getById(id);
        if (task != null) {
            task.setStatus(LightTask.Status.DELETED);
            taskDao.update(task);
        }
    }

    @Override
    public List<LightTask> getCollection() {
        return taskDao.getCollection();
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void setTaskUsed(Long id, Long chatId) {
        taskDao.setUsedByChat(id, chatId);
    }

    @Override
    public Date getUsageTime(Long taskId, Long chatId) {
        return taskDao.getUsageTime(taskId, chatId);
    }

    @Override
    public LightTask getUnusedByChatTask(Long chatId, Category category) {
        if (category.getId().equals(Category.ALL)) {
            category = null;
        }
        return taskDao.getUnusedByChat(chatId, category);
    }

    @Override
    public List<LightTask> getByStatus(LightTask.Status status) {
        return taskDao.getByStatus(status);
    }

    @Override
    public List<LightTask> getByText(String text) {
        return taskDao.getByText(text);
    }

    @Transactional
    @Override
    public LightTask changeStatus(Long taskId, LightTask.Status status) {
        LightTask task = taskDao.getById(taskId);
        if (status == null || task == null) {
            return task;
        }
        task.setStatus(status);
        return taskDao.update(task);
    }

    @Override
    public void changeStatus(List<Long> taskIds, LightTask.Status fromStatus, LightTask.Status toStatus) {
        taskDao.updateStatus(taskIds, fromStatus, toStatus);
    }

    @Override
    public boolean isExist(String text) {
        return taskDao.isExist(text);
    }

    @Override
    public void addCategory(Long taskId, String categoryId) {
        taskDao.addCategory(taskId, categoryId);
    }

    @Override
    public void removeCategory(Long taskId, String categoryId) {
        taskDao.removeCategory(taskId, categoryId);
    }

    @Override
    @Transactional(readOnly = true)
    public Task createCompositeTask(Long id) {
        LightTask lightTask = read(id);
        return createCompositeTask(lightTask);
    }

    @Override
    @Transactional(readOnly = true)
    public Task createCompositeTask(LightTask lightTask) {
        if (lightTask == null) {
            return null;
        }
        Task task = new Task(lightTask);
        Long id = lightTask.getId();
        if (id != null) {
            task.setAnswers(answerService.getByTask(id));
            task.setCommentPictures(pictureService.getPicturesOfTaskComment(id));
            task.setTextPictures(pictureService.getPicturesOfTaskText(id));
        }
        return task;
    }

    @Override
    public LightTask getNextTaskInTournament(Tournament currentTournament, Task currentTask) {
        Long currentTaskTourId = currentTask == null ? null : currentTask.getTourId();
        LightTour currentTaskTourInTournament = null;
        if (currentTaskTourId != null) {
            for (LightTour lightTour : currentTournament.getChildTours()) {
                if (currentTaskTourId.equals(lightTour.getId())) {
                    currentTaskTourInTournament = lightTour;
                    break;
                }
            }
        }
        int currentNumberInTour;
        int currentTourNumber;
        if (currentTaskTourInTournament == null) {
            currentNumberInTour = 0;
            currentTourNumber = 0;
        } else {
            currentNumberInTour = currentTask == null || currentTask.getNumberInTour() == null ? 0 : currentTask.getNumberInTour();
            currentTourNumber = currentTaskTourInTournament.getNumber() == null ? 0 : currentTaskTourInTournament.getNumber();
        }
        return taskDao.getNextTaskInTournament(currentTournament.getId(), currentTourNumber, currentNumberInTour);
    }

    @Override
    public UsageStat getUsageStatForChat(long chatId, Category category) {
        if (category.getId().equals(Category.ALL)) {
            category = null;
        }
        return taskDao.getUsageStatForChatByChatId(chatId, category);
    }

    @Override
    public UsageStat getUsageStatForChatByTournament(long chatId, Tournament tournament) {
        return taskDao.getUsageStatForChatByTournament(chatId, tournament.getId());
    }
}
