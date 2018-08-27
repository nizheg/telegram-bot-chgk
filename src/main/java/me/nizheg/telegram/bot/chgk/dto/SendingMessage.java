package me.nizheg.telegram.bot.chgk.dto;

import javax.annotation.Nullable;

import lombok.Data;
import me.nizheg.telegram.bot.chgk.work.data.ForwardMessageData;
import me.nizheg.telegram.bot.chgk.work.data.SendMessageData;

@Data
public class SendingMessage {

    public static final String RECEIVER_ALL = "all";
    public static final String RECEIVER_ME = "me";

    @Nullable
    private TelegramUser sender;
    @Nullable
    private String receiver;
    @Nullable
    private ForwardMessageData forwardMessageData;
    @Nullable
    private SendMessageData sendMessageData;
    @Nullable
    private Long taskId;
}
