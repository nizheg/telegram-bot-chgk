package me.nizheg.telegram.bot.chgk.service;

import java.util.List;

import me.nizheg.telegram.bot.chgk.domain.ChatGame;
import me.nizheg.telegram.bot.chgk.dto.Chat;
import me.nizheg.telegram.bot.chgk.dto.ChatError;

/**

 *
 * @author Nikolay Zhegalin
 */
public interface ChatService {

    Chat getChat(long chatId);

    Chat createOrUpdate(Chat chat);

    List<Long> getActiveChats();

    boolean isChatActive(long chatId);

    void activateChat(long chatId);

    void setTimer(long chatId, int timeoutSeconds);

    void clearTimer(long chatId);

    void deactivateChat(long chatId);

    ChatError storeChatError(ChatError chatError);

    void handleGroupToSuperGroupConverting(Long groupId, Long superGroupId);

    ChatGame getGame(Chat chatId);
}
