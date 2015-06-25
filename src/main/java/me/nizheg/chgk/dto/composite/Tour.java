package me.nizheg.chgk.dto.composite;

import me.nizheg.chgk.dto.LightTask;
import me.nizheg.chgk.dto.LightTour;

import java.util.List;

/**
 * @author Nikolay Zhegalin
 */
public class Tour extends LightTour {

    private static final long serialVersionUID = -8937024345191491732L;
    private LightTour parentTour;
    private List<LightTask> tasks;

    public Tour(LightTour lightTour) {
        super(lightTour);
    }

    public Tour() {
        super();
    }

    public LightTour getParentTour() {
        return parentTour;
    }

    public void setParentTour(LightTour parentTour) {
        this.parentTour = parentTour;
    }

    public List<LightTask> getTasks() {
        return tasks;
    }

    public void setTasks(List<LightTask> tasks) {
        this.tasks = tasks;
    }
}
