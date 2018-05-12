package me.nizheg.telegram.bot.chgk.dto;

import java.io.Serializable;

public class Answer implements Serializable {

    private static final long serialVersionUID = -4237104646315282259L;

    private Long id;
    private Long taskId;
    private String text;
    private Type type = Type.APPROXIMATE;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getTaskId() {
        return taskId;
    }

    public void setTaskId(Long taskId) {
        this.taskId = taskId;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public enum Type {
        EXACT, APPROXIMATE, CONTAINS, EXACT_WITH_PUNCTUATION
    }

}
