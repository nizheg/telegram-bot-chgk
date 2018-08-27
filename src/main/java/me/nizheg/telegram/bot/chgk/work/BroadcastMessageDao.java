package me.nizheg.telegram.bot.chgk.work;

import java.util.List;

public interface BroadcastMessageDao {

    void createBroadcastToActiveChats(String data, String type, String status);

    void createBroadcastToChats(String data, String type, String status, List<Long> chatIds);

    List<BroadcastMessage> findByStatus(String status, int limit);

    void updateStatusByIdChatIdStatus(long id, long chatId, List<String> fromStatuses, String status);

    void updateStatusByIdStatus(long id, List<String> fromStatuses, String status);
}
