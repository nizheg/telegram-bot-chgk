package me.nizheg.telegram.bot.chgk.dto;

import java.util.Map;

import javax.annotation.Nullable;

import lombok.Builder;
import lombok.Getter;
import me.nizheg.telegram.bot.chgk.work.data.ForwardMessageData;
import me.nizheg.telegram.bot.chgk.work.data.SendMessageData;

@Builder
@Getter
public class SendingMessageStatus {

    private final long id;
    private final Map<SendingMessageReceiverStatus, Integer> statuses;
    @Nullable
    private ForwardMessageData forwardMessageData;
    @Nullable
    private SendMessageData sendMessageData;
    @Nullable
    private Long taskId;

}
