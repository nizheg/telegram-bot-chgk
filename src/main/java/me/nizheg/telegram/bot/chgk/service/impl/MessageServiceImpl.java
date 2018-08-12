package me.nizheg.telegram.bot.chgk.service.impl;

import org.apache.commons.lang3.Validate;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import java.util.function.Supplier;

import javax.annotation.Nonnull;

import me.nizheg.telegram.bot.api.model.ParseMode;
import me.nizheg.telegram.bot.api.service.TelegramApiClient;
import me.nizheg.telegram.bot.api.service.param.ChatId;
import me.nizheg.telegram.bot.api.service.param.Message;
import me.nizheg.telegram.bot.chgk.dto.BroadcastStatus;
import me.nizheg.telegram.bot.chgk.dto.Chat;
import me.nizheg.telegram.bot.chgk.dto.ForwardingMessage;
import me.nizheg.telegram.bot.chgk.dto.SendingMessage;
import me.nizheg.telegram.bot.chgk.dto.TelegramUser;
import me.nizheg.telegram.bot.chgk.dto.composite.Task;
import me.nizheg.telegram.bot.chgk.service.ChatGameService;
import me.nizheg.telegram.bot.chgk.service.ChatService;
import me.nizheg.telegram.bot.chgk.service.MessageService;
import me.nizheg.telegram.bot.chgk.util.TaskSender;
import me.nizheg.util.NamedThreadFactory;

import static me.nizheg.telegram.bot.chgk.dto.SendingMessage.RECEIVER_ALL;
import static me.nizheg.telegram.bot.chgk.dto.SendingMessage.RECEIVER_ME;

@Service
public class MessageServiceImpl implements MessageService {

    private final Log logger = LogFactory.getLog(getClass());
    private final ChatService chatService;
    private final ChatGameService chatGameService;
    private final TelegramApiClient telegramApiClient;
    private final TaskSender taskSender;

    private final ExecutorService executorService = Executors.newSingleThreadExecutor(new NamedThreadFactory
            ("Message Broadcast "));

    private BroadcastStatus broadcastStatus = new BroadcastStatus(BroadcastStatus.Status.NOT_STARTED);
    private volatile ForwardingMessage forwardingMessage;

    public MessageServiceImpl(
            ChatService chatService,
            ChatGameService chatGameService,
            TelegramApiClient telegramApiClient,
            TaskSender taskSender) {
        this.chatService = chatService;
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
                        send(message, Collections.singletonList(myIdValue));
                    }
                } else {
                    this.broadcastStatus = new BroadcastStatus(BroadcastStatus.Status.REJECTED,
                            "Sender is not resolved");
                }
                break;
            case RECEIVER_ALL:
                final List<Long> activeChats = chatService.getActiveChats();
                logger.info(">>>There was found " + activeChats.size() + " active chats for broadcast");
                send(message, activeChats);
                break;
            default:
                throw new UnsupportedOperationException("Not supported now");
        }
        return this.broadcastStatus;
    }

    private void send(SendingMessage message, List<Long> receivers) {
        switch (broadcastStatus.getStatus()) {
            case FORWARD_INITIATED:
                forwardMessage(receivers);
                break;
            case IN_PROCESS:
                break;
            default:
                sendMessage(message, receivers);
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
            ForwardingMessage sourceMessage) {
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


    private void sendMessage(SendingMessage message, List<Long> chatIds) {
        if (StringUtils.isEmpty(message.getText())) {
            this.broadcastStatus = new BroadcastStatus(BroadcastStatus.Status.REJECTED, "Пустой текст");
            return;
        }
        Message telegramMessage = convertMessage(message);
        doBroadcast(message::getText, chatIds, chatId -> {
            telegramMessage.setChatId(chatId);
            telegramApiClient.sendMessage(telegramMessage).setCallback(
                    (errorResponse, httpStatus) -> this.handleError(httpStatus, chatId.getChatId()));
        });
    }

    private void forwardMessage(List<Long> receivers) {
        if (forwardingMessage == null || forwardingMessage.getFromChatId() == null
                || forwardingMessage.getMessageId() == null) {
            this.broadcastStatus = new BroadcastStatus(BroadcastStatus.Status.REJECTED,
                    "Сообщение для рассылки не установлено");
            return;
        }
        final me.nizheg.telegram.bot.api.service.param.ForwardingMessage telegramForwardedMessage =
                convertMessage(forwardingMessage);
        final String text = this.forwardingMessage.getText();

        doBroadcast(() -> text, receivers, chatId -> {
            telegramForwardedMessage.setChatId(chatId);
            telegramApiClient.forwardMessage(telegramForwardedMessage).setCallback(
                    (errorResponse, httpStatus) -> this.handleError(httpStatus, chatId.getChatId()));
        });
    }

    private void handleError(HttpStatus httpStatus, long chatId) {
        switch (httpStatus) {
            case FORBIDDEN:
            case BAD_REQUEST:
                chatService.deactivateChat(chatId);
                break;
            default:
                logger.error("Api exception. Skip chat " + chatId);
        }

    }

    private void doBroadcast(
            Supplier<String> messageTextSupplier, List<Long> receivers, Consumer<ChatId> messageSender) {
        this.broadcastStatus = new BroadcastStatus(BroadcastStatus.Status.IN_PROCESS);
        broadcastStatus.setTotalCount(receivers.size());
        broadcastStatus.setMessage(messageTextSupplier.get());
        executorService.submit(new SendingTask(receivers, broadcastStatus, messageSender));
    }
}
