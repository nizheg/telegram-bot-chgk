package me.nizheg.chgk.dto;

import java.io.Serializable;

/**
 * @author Nikolay Zhegalin
 */
public class TaskRating implements Serializable {

    private static final long serialVersionUID = 1089195676958110069L;
    private Long taskId;
    private Long likesCount;
    private Long dislikesCount;

    public Long getTaskId() {
        return taskId;
    }

    public void setTaskId(Long taskId) {
        this.taskId = taskId;
    }

    public Long getLikesCount() {
        return likesCount;
    }

    public void setLikesCount(Long likesCount) {
        this.likesCount = likesCount;
    }

    public Long getDislikesCount() {
        return dislikesCount;
    }

    public void setDislikesCount(Long dislikesCount) {
        this.dislikesCount = dislikesCount;
    }
}
