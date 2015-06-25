package me.nizheg.chgk.domain;

import java.util.ArrayList;
import java.util.List;

import me.nizheg.chgk.dto.composite.StatEntry;
import me.nizheg.chgk.dto.composite.Task;
import me.nizheg.chgk.dto.composite.Tournament;

/**
 * @author Nikolay Zhegalin
 */
public class NextTaskResult {

    public final static String STAT_USERS = "users";
    private Task unansweredTask;
    private Task nextTask;
    private boolean isTournament;
    private Tournament tournament;
    private List<StatEntry> tournamentStat = new ArrayList<StatEntry>();

    public Task getUnansweredTask() {
        return unansweredTask;
    }

    public void setUnansweredTask(Task unansweredTask) {
        this.unansweredTask = unansweredTask;
    }

    public Task getNextTask() {
        return nextTask;
    }

    public void setNextTask(Task nextTask) {
        this.nextTask = nextTask;
    }

    public boolean isTournament() {
        return isTournament;
    }

    public void setTournament(boolean isTournament) {
        this.isTournament = isTournament;
    }

    public Tournament getTournament() {
        return tournament;
    }

    public void setTournament(Tournament tournament) {
        this.tournament = tournament;
    }

    public List<StatEntry> getTournamentStat() {
        return tournamentStat;
    }

    public void setTournamentStat(List<StatEntry> tournamentStat) {
        this.tournamentStat = tournamentStat;
    }
}
