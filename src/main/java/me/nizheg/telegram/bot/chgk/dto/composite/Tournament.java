package me.nizheg.telegram.bot.chgk.dto.composite;

import me.nizheg.telegram.bot.chgk.dto.LightTour;

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
