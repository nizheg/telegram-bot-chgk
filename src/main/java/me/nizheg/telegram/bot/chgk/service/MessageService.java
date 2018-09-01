package me.nizheg.telegram.bot.chgk.service;

import java.util.List;

import lombok.NonNull;
import me.nizheg.telegram.bot.chgk.dto.SendingMessage;
import me.nizheg.telegram.bot.chgk.dto.SendingMessageStatus;
import me.nizheg.telegram.bot.chgk.work.data.ForwardMessageData;

public interface MessageService {

    void setMessageForForwarding(@NonNull ForwardMessageData forwardingMessage);

    SendingMessageStatus send(@NonNull SendingMessage message);

    void setStatus(long id, SendingMessageStatus status);

    SendingMessageStatus getStatus(long id);

    List<SendingMessageStatus> getStatuses(int page);
}
