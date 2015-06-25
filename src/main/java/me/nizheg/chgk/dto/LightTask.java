package me.nizheg.chgk.dto;

import java.io.Serializable;

public class LightTask implements Serializable {

    private static final long serialVersionUID = 4L;
    private Long id;
    private String text;
    private String importedText;
    private String comment;
    private Status status = Status.NEW;
    private Long tourId;
    private Integer numberInTour;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getImportedText() {
        return importedText;
    }

    public void setImportedText(String importedText) {
        this.importedText = importedText;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public Long getTourId() {
        return tourId;
    }

    public void setTourId(Long tourId) {
        this.tourId = tourId;
    }

    public Integer getNumberInTour() {
        return numberInTour;
    }

    public void setNumberInTour(Integer numberInTour) {
        this.numberInTour = numberInTour;
    }

    public static enum Status {
        NEW, PUBLISH_READY, PUBLISHED, DELETED
    }
}
