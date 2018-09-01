package me.nizheg.telegram.bot.chgk.work;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.apachecommons.CommonsLog;
import me.nizheg.telegram.bot.chgk.dto.PagingParameters;
import me.nizheg.telegram.bot.chgk.work.data.ForwardMessageData;
import me.nizheg.telegram.bot.chgk.work.data.SendMessageData;
import me.nizheg.telegram.bot.chgk.work.data.SendingWorkData;

@CommonsLog
@RequiredArgsConstructor
public class WorkServiceImpl implements WorkService {

    private final BroadcastMessageDao broadcastMessageDao;
    private final ObjectMapper objectMapper;

    @Override
    public SendingWorkStatus sendMessageToActiveChats(ForwardMessageData forwardMessageData) {
        String data = serializeData(forwardMessageData);
        BroadcastMessagePackage broadcastToActiveChats = broadcastMessageDao.createBroadcastToActiveChats(data,
                WorkType.FORWARD_MESSAGE.name(),
                WorkStatus.CREATED.name());
        return convertToSendWorkingStatus(broadcastToActiveChats);
    }

    @Override
    public SendingWorkStatus sendMessageToActiveChats(SendMessageData sendMessageData) {
        String data = serializeData(sendMessageData);
        BroadcastMessagePackage broadcastToActiveChats = broadcastMessageDao.createBroadcastToActiveChats(data,
                WorkType.SEND_MESSAGE.name(),
                WorkStatus.CREATED.name());
        return convertToSendWorkingStatus(broadcastToActiveChats);
    }

    @Override
    public SendingWorkStatus sendMessageToChats(ForwardMessageData forwardMessageData, List<Long> receivers) {
        String data = serializeData(forwardMessageData);
        BroadcastMessagePackage broadcastToChats = broadcastMessageDao.createBroadcastToChats(data,
                WorkType.FORWARD_MESSAGE.name(),
                WorkStatus.CREATED.name(),
                receivers);
        return convertToSendWorkingStatus(broadcastToChats);
    }

    @Override
    public SendingWorkStatus sendMessageToChats(SendMessageData sendMessageData, List<Long> receivers) {
        String data = serializeData(sendMessageData);
        BroadcastMessagePackage broadcastToChats = broadcastMessageDao.createBroadcastToChats(data,
                WorkType.SEND_MESSAGE.name(),
                WorkStatus.CREATED.name(),
                receivers
        );
        return convertToSendWorkingStatus(broadcastToChats);
    }

    @Override
    public List<WorkDescription> getWorks(int count, @NonNull WorkStatus status) {
        List<BroadcastMessage> broadcastMessages = broadcastMessageDao.findByStatus(status.name(), count);
        return broadcastMessages.stream().map(broadcastMessage -> {
            try {
                WorkType workType = WorkType.valueOf(broadcastMessage.getType());
                switch (workType) {
                    case SEND_MESSAGE:
                        return createSendMessageData(broadcastMessage);
                    case FORWARD_MESSAGE:
                        return createForwardMessageData(broadcastMessage);
                    default:
                        return new NoWork();
                }
            } catch (Exception ex) {
                log.error("Unable to create work", ex);
                return new NoWork();
            }
        }).collect(Collectors.toList());
    }

    /**
     * Change status of workDescription. Matrix of possible transitions:<br>
     * created: -> ready, -------, -----, ---------, --------<br>
     * ready:   -> -----, started, -----, cancelled, --------<br>
     * started: -> -----, -------, error, ---------, finished<br>
     * error:   -> ready, -------, -----, ---------, --------<br>
     * cancelled:> ready, -------, -----, ---------, --------<br>
     * finished:-> ready, -------, -----, ---------, --------<br>
     *
     * @param workDescription - which status is changing
     * @param toStatus - new status
     */
    @Override
    public void changeStatus(WorkDescription workDescription, WorkStatus toStatus) {
        List<String> fromStatusesStrings = getFromStatusesForTransition(toStatus);

        if (workDescription instanceof SendMessageWork) {
            SendMessageWork sendMessageWork = (SendMessageWork) workDescription;
            broadcastMessageDao.updateStatusByIdChatIdStatus(sendMessageWork.getId(),
                    sendMessageWork.getChatId(), fromStatusesStrings, toStatus.toString());
        } else if (workDescription instanceof ForwardMessageWork) {
            ForwardMessageWork forwardMessageWork = (ForwardMessageWork) workDescription;
            broadcastMessageDao.updateStatusByIdChatIdStatus(forwardMessageWork.getId(),
                    forwardMessageWork.getChatId(), fromStatusesStrings, toStatus.toString());
        } else if (!(workDescription instanceof NoWork)) {
            throw new UnsupportedOperationException(workDescription.getClass().toString());
        }
    }

