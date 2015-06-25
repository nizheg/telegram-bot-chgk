package me.nizheg.chgk.dto.composite;

import me.nizheg.chgk.dto.Answer;
import me.nizheg.chgk.dto.AttachedPicture;
import me.nizheg.chgk.dto.LightTask;

import java.util.List;

/**
 * @author Nikolay Zhegalin
 */
public class Task extends LightTask {

    private static final long serialVersionUID = -3770141731175407348L;
    private List<Answer> answers;
    private List<AttachedPicture> textPictures;
    private List<AttachedPicture> commentPictures;

    public Task(LightTask lightTask) {
        setId(lightTask.getId());
        setText(lightTask.getText());
        setImportedText(lightTask.getImportedText());
        setComment(lightTask.getComment());
        setStatus(lightTask.getStatus());
        setNumberInTour(lightTask.getNumberInTour());
        setTourId(lightTask.getTourId());
    }

    public List<Answer> getAnswers() {
        return answers;
    }

    public void setAnswers(List<Answer> answers) {
        this.answers = answers;
    }

    public List<AttachedPicture> getTextPictures() {
        return textPictures;
    }

    public void setTextPictures(List<AttachedPicture> textPictures) {
        this.textPictures = textPictures;
    }

    public List<AttachedPicture> getCommentPictures() {
        return commentPictures;
    }

    public void setCommentPictures(List<AttachedPicture> commentPictures) {
        this.commentPictures = commentPictures;
    }
}
