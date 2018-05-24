package me.nizheg.telegram.bot.chgk.service;

import javax.annotation.Nonnull;

import me.nizheg.telegram.bot.chgk.dto.BroadcastStatus;
import me.nizheg.telegram.bot.chgk.dto.ForwardingMessage;
import me.nizheg.telegram.bot.chgk.dto.SendingMessage;
import me.nizheg.telegram.bot.chgk.dto.TelegramUser;

public interface MessageService {

    BroadcastStatus setMessageForForwarding(@Nonnull ForwardingMessage forwardingMessage);

    BroadcastStatus send(SendingMessage message, TelegramUser me);

    BroadcastStatus setStatus(BroadcastStatus status);

    BroadcastStatus getStatus();
}