    @Override
    public void changeStatusForAllReceivers(long sendingWorkId, WorkStatus status) {
        List<String> fromStatusesStrings = getFromStatusesForTransition(status);
        broadcastMessageDao.updateStatusByIdStatus(sendingWorkId, fromStatusesStrings, status.name(), null);
    }

    @Override
    public void changeStatusForPartOfReceivers(long sendingWorkId, WorkStatus status, int count) {
        List<String> fromStatusesStrings = getFromStatusesForTransition(status);
        broadcastMessageDao.updateStatusByIdStatus(sendingWorkId, fromStatusesStrings, status.name(), count);
    }

    @Override
    public SendingWorkStatus getSendingWorkStatus(long sendingWorkId) {
        return convertToSendWorkingStatus(broadcastMessageDao.getPackage(sendingWorkId));
    }

    @Override
    public List<SendingWorkStatus> getSendingWorkStatuses(@NonNull PagingParameters pagingParameters) {
        return broadcastMessageDao.getPackages(pagingParameters)
                .stream()
                .map(this::convertToSendWorkingStatus)
                .collect(Collectors.toList());
    }

    private ForwardMessageWork createForwardMessageData(BroadcastMessage broadcastMessage) {
        ForwardMessageData forwardMessageData = deserializeForwardMessageData(broadcastMessage.getData());
        return ForwardMessageWork.builder()
                .id(broadcastMessage.getId())
                .chatId(broadcastMessage.getChatId())
                .forwardMessageData(forwardMessageData)
                .build();
    }

    private SendMessageWork createSendMessageData(BroadcastMessage broadcastMessage) {
        SendMessageData sendMessageData = deserializeSendMessageData(broadcastMessage.getData());
        return SendMessageWork.builder()
                .id(broadcastMessage.getId())
                .chatId(broadcastMessage.getChatId())
                .sendMessageData(sendMessageData)
                .build();
    }

    private List<String> getFromStatusesForTransition(WorkStatus toStatus) {
        List<WorkStatus> fromStatuses = Collections.emptyList();
        switch (toStatus) {
            case CREATED:
                break;
            case READY:
                fromStatuses = Arrays.asList(WorkStatus.CREATED, WorkStatus.ERROR, WorkStatus.CANCELLED,
                        WorkStatus.FINISHED);
                break;
            case STARTED:
                fromStatuses = Collections.singletonList(WorkStatus.READY);
                break;
            case ERROR:
                fromStatuses = Collections.singletonList(WorkStatus.STARTED);
                break;
            case CANCELLED:
                fromStatuses = Collections.singletonList(WorkStatus.READY);
                break;
            case FINISHED:
                fromStatuses = Collections.singletonList(WorkStatus.STARTED);
                break;
            default:
        }
        return fromStatuses.stream().map(Enum::name).collect(Collectors.toList());
    }


    private SendingWorkStatus convertToSendWorkingStatus(BroadcastMessagePackage broadcastToActiveChats) {
        Map<WorkStatus, Integer> workStatuses = broadcastToActiveChats.getStatusesCount().entrySet().stream()
                .collect(Collectors.toMap(entry -> WorkStatus.valueOf(entry.getKey()), Map.Entry::getValue));
        return SendingWorkStatus.builder()
                .id(broadcastToActiveChats.getId())
                .data(deserializeSendingWorkData(broadcastToActiveChats.getData(), broadcastToActiveChats.getType()))
                .statuses(workStatuses).build();
    }

    private String serializeData(ForwardMessageData forwardMessageData) {
        String data;
        try {
            data = objectMapper.writeValueAsString(forwardMessageData);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException(e);
        }
        return data;
    }

    private String serializeData(SendMessageData sendMessageData) {
        String data;
        try {
            data = objectMapper.writeValueAsString(sendMessageData);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException(e);
        }
        return data;
    }

    @Nullable
    private SendingWorkData deserializeSendingWorkData(@Nonnull String data, @Nonnull String type) {
        WorkType workType = WorkType.valueOf(type);
        switch (workType) {
            case SEND_MESSAGE:
                return deserializeSendMessageData(data);
            case FORWARD_MESSAGE:
                return deserializeForwardMessageData(data);
            default:
                return null;
        }
    }

    private SendMessageData deserializeSendMessageData(String data) {
        try {
            return objectMapper.readerFor(SendMessageData.class).readValue(data);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    private ForwardMessageData deserializeForwardMessageData(String data) {
        try {
            return objectMapper.readerFor(ForwardMessageData.class).readValue(data);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    private enum WorkType {
        SEND_MESSAGE,
        FORWARD_MESSAGE
    }

    private static class NoWork implements WorkDescription {
    }
}
