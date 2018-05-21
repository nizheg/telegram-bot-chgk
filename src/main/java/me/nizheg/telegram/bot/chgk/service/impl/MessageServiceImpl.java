package me.nizheg.telegram.bot.chgk.service.impl;

import org.springframework.stereotype.Service;

import java.util.Optional;

import me.nizheg.telegram.bot.api.model.ParseMode;
import me.nizheg.telegram.bot.api.service.TelegramApiClient;
import me.nizheg.telegram.bot.api.service.param.ChatId;
import me.nizheg.telegram.bot.chgk.dto.BroadcastStatus;
import me.nizheg.telegram.bot.chgk.dto.Chat;
import me.nizheg.telegram.bot.chgk.dto.ForwardingMessage;
import me.nizheg.telegram.bot.chgk.dto.SendingMessage;
import me.nizheg.telegram.bot.chgk.dto.TelegramUser;
import me.nizheg.telegram.bot.chgk.dto.composite.Task;
import me.nizheg.telegram.bot.chgk.service.BroadcastSender;
import me.nizheg.telegram.bot.chgk.service.ChatGameService;
import me.nizheg.telegram.bot.chgk.service.MessageService;
import me.nizheg.telegram.bot.chgk.util.TaskSender;

import static me.nizheg.telegram.bot.chgk.dto.SendingMessage.RECEIVER_ALL;
import static me.nizheg.telegram.bot.chgk.dto.SendingMessage.RECEIVER_ME;

@Service
public class MessageServiceImpl implements MessageService {

    private final BroadcastSender broadcastSender;
    private final ChatGameService chatGameService;
    private final TelegramApiClient telegramApiClient;
    private final TaskSender taskSender;

    private final Object lock = new Object();
    private BroadcastStatus broadcastStatus = new BroadcastStatus(BroadcastStatus.Status.NOT_STARTED);
    private volatile ForwardingMessage forwardingMessage;

    public MessageServiceImpl(
            BroadcastSender broadcastSender,
            ChatGameService chatGameService,
            TelegramApiClient telegramApiClient,
            TaskSender taskSender) {
        this.broadcastSender = broadcastSender;
        this.chatGameService = chatGameService;
        this.telegramApiClient = telegramApiClient;
        this.taskSender = taskSender;
    }

    @Override
    public BroadcastStatus setMessageForForwarding(ForwardingMessage forwardingMessage) {
        synchronized (lock) {
            if (BroadcastStatus.Status.NOT_STARTED.equals(broadcastStatus.getStatus()) || BroadcastStatus.Status.INIT
                    .equals(broadcastStatus.getStatus())) {
                this.forwardingMessage = forwardingMessage;
                this.broadcastStatus = new BroadcastStatus(BroadcastStatus.Status.INIT);
                this.broadcastStatus.setSendingMessage(forwardingMessage.getText());
                return this.broadcastStatus;
            } else {
                return new BroadcastStatus(BroadcastStatus.Status.REJECTED, "Установка сообщения для Forward сейчас "
                        + "не разрешена");
            }
        }

    }

    @Override
    public BroadcastStatus forwardToAll() {
        synchronized (lock) {
            if (BroadcastStatus.Status.IN_PROCESS.equals(broadcastStatus.getStatus())) {
                return new BroadcastStatus(BroadcastStatus.Status.REJECTED,
                        "Предыдущая задача отправки ещё не завершена.");
            } else if (BroadcastStatus.Status.INIT.equals(broadcastStatus.getStatus())){
                this.broadcastStatus = broadcastSender.forwardMessage(convertMessage(forwardingMessage),
                        forwardingMessage.getText());
            }
            return this.broadcastStatus;
        }

    }

    @Override
    public BroadcastStatus send(SendingMessage message, TelegramUser me) {
        synchronized (lock) {
            if (this.forwardingMessage != null) {
                return new BroadcastStatus(BroadcastStatus.Status.REJECTED, "Рассылка находится в режиме Forward");
            }
        }
        String receiver = Optional.ofNullable(message.getReceiver()).orElse(RECEIVER_ME);
        switch (receiver) {
            case RECEIVER_ME:
                if (me != null) {
                    Long taskId = message.getTaskId();
                    if (taskId != null) {
                        Task currentTask = chatGameService.getGame(new Chat(me.getId(), true))
                                .setCurrentTask(taskId);
                        taskSender.sendTaskText(currentTask, me.getId());
                    } else {
                        me.nizheg.telegram.bot.api.service.param.Message sendingMessage = convertMessage(message);
                        sendingMessage.setChatId(new ChatId(me.getId()));
                        telegramApiClient.sendMessage(sendingMessage);
                    }
                    this.broadcastStatus = new BroadcastStatus(BroadcastStatus.Status.FINISHED);
                    return this.broadcastStatus;
                }
                return new BroadcastStatus(BroadcastStatus.Status.REJECTED, "Пользователь не определен");
            case RECEIVER_ALL:
                synchronized (lock) {
                    if (BroadcastStatus.Status.IN_PROCESS.equals(broadcastStatus.getStatus())) {
                        return new BroadcastStatus(BroadcastStatus.Status.REJECTED,
                                "Предыдущая задача отправки ещё не завершена.");
                    } else {
                        this.broadcastStatus = broadcastSender.sendMessage(convertMessage(message));
                        return this.broadcastStatus;
                    }
                }
            default:
                throw new UnsupportedOperationException("Not supported now");
        }
    }

    private me.nizheg.telegram.bot.api.service.param.Message convertMessage(SendingMessage message) {
        return new me.nizheg.telegram.bot.api.service.param.Message(
                message.getText(),
                null,
                Optional.ofNullable(message.getParseMode())
                        .map(ParseMode::valueOf)
                        .orElse(null),
                message.getDisableWebPagePreview());
    }


    private me.nizheg.telegram.bot.api.service.param.ForwardingMessage convertMessage(ForwardingMessage sourceMessage) {
        me.nizheg.telegram.bot.api.service.param.ForwardingMessage forwardingMessage = new me.nizheg.telegram.bot.api
                .service.param.ForwardingMessage();
        forwardingMessage.setFromChatId(new ChatId(sourceMessage.getFromChatId()));
        forwardingMessage.setMessageId(sourceMessage.getMessageId());
        return forwardingMessage;
    }

    @Override
    public BroadcastStatus setStatus(BroadcastStatus status) {
        if (BroadcastStatus.Status.CANCELLED.equals(status.getStatus())) {
            synchronized (lock) {
                broadcastStatus.setStatus(BroadcastStatus.Status.CANCELLED);
                this.forwardingMessage = null;
            }
        }
        return broadcastStatus;
    }

    @Override
    public BroadcastStatus getStatus() {
        synchronized (lock) {
            return broadcastStatus;
        }
    }

}
