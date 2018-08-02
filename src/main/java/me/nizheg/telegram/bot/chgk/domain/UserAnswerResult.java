package me.nizheg.telegram.bot.chgk.domain;

import java.time.OffsetDateTime;

import me.nizheg.telegram.bot.chgk.dto.composite.Task;

/**
* @author Nikolay Zhegalin
*/
public class UserAnswerResult {
    private boolean isCorrect = false;
    private String exactAnswer = null;
    private Long firstAnsweredUser = null;
    private OffsetDateTime usageTime;
    private Task currentTask;

    public boolean isCorrect() {
        return isCorrect;
    }

    public void setCorrect(boolean isCorrect) {
        this.isCorrect = isCorrect;
    }

    public String getExactAnswer() {
        return exactAnswer;
    }

    public void setExactAnswer(String exactAnswer) {
        this.exactAnswer = exactAnswer;
    }

    public Long getFirstAnsweredUser() {
        return firstAnsweredUser;
    }

    public void setFirstAnsweredUser(Long firstAnsweredUser) {
        this.firstAnsweredUser = firstAnsweredUser;
    }

    public OffsetDateTime getUsageTime() {
        return usageTime;
    }

    public void setUsageTime(OffsetDateTime usageTime) {
        this.usageTime = usageTime;
    }

    public Task getCurrentTask() {
        return currentTask;
    }

    public void setCurrentTask(Task currentTask) {
        this.currentTask = currentTask;
    }
}
