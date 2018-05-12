package me.nizheg.telegram.bot.chgk.service;

import me.nizheg.telegram.bot.chgk.dto.BroadcastStatus;

/**

 *
 * @author Nikolay Zhegalin
 */
public interface BroadcastSender {
    BroadcastStatus sendMessage(String message);
}
