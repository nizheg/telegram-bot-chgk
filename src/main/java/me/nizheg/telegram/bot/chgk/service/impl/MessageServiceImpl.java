package me.nizheg.telegram.bot.chgk.service.impl;

import org.apache.commons.lang3.Validate;
import org.springframework.stereotype.Service;

import java.util.Optional;

import javax.annotation.Nonnull;

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
    public synchronized BroadcastStatus setMessageForForwarding(@Nonnull ForwardingMessage forwardingMessage) {
        Validate.notNull(forwardingMessage);
        switch (broadcastStatus.getStatus()) {
            case NOT_STARTED:
            case REJECTED:
            case FORWARD_INITIATED:
            case FINISHED:
            case CANCELLED:
                this.forwardingMessage = forwardingMessage;
                this.broadcastStatus = new BroadcastStatus(BroadcastStatus.Status.FORWARD_INITIATED);
                this.broadcastStatus.setMessage(forwardingMessage.getText());
                break;
            default:
        }
        return this.broadcastStatus;
    }

    @Override
    public synchronized BroadcastStatus send(SendingMessage message, TelegramUser me) {
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
                    return new BroadcastStatus(BroadcastStatus.Status.FINISHED);
                }
                return new BroadcastStatus(BroadcastStatus.Status.REJECTED);
            case RECEIVER_ALL:
                switch (broadcastStatus.getStatus()) {
                    case FORWARD_INITIATED:
                        this.broadcastStatus = broadcastSender.forwardMessage(convertMessage(forwardingMessage),
                                forwardingMessage.getText());
                        break;
                    case IN_PROCESS:
                        break;
                    default:
                        this.broadcastStatus = broadcastSender.sendMessage(convertMessage(message));
                }
                return this.broadcastStatus;
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
    public synchronized BroadcastStatus setStatus(BroadcastStatus status) {
        if (BroadcastStatus.Status.CANCELLED.equals(status.getStatus())) {
            broadcastStatus.setStatus(BroadcastStatus.Status.CANCELLED);
            this.forwardingMessage = null;
        }
        return broadcastStatus;
    }

    @Override
    public synchronized BroadcastStatus getStatus() {
        return broadcastStatus;
    }

}
