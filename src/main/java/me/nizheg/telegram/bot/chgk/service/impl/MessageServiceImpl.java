package me.nizheg.telegram.bot.chgk.service.impl;

import org.apache.commons.lang3.Validate;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Optional;

import javax.annotation.Nonnull;

import lombok.RequiredArgsConstructor;
import me.nizheg.telegram.bot.api.model.AtomicResponse;
import me.nizheg.telegram.bot.api.model.Callback;
import me.nizheg.telegram.bot.api.model.ErrorResponse;
import me.nizheg.telegram.bot.api.model.ParseMode;
import me.nizheg.telegram.bot.api.service.TelegramApiClient;
import me.nizheg.telegram.bot.api.service.param.ChatId;
import me.nizheg.telegram.bot.api.service.param.Message;
import me.nizheg.telegram.bot.chgk.dto.BroadcastStatus;
import me.nizheg.telegram.bot.chgk.dto.Chat;
import me.nizheg.telegram.bot.chgk.dto.SendingMessage;
import me.nizheg.telegram.bot.chgk.dto.TelegramUser;
import me.nizheg.telegram.bot.chgk.dto.composite.Task;
import me.nizheg.telegram.bot.chgk.service.ChatGameService;
import me.nizheg.telegram.bot.chgk.service.ChatService;
import me.nizheg.telegram.bot.chgk.service.MessageService;
import me.nizheg.telegram.bot.chgk.util.TaskSender;
import me.nizheg.telegram.bot.chgk.work.WorkService;
import me.nizheg.telegram.bot.chgk.work.data.ForwardMessageData;
import me.nizheg.telegram.bot.chgk.work.data.SendMessageData;

import static me.nizheg.telegram.bot.chgk.dto.SendingMessage.RECEIVER_ALL;
import static me.nizheg.telegram.bot.chgk.dto.SendingMessage.RECEIVER_ME;

@Service
@RequiredArgsConstructor
public class MessageServiceImpl implements MessageService {

    private final Log logger = LogFactory.getLog(getClass());
    private final ChatService chatService;
    private final ChatGameService chatGameService;
    private final TelegramApiClient telegramApiClient;
    private final TaskSender taskSender;
    private final WorkService workService;

    private BroadcastStatus broadcastStatus = new BroadcastStatus(BroadcastStatus.Status.NOT_STARTED);
    private volatile ForwardMessageData forwardMessageData;

    @Override
    public synchronized void setMessageForForwarding(@Nonnull ForwardMessageData forwardMessageData) {
        Validate.notNull(forwardMessageData);
        switch (broadcastStatus.getStatus()) {
            case NOT_STARTED:
            case REJECTED:
            case FORWARD_INITIATED:
            case FINISHED:
            case CANCELLED:
                this.forwardMessageData = forwardMessageData;
                this.broadcastStatus = new BroadcastStatus(BroadcastStatus.Status.FORWARD_INITIATED);
                this.broadcastStatus.setMessage(forwardMessageData.getText());
                break;
            default:
        }
    }

    @Override
    public synchronized BroadcastStatus send(SendingMessage message) {
        String receiver = Optional.ofNullable(message.getReceiver()).orElse(RECEIVER_ME);
        switch (receiver) {
            case RECEIVER_ME:
                Optional<Long> myId = Optional.ofNullable(message.getSender()).map(TelegramUser::getId);
                if (myId.isPresent()) {
                    Long taskId = message.getTaskId();
                    Long myIdValue = myId.get();
                    if (taskId != null) {
                        Task currentTask = chatGameService.getGame(new Chat(myIdValue, true)).setCurrentTask(taskId);
                        taskSender.sendTaskText(currentTask, myIdValue);
                        this.broadcastStatus = new BroadcastStatus(BroadcastStatus.Status.FINISHED);
                    } else {
                        send(message, myIdValue);
                    }
                } else {
                    this.broadcastStatus = new BroadcastStatus(BroadcastStatus.Status.REJECTED,
                            "Sender is not resolved");
                }
                break;
            case RECEIVER_ALL:
                switch (broadcastStatus.getStatus()) {
                    case FORWARD_INITIATED:
                        forwardMessageToAll();
                        break;
                    case IN_PROCESS:
                        break;
                    default:
                        sendMessageToAll(message);
                }
                break;
            default:
                throw new UnsupportedOperationException("Not supported now");
        }
        return this.broadcastStatus;
    }

