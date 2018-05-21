package me.nizheg.telegram.bot.chgk.service;

import me.nizheg.telegram.bot.api.service.param.ForwardingMessage;
import me.nizheg.telegram.bot.api.service.param.Message;
import me.nizheg.telegram.bot.chgk.dto.BroadcastStatus;

/**
 * @author Nikolay Zhegalin
 */
public interface BroadcastSender {

    BroadcastStatus sendMessage(Message message);

    BroadcastStatus forwardMessage(ForwardingMessage forwardingMessage, String description);
}
