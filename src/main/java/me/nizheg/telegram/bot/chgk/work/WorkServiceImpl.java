package me.nizheg.telegram.bot.chgk.work;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.apachecommons.CommonsLog;
import me.nizheg.telegram.bot.chgk.work.data.ForwardMessageData;
import me.nizheg.telegram.bot.chgk.work.data.SendMessageData;

@CommonsLog
@RequiredArgsConstructor
public class WorkServiceImpl implements WorkService {

    private final BroadcastMessageDao broadcastMessageDao;
    private final ObjectMapper objectMapper;

    @Override
    public void forwardMessageToActiveChats(ForwardMessageData forwardMessageData) {
        String data = serializeData(forwardMessageData);
        broadcastMessageDao.createBroadcastToActiveChats(data,
                WorkType.FORWARD_MESSAGE.name(),
                WorkStatus.CREATED.name());

    }

    @Override
    public void forwardMessageToChats(ForwardMessageData forwardMessageData, List<Long> receivers) {
        String data = serializeData(forwardMessageData);
        broadcastMessageDao.createBroadcastToChats(data,
                WorkType.FORWARD_MESSAGE.name(),
                WorkStatus.CREATED.name(),
                receivers);
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

    @Override
    public void sendMessageToActiveChats(SendMessageData sendMessageData) {
        String data = serializeData(sendMessageData);
        broadcastMessageDao.createBroadcastToActiveChats(data,
                WorkType.SEND_MESSAGE.name(),
                WorkStatus.CREATED.name());
    }

    @Override
    public void sendMessageToChats(SendMessageData sendMessageData, List<Long> receivers) {
        String data = serializeData(sendMessageData);
        broadcastMessageDao.createBroadcastToChats(data,
                WorkType.SEND_MESSAGE.name(),
                WorkStatus.CREATED.name(),
                receivers
        );
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

    private ForwardMessageWork createForwardMessageData(BroadcastMessage broadcastMessage) throws java.io.IOException {
        ForwardMessageData forwardMessageData =
                objectMapper.readerFor(ForwardMessageData.class).readValue(broadcastMessage.getData());
        return ForwardMessageWork.builder()
                .id(broadcastMessage.getId())
                .chatId(broadcastMessage.getChatId())
                .forwardMessageData(forwardMessageData)
                .build();
    }

    private SendMessageWork createSendMessageData(BroadcastMessage broadcastMessage) throws java.io.IOException {
        SendMessageData sendMessageData =
                objectMapper.readerFor(SendMessageData.class).readValue(broadcastMessage.getData());
        return SendMessageWork.builder()
                .id(broadcastMessage.getId())
                .chatId(broadcastMessage.getChatId())
                .sendMessageData(sendMessageData)
                .build();
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
    public void changeStatusForAllChats(long broadcastId, WorkStatus status) {
        List<String> fromStatusesStrings = getFromStatusesForTransition(WorkStatus.CANCELLED);
        broadcastMessageDao.updateStatusByIdStatus(broadcastId, fromStatusesStrings, WorkStatus.CANCELLED.name());
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

    private enum WorkType {
        SEND_MESSAGE,
        FORWARD_MESSAGE
    }

    private static class NoWork implements WorkDescription {
    }
}
