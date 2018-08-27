package me.nizheg.telegram.bot.chgk.work;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

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
        String data;
        try {
            data = objectMapper.writeValueAsString(forwardMessageData);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException(e);
        }
        broadcastMessageDao.createBroadcastToActiveChats(data,
                WorkType.FORWARD_MESSAGE.name(),
                WorkStatus.CREATED.name());

    }

    @Override
    public void sendMessageToActiveChats(SendMessageData sendMessageData) {
        String data;
        try {
            data = objectMapper.writeValueAsString(sendMessageData);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException(e);
        }
        broadcastMessageDao.createBroadcastToActiveChats(data,
                WorkType.SEND_MESSAGE.name(),
                WorkStatus.CREATED.name());
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

    @Override
    public void changeStatus(WorkDescription workDescription, WorkStatus status) {
        if (workDescription instanceof SendMessageWork) {
            SendMessageWork sendMessageWork = (SendMessageWork) workDescription;
            broadcastMessageDao.updateStatus(sendMessageWork.getId(), sendMessageWork.getChatId(), status.toString());
        } else if (workDescription instanceof ForwardMessageWork) {
            ForwardMessageWork forwardMessageWork = (ForwardMessageWork) workDescription;
            broadcastMessageDao.updateStatus(forwardMessageWork.getId(), forwardMessageWork.getChatId(),
                    status.toString());
        } else if (!(workDescription instanceof NoWork)) {
            throw new UnsupportedOperationException(workDescription.getClass().toString());
        }
    }

    private enum WorkType {
        SEND_MESSAGE,
        FORWARD_MESSAGE
    }

    private static class NoWork implements WorkDescription {

    }
}
