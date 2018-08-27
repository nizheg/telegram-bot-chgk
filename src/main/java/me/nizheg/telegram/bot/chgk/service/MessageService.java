package me.nizheg.telegram.bot.chgk.service;

import javax.annotation.Nonnull;

import me.nizheg.telegram.bot.chgk.dto.BroadcastStatus;
import me.nizheg.telegram.bot.chgk.dto.SendingMessage;
import me.nizheg.telegram.bot.chgk.work.data.ForwardMessageData;

public interface MessageService {

    void setMessageForForwarding(@Nonnull ForwardMessageData forwardingMessage);

    BroadcastStatus send(SendingMessage message);

    BroadcastStatus setStatus(BroadcastStatus status);

    BroadcastStatus getStatus();
}
