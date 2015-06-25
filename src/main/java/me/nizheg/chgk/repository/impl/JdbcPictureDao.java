package me.nizheg.chgk.repository.impl;

import me.nizheg.chgk.dto.AttachedPicture;
import me.nizheg.chgk.dto.Picture;
import me.nizheg.chgk.repository.PictureDao;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JdbcPictureDao implements PictureDao {
    private JdbcTemplate template;

    private PictureMapper pictureMapper = new PictureMapper();
    private AttachedPictureMapper attachedPictureMapper = new AttachedPictureMapper();
    private SimpleJdbcInsert pictureInsert;

    public void setDataSource(DataSource dataSource) {
        this.template = new JdbcTemplate(dataSource);
        this.pictureInsert = new SimpleJdbcInsert(dataSource).withTableName("picture").usingGeneratedKeyColumns("id");
    }

    private static class PictureMapper implements RowMapper<Picture> {
        @Override
        public Picture mapRow(ResultSet rs, int rowNum) throws SQLException {
            Long id = rs.getLong("id");
            String telegramFileId = rs.getString("telegram_file_id");
            String sourceUrl = rs.getString("source_url");
            String caption = rs.getString("caption");
            Picture picture = new Picture();
            picture.setId(id);
            picture.setTelegramFileId(telegramFileId);
            picture.setSourceUrl(sourceUrl);
            picture.setCaption(caption);
            return picture;
        }
    }

    private static class AttachedPictureMapper implements RowMapper<AttachedPicture> {

        @Override
        public AttachedPicture mapRow(ResultSet rs, int rowNum) throws SQLException {
            Long id = rs.getLong("id");
            String telegramFileId = rs.getString("telegram_file_id");
            String sourceUrl = rs.getString("source_url");
            String caption = rs.getString("caption");
            int position = rs.getInt("position");
            AttachedPicture picture = new AttachedPicture();
            picture.setId(id);
            picture.setTelegramFileId(telegramFileId);
            picture.setSourceUrl(sourceUrl);
            picture.setCaption(caption);
            picture.setPosition(position);
            return picture;
        }

    }

    @Override
    public Picture create(Picture picture) {
        Map<String, Object> parameters = new HashMap<String, Object>(3);
        parameters.put("telegram_file_id", picture.getTelegramFileId());
        parameters.put("source_url", picture.getSourceUrl());
        parameters.put("caption", picture.getCaption());
        long id = this.pictureInsert.executeAndReturnKey(parameters).longValue();
        picture.setId(id);
        return picture;
    }

    @Override
    public Picture getById(Long id) {
        return template.queryForObject("select * from picture where id = ?", pictureMapper, id);
    }

    @Override
    public Picture update(Picture picture) {
        template.update("update picture set telegram_file_id = ?, source_url = ?, caption = ? where id = ?", picture.getTelegramFileId(),
                picture.getSourceUrl(), picture.getCaption(), picture.getId());
        return picture;
    }

    @Override
    public void delete(Long id) {
        template.update("delete from picture where id = ?", id);
    }

    @Override
    public boolean hasPictureLinks(Long id) {
        return 0 < template.queryForObject("select count(task_id) from (select task_id from task_picture where picture_id = ? "
                + "union select task_id from comment_picture where picture_id = ?) as t", Long.class, id, id);
    }

    @Override
    public void savePictureToTaskTextAtPosition(Long pictureId, Long taskId, int position) {
        template.update("insert into task_picture(task_id, picture_id, position) values(?,?,?)", taskId, pictureId, position);
    }

    @Override
    public List<AttachedPicture> getPicturesOfTaskText(Long taskId) {
        return template.query(
                "select p.*, tp.position from task_picture tp inner join picture p on p.id = tp.picture_id where tp.task_id = ? order by tp.position",
                attachedPictureMapper, taskId);
    }

    @Override
    public void updatePictureTaskTextPosition(Long pictureId, Long taskId, int position) {
        template.update("update task_picture set position = ? where picture_id = ? and task_id = ?", position, pictureId, taskId);
    }

    @Override
    public void deletePictureFromTaskText(Long taskId, Long pictureId) {
        template.update("delete from task_picture where task_id = ? and picture_id = ?", taskId, pictureId);
    }

    @Override
    public void savePictureToTaskCommentAtPosition(Long pictureId, Long taskId, int position) {
        template.update("insert into comment_picture(task_id, picture_id, position) values(?,?,?)", taskId, pictureId, position);
    }

    @Override
    public List<AttachedPicture> getPicturesOfComment(Long taskId) {
        return template.query(
                "select p.*, cp.position from comment_picture cp inner join picture p on p.id = cp.picture_id where cp.task_id = ? order by cp.position",
                attachedPictureMapper, taskId);
    }

    @Override
    public void updatePictureTaskCommentPosition(Long pictureId, Long taskId, int position) {
        template.update("update comment_picture set position = ? where picture_id = ? and task_id = ?", position, pictureId, taskId);
    }

    @Override
    public void deletePictureFromTaskComment(Long taskId, Long pictureId) {
        template.update("delete from comment_picture where task_id = ? and picture_id = ?", taskId, pictureId);
    }
}
