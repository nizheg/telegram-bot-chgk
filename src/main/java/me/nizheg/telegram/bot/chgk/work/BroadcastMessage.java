package me.nizheg.telegram.bot.chgk.work;

import lombok.Builder;
import lombok.Data;
import lombok.NonNull;

@Data
@Builder
public class BroadcastMessage {

    @NonNull
    private final BroadcastMessageDescription description;
    private final long chatId;
    @NonNull
    private final String status;

    public long getId() {
        return description.getId();
    }

    public String getData() {
        return description.getData();
    }

    public String getType() {
        return description.getType();
    }

}
