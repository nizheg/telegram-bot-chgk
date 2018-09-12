package me.nizheg.telegram.bot.chgk.domain;

import java.time.OffsetDateTime;
import java.util.Optional;

import javax.annotation.Nullable;

import lombok.Builder;
import lombok.NonNull;
import lombok.Value;
import me.nizheg.telegram.bot.chgk.dto.composite.Task;

/**
 * @author Nikolay Zhegalin
 */
@Value
@Builder
public class UserAnswerResult {

    private final boolean isCorrect;
    @Nullable
    private String exactAnswer;
    @Nullable
    private Long firstAnsweredUser;
    @Nullable
    private final OffsetDateTime usageTime;
    @NonNull
    private final Task currentTask;

    public Optional<String> getExactAnswer() {
        return Optional.ofNullable(exactAnswer);
    }

    public Optional<Long> getFirstAnsweredUser() {
        return Optional.ofNullable(firstAnsweredUser);
    }
}
