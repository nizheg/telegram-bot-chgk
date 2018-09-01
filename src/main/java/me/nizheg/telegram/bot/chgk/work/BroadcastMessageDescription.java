package me.nizheg.telegram.bot.chgk.work;

import lombok.Builder;
import lombok.Data;
import lombok.NonNull;

@Data
@Builder
public class BroadcastMessageDescription {

    private final long id;
    @NonNull
    private final String data;
    @NonNull
    private final String type;
}
