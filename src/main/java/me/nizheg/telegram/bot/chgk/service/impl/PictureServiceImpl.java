package me.nizheg.telegram.bot.chgk.service.impl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import me.nizheg.telegram.bot.chgk.dto.AttachedPicture;
import me.nizheg.telegram.bot.chgk.dto.Picture;
import me.nizheg.telegram.bot.chgk.repository.PictureDao;
import me.nizheg.telegram.bot.chgk.service.PictureService;

@Service
public class PictureServiceImpl implements PictureService {

    private final PictureDao pictureDao;

    public PictureServiceImpl(PictureDao pictureDao) {this.pictureDao = pictureDao;}

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
        return pictureDao.update(picture);
    }

    @Override
    public void savePictureToTaskTextAtPosition(Long pictureId, Long taskId, int position) {
        pictureDao.savePictureToTaskTextAtPosition(pictureId, taskId, position);

    }

    @Override
    public List<AttachedPicture> getPicturesOfTaskText(Long taskId) {
        return pictureDao.getPicturesOfTaskText(taskId);
    }

    @Override
    @Transactional
    public AttachedPicture updatePictureOfTaskText(AttachedPicture picture, Long taskId) {
        pictureDao.update(picture);
        pictureDao.updatePictureTaskTextPosition(picture.getId(), taskId, picture.getPosition());
        return picture;
    }

    @Override
    @Transactional
    public void deletePictureFromTaskText(Long taskId, Long pictureId) {
        pictureDao.deletePictureFromTaskText(taskId, pictureId);
        if (!pictureDao.hasPictureLinks(pictureId)) {
            pictureDao.delete(pictureId);
        }
    }

    @Override
    public void savePictureToTaskCommentAtPosition(Long pictureId, Long taskId, int position) {
        pictureDao.savePictureToTaskCommentAtPosition(pictureId, taskId, position);

    }

    @Override
    public List<AttachedPicture> getPicturesOfTaskComment(Long taskId) {
        return pictureDao.getPicturesOfComment(taskId);
    }

    @Override
    @Transactional
    public AttachedPicture updatePictureOfTaskComment(AttachedPicture picture, Long taskId) {
        pictureDao.update(picture);
        pictureDao.updatePictureTaskCommentPosition(picture.getId(), taskId, picture.getPosition());
        return picture;
    }

    @Override
    @Transactional
    public void deletePictureFromTaskComment(Long taskId, Long pictureId) {
        pictureDao.deletePictureFromTaskComment(taskId, pictureId);
        if (!pictureDao.hasPictureLinks(pictureId)) {
            pictureDao.delete(pictureId);
        }
    }

}
