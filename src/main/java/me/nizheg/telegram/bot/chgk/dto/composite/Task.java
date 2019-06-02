package me.nizheg.telegram.bot.chgk.dto.composite;

import java.util.List;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import me.nizheg.telegram.bot.chgk.dto.Answer;
import me.nizheg.telegram.bot.chgk.dto.AttachedPicture;
import me.nizheg.telegram.bot.chgk.dto.LightTask;

/**
 * @author Nikolay Zhegalin
 */
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@Data
public class Task extends LightTask {

    private static final long serialVersionUID = -3770141731175407348L;
    private List<Answer> answers;
    private List<AttachedPicture> textPictures;
    private List<AttachedPicture> commentPictures;
    private List<String> categories;

    public Task(LightTask lightTask) {
        super(lightTask.getId(),
                lightTask.getText(),
                lightTask.getImportedText(),
                lightTask.getComment(),
                lightTask.getStatus(),
                lightTask.getTourId(),
                lightTask.getNumberInTour());
    }

    public Task() {
        super();
    }
}
