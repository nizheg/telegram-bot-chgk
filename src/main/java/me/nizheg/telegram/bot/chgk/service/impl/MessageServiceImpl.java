package me.nizheg.telegram.bot.chgk.service.impl;

import org.springframework.stereotype.Service;
import org.springframework.web.context.request.async.DeferredResult;

import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import me.nizheg.telegram.bot.chgk.dto.Chat;
import me.nizheg.telegram.bot.chgk.dto.PagingParameters;
import me.nizheg.telegram.bot.chgk.dto.SendingMessage;
import me.nizheg.telegram.bot.chgk.dto.SendingMessageReceiverStatus;
import me.nizheg.telegram.bot.chgk.dto.SendingMessageStatus;
import me.nizheg.telegram.bot.chgk.dto.TelegramUser;
import me.nizheg.telegram.bot.chgk.dto.composite.Task;
import me.nizheg.telegram.bot.chgk.service.ChatGameService;
import me.nizheg.telegram.bot.chgk.service.MessageService;
import me.nizheg.telegram.bot.chgk.util.TaskSender;
import me.nizheg.telegram.bot.chgk.work.SendingWorkStatus;
import me.nizheg.telegram.bot.chgk.work.WorkService;
import me.nizheg.telegram.bot.chgk.work.WorkStatus;
import me.nizheg.telegram.bot.chgk.work.data.ForwardMessageData;
import me.nizheg.telegram.bot.chgk.work.data.SendMessageData;
import me.nizheg.telegram.bot.chgk.work.data.SendingWorkData;

import static me.nizheg.telegram.bot.chgk.dto.SendingMessage.RECEIVER_ALL;
import static me.nizheg.telegram.bot.chgk.dto.SendingMessage.RECEIVER_ME;

@Service
@RequiredArgsConstructor
public class MessageServiceImpl implements MessageService {

    private final ChatGameService chatGameService;
    private final TaskSender taskSender;
    private final WorkService workService;
    private DeferredResult<ForwardMessageData> forwardMessageDataWaiting;

    @Override
    public synchronized void setMessageForForwarding(ForwardMessageData forwardingMessage) {
        if (this.forwardMessageDataWaiting != null) {
            this.forwardMessageDataWaiting.setResult(forwardingMessage);
        }
    }

    @Override
    public synchronized DeferredResult<ForwardMessageData> waitMessageForForwarding() {
        if (this.forwardMessageDataWaiting != null) {
            return this.forwardMessageDataWaiting;
        }
        this.forwardMessageDataWaiting = new DeferredResult<>(30000L);
        this.forwardMessageDataWaiting.onTimeout(this::cleanForwardMessageDataWaiting);
        this.forwardMessageDataWaiting.onCompletion(this::cleanForwardMessageDataWaiting);
        this.forwardMessageDataWaiting.onError(error -> cleanForwardMessageDataWaiting());
        return this.forwardMessageDataWaiting;
    }

    private synchronized void cleanForwardMessageDataWaiting() {
        this.forwardMessageDataWaiting = null;
    }

    @Override
    public synchronized SendingMessageStatus send(@NonNull SendingMessage message) {
        String receiver = Optional.ofNullable(message.getReceiver()).orElse(RECEIVER_ME);
        switch (receiver) {
            case RECEIVER_ME:
                Long myId = Optional.ofNullable(message.getSender())
                        .map(TelegramUser::getId)
                        .orElseThrow(() -> new IllegalArgumentException("Sender is not resolved"));
                Long taskId = message.getTaskId();
                if (taskId != null) {
                    Task currentTask = chatGameService.getGame(new Chat(myId, true)).setCurrentTask(taskId);
                    taskSender.sendTaskText(currentTask, myId);
                    return createTaskIdSendingStatus(taskId);
                } else if (message.getForwardMessageData() != null) {
                    return convertToSendingMessageStatus(
                            workService.sendMessageToChats(message.getForwardMessageData(),
                                    Collections.singletonList(myId)));
                } else if (message.getSendMessageData() != null) {
                    return convertToSendingMessageStatus(
                            workService.sendMessageToChats(message.getSendMessageData(),
                                    Collections.singletonList(myId)));
                } else {
                    throw new IllegalStateException("Сообщение для отправки не определено");
                }
            case RECEIVER_ALL:
                if (message.getForwardMessageData() != null) {
                    return convertToSendingMessageStatus(
                            workService.sendMessageToActiveChats(message.getForwardMessageData()));
                } else if (message.getSendMessageData() != null) {
                    return convertToSendingMessageStatus(
                            workService.sendMessageToActiveChats(message.getSendMessageData()));
                } else {
                    throw new IllegalStateException("Сообщение для отправки не определено");
                }
            default:
                throw new UnsupportedOperationException("Not supported now");
        }
    }

    @Override
    public void setStatus(long id, SendingMessageStatus status) {
        status.getStatuses().forEach((sendingMessageReceiverStatus, count) -> {
            if (count == -1) {
                workService.changeStatusForAllReceivers(id, convertStatus(sendingMessageReceiverStatus));
            } else {
                workService.changeStatusForPartOfReceivers(id, convertStatus(sendingMessageReceiverStatus), count);
            }
        });
    }

    @Override
    public SendingMessageStatus getStatus(long id) {
        return convertToSendingMessageStatus(workService.getSendingWorkStatus(id));
    }

    @Override
    public List<SendingMessageStatus> getStatuses(int page) {
        PagingParameters pagingParameters = new PagingParameters(20).pageNumber(page);
        List<SendingWorkStatus> sendingWorkStatuses = workService.getSendingWorkStatuses(pagingParameters);
        return sendingWorkStatuses.stream().map(this::convertToSendingMessageStatus).collect(Collectors.toList());
    }

    private SendingMessageStatus convertToSendingMessageStatus(SendingWorkStatus sendMessageWork) {
        SendingMessageStatus.SendingMessageStatusBuilder resultBuilder = SendingMessageStatus.builder()
                .id(sendMessageWork.getId());
        SendingWorkData data = sendMessageWork.getData();
        if (data instanceof ForwardMessageData) {
            resultBuilder.forwardMessageData((ForwardMessageData) data);
        } else if (data instanceof SendMessageData) {
            resultBuilder.sendMessageData((SendMessageData) data);
        }
        resultBuilder.statuses(sendMessageWork.getStatuses().entrySet().stream()
                .collect(Collectors.toMap(
                        entry -> convertStatus(entry.getKey()),
                        Map.Entry::getValue,
                        (l, r) -> l,
                        () -> new EnumMap<>(SendingMessageReceiverStatus.class))));
        return resultBuilder.build();
    }

    private SendingMessageStatus createTaskIdSendingStatus(Long taskId) {
        return SendingMessageStatus.builder()
                .id(0)
                .taskId(taskId)
                .statuses(Collections.singletonMap(SendingMessageReceiverStatus.FINISHED, 1))
                .build();
    }

    private WorkStatus convertStatus(SendingMessageReceiverStatus status) {
        return WorkStatus.valueOf(status.name());
    }

    private SendingMessageReceiverStatus convertStatus(WorkStatus workStatus) {
        return SendingMessageReceiverStatus.valueOf(workStatus.name());
    }

}
