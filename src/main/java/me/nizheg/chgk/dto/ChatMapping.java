package me.nizheg.chgk.dto;

import java.io.Serializable;

/**
 * //todo add comments
 *
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
