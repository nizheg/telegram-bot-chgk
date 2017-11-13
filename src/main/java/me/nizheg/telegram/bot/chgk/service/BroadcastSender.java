package me.nizheg.telegram.bot.chgk.service;

import me.nizheg.telegram.bot.chgk.dto.BroadcastStatus;

/**
 * //todo add comments
 *
 * @author Nikolay Zhegalin
 */
public interface BroadcastSender {
    BroadcastStatus sendMessage(String message);
}
