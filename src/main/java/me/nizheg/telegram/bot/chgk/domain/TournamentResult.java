package me.nizheg.telegram.bot.chgk.domain;

import java.util.Optional;

import javax.annotation.Nullable;

import lombok.Builder;
import lombok.NonNull;
import lombok.Value;
import me.nizheg.telegram.bot.chgk.dto.composite.Task;
import me.nizheg.telegram.bot.chgk.dto.composite.Tournament;

/**
 * @author Nikolay Zhegalin
 */
@Value
@Builder
public class TournamentResult {

    @NonNull
    private final Tournament tournament;
    private final boolean isCurrentTaskFromTournament;
    @Nullable
    private Task currentTask;

    public Optional<Task> getCurrentTask() {
        return Optional.ofNullable(currentTask);
    }
}
