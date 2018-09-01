package me.nizheg.telegram.bot.chgk.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.Map;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;

import lombok.Builder;
import lombok.Data;
import me.nizheg.telegram.bot.chgk.work.data.ForwardMessageData;
import me.nizheg.telegram.bot.chgk.work.data.SendMessageData;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
@Builder
public class SendingMessageStatus {

    private long id;
    @NotNull
    private Map<SendingMessageReceiverStatus, Integer> statuses;
    @Nullable
    private ForwardMessageData forwardMessageData;
    @Nullable
    private SendMessageData sendMessageData;
    @Nullable
    private Long taskId;

}
