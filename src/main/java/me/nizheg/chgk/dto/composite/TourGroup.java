package me.nizheg.chgk.dto.composite;

import me.nizheg.chgk.dto.LightTour;

import java.util.List;

/**
 * @author Nikolay Zhegalin
 */
public class TourGroup extends LightTour {

    private static final long serialVersionUID = -1506685482440150323L;
    private LightTour parentTour;
    private List<LightTour> childTours;

    public TourGroup() {
    }

    public TourGroup(LightTour lightTour) {
        super(lightTour);
    }

    public LightTour getParentTour() {
        return parentTour;
    }

    public void setParentTour(LightTour parentTour) {
        this.parentTour = parentTour;
    }

    public List<LightTour> getChildTours() {
        return childTours;
    }

    public void setChildTours(List<LightTour> childTours) {
        this.childTours = childTours;
    }
}
