package me.nizheg.chgk.repository;

import me.nizheg.chgk.dto.AttachedPicture;
import me.nizheg.chgk.dto.Picture;

import java.util.List;

public interface PictureDao {

    Picture create(Picture picture);

    Picture getById(Long id);

    Picture update(Picture picture);

    void delete(Long id);

    boolean hasPictureLinks(Long id);

    void savePictureToTaskTextAtPosition(Long pictureId, Long taskId, int position);

    List<AttachedPicture> getPicturesOfTaskText(Long taskId);

    void updatePictureTaskTextPosition(Long pictureId, Long taskId, int position);

    void deletePictureFromTaskText(Long taskId, Long pictureId);

    void savePictureToTaskCommentAtPosition(Long pictureId, Long taskId, int position);

    List<AttachedPicture> getPicturesOfComment(Long taskId);

    void updatePictureTaskCommentPosition(Long pictureId, Long taskId, int position);

    void deletePictureFromTaskComment(Long taskId, Long pictureId);

}
