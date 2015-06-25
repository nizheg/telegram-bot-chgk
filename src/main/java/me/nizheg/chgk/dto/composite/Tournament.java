package me.nizheg.chgk.dto.composite;

import me.nizheg.chgk.dto.LightTour;
import me.nizheg.chgk.dto.composite.TourGroup;

/**
 * @author Nikolay Zhegalin
 */
public class Tournament extends TourGroup {

    private static final long serialVersionUID = 4806575698712033976L;

    public Tournament() {
    }

    public Tournament(LightTour lightTour) {
        super(lightTour);
    }
}
