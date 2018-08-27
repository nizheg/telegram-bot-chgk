package me.nizheg.telegram.bot.chgk.service.impl;

import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Optional;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import me.nizheg.telegram.bot.chgk.dto.Chat;
import me.nizheg.telegram.bot.chgk.dto.SendingMessage;
import me.nizheg.telegram.bot.chgk.dto.TelegramUser;
import me.nizheg.telegram.bot.chgk.dto.composite.Task;
import me.nizheg.telegram.bot.chgk.service.ChatGameService;
import me.nizheg.telegram.bot.chgk.service.MessageService;
import me.nizheg.telegram.bot.chgk.util.TaskSender;
import me.nizheg.telegram.bot.chgk.work.WorkService;
import me.nizheg.telegram.bot.chgk.work.data.ForwardMessageData;

import static me.nizheg.telegram.bot.chgk.dto.SendingMessage.RECEIVER_ALL;
import static me.nizheg.telegram.bot.chgk.dto.SendingMessage.RECEIVER_ME;

@Service
@RequiredArgsConstructor
public class MessageServiceImpl implements MessageService {

    private final ChatGameService chatGameService;
    private final TaskSender taskSender;
    private final WorkService workService;

    @Override
    public void setMessageForForwarding(ForwardMessageData forwardingMessage) {

    }

    @Override
    public synchronized void send(@NonNull SendingMessage message) {
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
                } else if (message.getForwardMessageData() != null) {
                    workService.forwardMessageToChats(message.getForwardMessageData(), Collections.singletonList(myId));
                } else if (message.getSendMessageData() != null) {
                    workService.sendMessageToChats(message.getSendMessageData(), Collections.singletonList(myId));
                } else {
                    throw new IllegalStateException("Сообщение для отправки не определено");
                }
                break;
            case RECEIVER_ALL:
                if (message.getForwardMessageData() != null) {
                    workService.forwardMessageToActiveChats(message.getForwardMessageData());
                } else if (message.getSendMessageData() != null) {
                    workService.sendMessageToActiveChats(message.getSendMessageData());
                } else {
                    throw new IllegalStateException("Сообщение для отправки не определено");
                }
                break;
            default:
                throw new UnsupportedOperationException("Not supported now");
        }
    }

}
