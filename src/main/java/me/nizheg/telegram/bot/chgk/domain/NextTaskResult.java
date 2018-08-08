package me.nizheg.telegram.bot.chgk.domain;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.annotation.Nullable;

import lombok.Builder;
import lombok.NonNull;
import lombok.Value;
import me.nizheg.telegram.bot.chgk.dto.composite.StatEntry;
import me.nizheg.telegram.bot.chgk.dto.composite.Task;
import me.nizheg.telegram.bot.chgk.dto.composite.Tournament;

/**
 * @author Nikolay Zhegalin
 */
@Value
@Builder
public class NextTaskResult {

    public final static String STAT_USERS = "users";
    @Nullable
    private final Task unansweredTask;
    @Nullable
    private final Task nextTask;
    private final boolean isTournament;
    @Nullable
    private final Tournament tournament;
    @Builder.Default
    @NonNull
    private final List<StatEntry> tournamentStat = new ArrayList<>();

    public Optional<Task> getUnansweredTask() {
        return Optional.ofNullable(unansweredTask);
    }

    public Optional<Task> getNextTask() {
        return Optional.ofNullable(nextTask);
    }

    public Optional<Tournament> getTournament() {
        return Optional.ofNullable(tournament);
    }

    public boolean isTournament() {
        return isTournament;
    }
}