    private void send(SendingMessage message, Long chatId) {
        switch (broadcastStatus.getStatus()) {
            case FORWARD_INITIATED:
                forwardMessage(chatId);
                break;
            case IN_PROCESS:
                break;
            default:
                sendMessage(message, chatId);
        }
    }

    private static me.nizheg.telegram.bot.api.service.param.Message convertMessage(SendingMessage message) {
        return new me.nizheg.telegram.bot.api.service.param.Message(
                message.getText(),
                null,
                Optional.ofNullable(message.getParseMode())
                        .map(ParseMode::valueOf)
                        .orElse(null),
                message.getDisableWebPagePreview());
    }


    private static me.nizheg.telegram.bot.api.service.param.ForwardingMessage convertMessage(
            ForwardMessageData sourceMessage) {
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
            this.forwardMessageData = null;
        }
        return broadcastStatus;
    }

    @Override
    public synchronized BroadcastStatus getStatus() {
        return broadcastStatus;
    }


    private void sendMessage(SendingMessage message, Long chatId) {
        if (StringUtils.isEmpty(message.getText())) {
            this.broadcastStatus = new BroadcastStatus(BroadcastStatus.Status.REJECTED, "Пустой текст");
            return;
        }
        Message telegramMessage = convertMessage(message);
        this.broadcastStatus = new BroadcastStatus(BroadcastStatus.Status.IN_PROCESS);
        broadcastStatus.setTotalCount(1);
        broadcastStatus.setMessage(message.getText());
        telegramMessage.setChatId(new ChatId(chatId));
        telegramApiClient.sendMessage(telegramMessage).setCallback(new SendCallback(chatId));
    }

    private void sendMessageToAll(SendingMessage message) {
        if (StringUtils.isEmpty(message.getText())) {
            this.broadcastStatus = new BroadcastStatus(BroadcastStatus.Status.REJECTED, "Пустой текст");
            return;
        }
        SendMessageData sendMessageData = new SendMessageData();
        sendMessageData.setText(message.getText());
        sendMessageData.setDisableWebPagePreview(message.getDisableWebPagePreview());
        sendMessageData.setParseMode(message.getParseMode());
        workService.sendMessageToActiveChats(sendMessageData);
    }

    private void forwardMessage(Long chatId) {
        if (forwardMessageData == null) {
            this.broadcastStatus = new BroadcastStatus(BroadcastStatus.Status.REJECTED,
                    "Сообщение для отправки не установлено");
            return;
        }
        final me.nizheg.telegram.bot.api.service.param.ForwardingMessage telegramForwardedMessage =
                convertMessage(forwardMessageData);
        final String text = this.forwardMessageData.getText();
        this.broadcastStatus = new BroadcastStatus(BroadcastStatus.Status.IN_PROCESS);
        broadcastStatus.setTotalCount(1);
        broadcastStatus.setMessage(text);
        telegramForwardedMessage.setChatId(new ChatId(chatId));
        telegramApiClient.forwardMessage(telegramForwardedMessage).setCallback(new SendCallback(chatId));
    }

    private void forwardMessageToAll() {
        if (forwardMessageData == null) {
            this.broadcastStatus = new BroadcastStatus(BroadcastStatus.Status.REJECTED,
                    "Сообщение для рассылки не установлено");
            return;
        }
        workService.forwardMessageToActiveChats(this.forwardMessageData);

    }

    private class SendCallback implements Callback<AtomicResponse<me.nizheg.telegram.bot.api.model.Message>> {

        private final Long chatId;

        private SendCallback(Long chatId) {this.chatId = chatId;}

        @Override
        public void onFailure(ErrorResponse errorResponse, HttpStatus httpStatus) {
            switch (httpStatus) {
                case FORBIDDEN:
                case BAD_REQUEST:
                    chatService.deactivateChat(chatId);
                    break;
                default:
                    logger.error("Api exception. Skip chat " + chatId);
            }
        }

        @Override
        public void onSuccessResult(AtomicResponse<me.nizheg.telegram.bot.api.model.Message> result) {
            broadcastStatus.setStatus(BroadcastStatus.Status.FINISHED);
        }
    }
}
