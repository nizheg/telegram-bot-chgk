package me.nizheg.telegram.bot.chgk.work;

import lombok.Builder;
import lombok.Data;
import lombok.NonNull;

@Data
@Builder
public class BroadcastMessage {

    private final long id;
    @NonNull
    private final String data;
    @NonNull
    private final String type;
    private final long chatId;
    @NonNull
    private final String status;
}
