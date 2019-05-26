package me.nizheg.telegram.bot.chgk.service.impl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import lombok.RequiredArgsConstructor;
import me.nizheg.telegram.bot.chgk.dto.AttachedPicture;
import me.nizheg.telegram.bot.chgk.dto.LightTask;
import me.nizheg.telegram.bot.chgk.dto.Picture;
import me.nizheg.telegram.bot.chgk.exception.OperationForbiddenException;
import me.nizheg.telegram.bot.chgk.repository.PictureDao;
import me.nizheg.telegram.bot.chgk.repository.TaskDao;
import me.nizheg.telegram.bot.chgk.service.PictureService;

@Service
@RequiredArgsConstructor
public class PictureServiceImpl implements PictureService {

    private final PictureDao pictureDao;
    private final TaskDao taskDao;

    @Override
    public Picture create(Picture picture) {
        return pictureDao.create(picture);
    }

    @Override
    public Picture getById(Long id) {
        return pictureDao.getById(id);
    }

    @Override
    public Picture update(Picture picture) {
        //todo: check permissions
        return pictureDao.update(picture);
    }

    @Override
    public void savePictureToTaskTextAtPosition(Long pictureId, Long taskId, int position) {
        checkPermissionsForTaskId(taskId);
        pictureDao.savePictureToTaskTextAtPosition(pictureId, taskId, position);

    }

    @Override
    public List<AttachedPicture> getPicturesOfTaskText(Long taskId) {
        return pictureDao.getPicturesOfTaskText(taskId);
    }

    @Override
    @Transactional
    public AttachedPicture updatePictureOfTaskText(AttachedPicture picture, Long taskId) {
        checkPermissionsForTaskId(taskId);
        pictureDao.update(picture);
        pictureDao.updatePictureTaskTextPosition(picture.getId(), taskId, picture.getPosition());
        return picture;
    }

    @Override
    @Transactional
    public void deletePictureFromTaskText(Long taskId, Long pictureId) {
        checkPermissionsForTaskId(taskId);
        pictureDao.deletePictureFromTaskText(taskId, pictureId);
        if (!pictureDao.hasPictureLinks(pictureId)) {
            pictureDao.delete(pictureId);
        }
    }

    @Override
    public void savePictureToTaskCommentAtPosition(Long pictureId, Long taskId, int position) {
        checkPermissionsForTaskId(taskId);
        pictureDao.savePictureToTaskCommentAtPosition(pictureId, taskId, position);

    }

    @Override
    public List<AttachedPicture> getPicturesOfTaskComment(Long taskId) {
        return pictureDao.getPicturesOfComment(taskId);
    }

    @Override
    @Transactional
    public AttachedPicture updatePictureOfTaskComment(AttachedPicture picture, Long taskId) {
        checkPermissionsForTaskId(taskId);
        pictureDao.update(picture);
        pictureDao.updatePictureTaskCommentPosition(picture.getId(), taskId, picture.getPosition());
        return picture;
    }

    @Override
    @Transactional
    public void deletePictureFromTaskComment(Long taskId, Long pictureId) {
        checkPermissionsForTaskId(taskId);
        pictureDao.deletePictureFromTaskComment(taskId, pictureId);
        if (!pictureDao.hasPictureLinks(pictureId)) {
            pictureDao.delete(pictureId);
        }
    }

    private void checkPermissionsForTaskId(Long taskId) {
        LightTask savedTask = taskDao.getById(taskId);
        if (savedTask != null && savedTask.getStatus() == LightTask.Status.PUBLISHED) {
            throw new OperationForbiddenException("It is forbidden to change answers of task in PUBLISHED status");
        }
    }

}
