package me.nizheg.chgk.dto;

import java.io.Serializable;
import java.util.Date;

/**
 * @author Nikolay Zhegalin
 */
public class LightTour implements Serializable {

    public static final long ROOT_ID = 0;
    private static final long serialVersionUID = 1818750921661320995L;
    private Long id;
    private Long parentTourId;
    private String title;
    private Integer number;
    private Date playedAt;
    private Status status = Status.NEW;
    private Type type;

    public LightTour() {
    }

    public LightTour(LightTour lightTour) {
        setId(lightTour.getId());
        setParentTourId(lightTour.getParentTourId());
        setTitle(lightTour.getTitle());
        setStatus(lightTour.getStatus());
        setType(lightTour.getType());
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getParentTourId() {
        return parentTourId;
    }

    public void setParentTourId(Long parentTourId) {
        this.parentTourId = parentTourId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Integer getNumber() {
        return number;
    }

    public void setNumber(Integer number) {
        this.number = number;
    }

    public Date getPlayedAt() {
        return playedAt;
    }

    public void setPlayedAt(Date playedAt) {
        this.playedAt = playedAt;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public static enum Status {
        NEW, PUBLISHED, DELETED
    }

    public static enum Type {
        TOUR, TOURNAMENT, TOURNAMENT_GROUP
    }
}
