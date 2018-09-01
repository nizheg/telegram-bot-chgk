package me.nizheg.telegram.bot.chgk.work;

import java.util.List;

import javax.annotation.Nullable;

import lombok.NonNull;
import me.nizheg.telegram.bot.chgk.dto.PagingParameters;

public interface BroadcastMessageDao {

    BroadcastMessagePackage createBroadcastToActiveChats(String data, String type, String status);

    BroadcastMessagePackage createBroadcastToChats(String data, String type, String status, List<Long> chatIds);

    List<BroadcastMessage> findByStatus(String status, int limit);

    void updateStatusByIdChatIdStatus(long id, long chatId, List<String> fromStatuses, String status);

    void updateStatusByIdStatus(long id, List<String> fromStatuses, String status, @Nullable Integer limit);

    BroadcastMessagePackage getPackage(long id);

    List<BroadcastMessagePackage> getPackages(@NonNull PagingParameters pagingParameters);
}
