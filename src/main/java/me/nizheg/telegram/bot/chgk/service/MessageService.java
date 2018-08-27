package me.nizheg.telegram.bot.chgk.service;

import lombok.NonNull;
import me.nizheg.telegram.bot.chgk.dto.BroadcastStatus;
import me.nizheg.telegram.bot.chgk.dto.SendingMessage;
import me.nizheg.telegram.bot.chgk.work.data.ForwardMessageData;

public interface MessageService {

    void setMessageForForwarding(@NonNull ForwardMessageData forwardingMessage);

    void send(@NonNull SendingMessage message);

    void setStatus(long id, BroadcastStatus.Status status);
}
