package me.nizheg.chgk.service;

import me.nizheg.chgk.dto.BroadcastStatus;

/**
 * //todo add comments
 *
 * @author Nikolay Zhegalin
 */
public interface BroadcastSender {
    BroadcastStatus sendMessage(String message);
}
