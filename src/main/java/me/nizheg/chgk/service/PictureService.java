package me.nizheg.chgk.service;

import me.nizheg.chgk.dto.AttachedPicture;
import me.nizheg.chgk.dto.Picture;

import java.util.List;

public interface PictureService {
    Picture create(Picture picture);

    Picture getById(Long id);

    Picture update(Picture picture);

    void savePictureToTaskTextAtPosition(Long pictureId, Long taskId, int position);

    List<AttachedPicture> getPicturesOfTaskText(Long taskId);

    AttachedPicture updatePictureOfTaskText(AttachedPicture picture, Long taskId);

    void deletePictureFromTaskText(Long taskId, Long pictureId);

    void savePictureToTaskCommentAtPosition(Long pictureId, Long taskId, int position);

    List<AttachedPicture> getPicturesOfTaskComment(Long taskId);

    AttachedPicture updatePictureOfTaskComment(AttachedPicture picture, Long taskId);

    void deletePictureFromTaskComment(Long taskId, Long pictureId);
}
