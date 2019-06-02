package me.nizheg.telegram.bot.chgk.dto;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LightTask implements Serializable {

    private static final long serialVersionUID = 4L;
    private long id;
    private String text;
    private String importedText;
    private String comment;
    private Status status = Status.NEW;
    private Long tourId;
    private Integer numberInTour;

    public enum Status {
        NEW, PUBLISH_READY, PUBLISHED, DELETED
    }
}
