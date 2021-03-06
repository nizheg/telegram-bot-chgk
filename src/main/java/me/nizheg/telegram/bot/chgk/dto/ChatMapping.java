package me.nizheg.telegram.bot.chgk.dto;

import java.io.Serializable;

/**
 * @author Nikolay Zhegalin
 */
public class ChatMapping implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long groupId;
    private Long superGroupId;

    public Long getGroupId() {
        return groupId;
    }

    public void setGroupId(Long groupId) {
        this.groupId = groupId;
    }

    public Long getSuperGroupId() {
        return superGroupId;
    }

    public void setSuperGroupId(Long superGroupId) {
        this.superGroupId = superGroupId;
    }
}
