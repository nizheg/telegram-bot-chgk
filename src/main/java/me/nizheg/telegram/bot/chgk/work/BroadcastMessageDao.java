package me.nizheg.telegram.bot.chgk.work;

import java.util.List;

public interface BroadcastMessageDao {

    void createBroadcastToActiveChats(String data, String type, String status);

    List<BroadcastMessage> findByStatus(String status, int limit);

    void updateStatus(long id, long chatId, String status);
}
