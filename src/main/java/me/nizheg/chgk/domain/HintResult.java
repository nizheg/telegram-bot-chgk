package me.nizheg.chgk.domain;

import me.nizheg.chgk.dto.composite.Task;

/**
 * @author Nikolay Zhegalin
 */
public class HintResult {
    private Task task;
    private boolean isTaskCurrent = true;

    public Task getTask() {
        return task;
    }

    public void setTask(Task task) {
        this.task = task;
    }

    public boolean isTaskCurrent() {
        return isTaskCurrent;
    }

    public void setTaskCurrent(boolean isTaskCurrent) {
        this.isTaskCurrent = isTaskCurrent;
    }
}
