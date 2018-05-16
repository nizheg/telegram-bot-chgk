package me.nizheg.telegram.bot.chgk.dto;

import java.util.Collections;
import java.util.List;

import javax.annotation.Nonnull;

public class PageResult<T> {

    private final List<T> values;
    private final long totalCount;

    public PageResult(@Nonnull List<T> values, long totalCount) {
        this.values = Collections.unmodifiableList(values);
        this.totalCount = totalCount;
    }

    @Nonnull
    public List<T> getValues() {
        return values;
    }

    public long getTotalCount() {
        return totalCount;
    }
}
