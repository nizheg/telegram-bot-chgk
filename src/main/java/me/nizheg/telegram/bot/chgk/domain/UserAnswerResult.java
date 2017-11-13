package me.nizheg.telegram.bot.chgk.domain;

import me.nizheg.telegram.bot.chgk.dto.composite.Task;

import java.util.Date;

/**
* @author Nikolay Zhegalin
*/
public class UserAnswerResult {
    private boolean isCorrect = false;
    private String exactAnswer = null;
    private Long firstAnsweredUser = null;
    private Date usageTime;
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

    public Date getUsageTime() {
        return usageTime;
    }

    public void setUsageTime(Date usageTime) {
        this.usageTime = usageTime;
    }

    public Task getCurrentTask() {
        return currentTask;
    }

    public void setCurrentTask(Task currentTask) {
        this.currentTask = currentTask;
    }
}
