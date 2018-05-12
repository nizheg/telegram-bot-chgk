package me.nizheg.telegram.bot.chgk.repository;

import me.nizheg.telegram.bot.chgk.dto.ChatError;

/**

 *
 * @author Nikolay Zhegalin
 */
public interface ChatErrorDao {
    ChatError create(ChatError chatError);
}
