package me.nizheg.telegram.bot.chgk.work;

import javax.annotation.Nullable;

import lombok.Builder;
import lombok.Data;
import lombok.NonNull;
import me.nizheg.telegram.bot.chgk.work.data.ForwardMessageData;

@Data
@Builder
public class ForwardMessageWork implements WorkDescription {

    private final long id;
    private final long chatId;
    @NonNull
    private ForwardMessageData forwardMessageData;

    public long getMessageId() {
        return forwardMessageData.getMessageId();
    }

    public long getFromChatId() {
        return forwardMessageData.getFromChatId();
    }

    @Nullable
    public Boolean getDisableNotification() {
        return forwardMessageData.getDisableNotification();
    }
}
