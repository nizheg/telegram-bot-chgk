package me.nizheg.telegram.bot.chgk.service;

import lombok.NonNull;
import me.nizheg.telegram.bot.chgk.dto.SendingMessage;
import me.nizheg.telegram.bot.chgk.dto.SendingMessageReceiverStatus;
import me.nizheg.telegram.bot.chgk.dto.SendingMessageStatus;
import me.nizheg.telegram.bot.chgk.work.data.ForwardMessageData;

public interface MessageService {

    void setMessageForForwarding(@NonNull ForwardMessageData forwardingMessage);

    SendingMessageStatus send(@NonNull SendingMessage message);

    void setStatus(long id, SendingMessageReceiverStatus status);

    SendingMessageStatus getStatus(long id);
}
