package me.nizheg.telegram.bot.chgk.work.data;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Getter;
import lombok.Setter;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@Setter
public class ForwardMessageData {

    private long messageId;
    private long fromChatId;
    private Boolean disableNotification;
    private String text;
}
