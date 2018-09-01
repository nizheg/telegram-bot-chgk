package me.nizheg.telegram.bot.chgk.work;

import java.util.List;

import lombok.NonNull;
import me.nizheg.telegram.bot.chgk.dto.PagingParameters;
import me.nizheg.telegram.bot.chgk.work.data.ForwardMessageData;
import me.nizheg.telegram.bot.chgk.work.data.SendMessageData;

public interface WorkService {

    SendingWorkStatus sendMessageToActiveChats(ForwardMessageData forwardMessageData);

    SendingWorkStatus sendMessageToActiveChats(SendMessageData sendMessageData);

    SendingWorkStatus sendMessageToChats(ForwardMessageData forwardMessageData, List<Long> receivers);

    SendingWorkStatus sendMessageToChats(SendMessageData sendMessageData, List<Long> receivers);

    void changeStatusForAllReceivers(long broadcastId, WorkStatus status);

    List<WorkDescription> getWorks(int count, WorkStatus status);

    void changeStatus(WorkDescription workDescription, WorkStatus status);

    void changeStatusForPartOfReceivers(long broadcastId, WorkStatus status, int count);

    SendingWorkStatus getSendingWorkStatus(long sendingWorkId);

    List<SendingWorkStatus> getSendingWorkStatuses(@NonNull PagingParameters pagingParameters);
}
