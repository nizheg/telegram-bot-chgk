package me.nizheg.telegram.bot.chgk.domain;

import java.util.Optional;

import javax.annotation.Nullable;

import lombok.Builder;
import lombok.Value;
import me.nizheg.telegram.bot.chgk.dto.composite.Task;

/**
 * @author Nikolay Zhegalin
 */
@Value
@Builder
public class HintResult {

    @Nullable
    private final Task task;
    private final boolean isTaskCurrent;

    public Optional<Task> getTask() {
        return Optional.ofNullable(task);
    }

}
