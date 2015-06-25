package me.nizheg.chgk.dto.composite;

import me.nizheg.chgk.dto.LightTour;

import java.io.Serializable;

/**
 * @author Nikolay Zhegalin
 */
public class LightTourWithStat implements Serializable {

    private static final long serialVersionUID = -1674590413000778231L;
    private LightTour lightTour;
    private int tasksCount;
    private int donePercent;

    public LightTourWithStat(LightTour lightTour) {
        this.lightTour = lightTour;
    }

    public LightTourWithStat() {
    }

    public LightTour getLightTour() {
        return lightTour;
    }

    public void setLightTour(LightTour lightTour) {
        this.lightTour = lightTour;
    }

    public int getTasksCount() {
        return tasksCount;
    }

    public void setTasksCount(int tasksCount) {
        this.tasksCount = tasksCount;
    }

    public int getDonePercent() {
        return donePercent;
    }

    public void setDonePercent(int donePercent) {
        this.donePercent = donePercent;
    }
}
