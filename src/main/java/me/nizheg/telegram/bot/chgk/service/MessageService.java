package me.nizheg.telegram.bot.chgk.service;

import javax.annotation.Nonnull;

import me.nizheg.telegram.bot.chgk.dto.BroadcastStatus;
import me.nizheg.telegram.bot.chgk.dto.ForwardingMessage;
import me.nizheg.telegram.bot.chgk.dto.SendingMessage;

public interface MessageService {

    BroadcastStatus setMessageForForwarding(@Nonnull ForwardingMessage forwardingMessage);

    BroadcastStatus send(SendingMessage message);

    BroadcastStatus setStatus(BroadcastStatus status);

    BroadcastStatus getStatus();
}
