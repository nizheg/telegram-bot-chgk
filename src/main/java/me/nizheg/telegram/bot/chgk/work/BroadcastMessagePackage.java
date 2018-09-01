package me.nizheg.telegram.bot.chgk.work;

import java.util.Collections;
import java.util.Map;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class BroadcastMessagePackage {
    @NonNull
    private final BroadcastMessageDescription description;
    @NonNull
    private final Map<String, Integer> statusesCount;

    public long getId() {
        return description.getId();
    }

    public String getData() {
        return description.getData();
    }

    public String getType() {
        return description.getType();
    }

    public Map<String, Integer> getStatusesCount() {
        return Collections.unmodifiableMap(statusesCount);
    }
}
