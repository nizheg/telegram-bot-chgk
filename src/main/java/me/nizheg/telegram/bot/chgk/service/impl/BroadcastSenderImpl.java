package me.nizheg.telegram.bot.chgk.service.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

import me.nizheg.telegram.bot.api.service.TelegramApiClient;
import me.nizheg.telegram.bot.api.service.TelegramApiException;
import me.nizheg.telegram.bot.api.service.param.ChatId;
import me.nizheg.telegram.bot.api.service.param.ForwardingMessage;
import me.nizheg.telegram.bot.api.service.param.Message;
import me.nizheg.telegram.bot.chgk.dto.BroadcastStatus;
import me.nizheg.telegram.bot.chgk.service.BroadcastSender;
import me.nizheg.telegram.bot.chgk.service.ChatService;

/**
 * @author Nikolay Zhegalin
 */
@Service
public class BroadcastSenderImpl implements BroadcastSender {

    private final TelegramApiClient telegramApiClient;
    private final ChatService chatService;

    private final Log logger = LogFactory.getLog(getClass());

    public BroadcastSenderImpl(
            TelegramApiClient telegramApiClient,
            ChatService chatService) {
        this.telegramApiClient = telegramApiClient;
        this.chatService = chatService;
    }

    @Override
    public BroadcastStatus sendMessage(Message message) {
        if (StringUtils.isEmpty(message.getText())) {
            return new BroadcastStatus(BroadcastStatus.Status.REJECTED, "Пустой текст");
        }
        return doBroadcast(message::getText, chatId -> {
            message.setChatId(chatId);
            telegramApiClient.sendMessage(message);
        });
    }

    @Override
    public BroadcastStatus forwardMessage(ForwardingMessage forwardingMessage, String description) {
        if (forwardingMessage == null || forwardingMessage.getFromChatId() == null
                || forwardingMessage.getMessageId() == null) {
            return new BroadcastStatus(BroadcastStatus.Status.REJECTED, "Сообщение для рассылки не установлено");
        }
        return doBroadcast(() -> description, chatId -> {
            forwardingMessage.setChatId(chatId);
            telegramApiClient.forwardMessage(forwardingMessage);
        });
    }

    private BroadcastStatus doBroadcast(Supplier<String> messageTextSupplier, Consumer<ChatId> messageSender) {
        final List<Long> activeChats = chatService.getActiveChats();
        logger.info(">>>There was found " + activeChats.size() + " active chats for broadcast");
        final BroadcastStatus broadcastStatus = new BroadcastStatus(BroadcastStatus.Status.IN_PROCESS);
        broadcastStatus.setTotalCount(activeChats.size());
        broadcastStatus.setSendingMessage(messageTextSupplier.get());

        new Thread(() -> {
            int tryingCount = 0;
            for (int i = 0; i < activeChats.size(); ) {
                if (BroadcastStatus.Status.CANCELLED.equals(broadcastStatus.getStatus())) {
                    logger.info("<<<Broadcast is cancelled. Sent " + i + " of " + broadcastStatus.getTotalCount());
                    return;
                }
                Long chatId = activeChats.get(i);
                i++;
                try {
                    messageSender.accept(new ChatId(chatId));
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        logger.error("interrupted", e);
                        Thread.interrupted();
                        return;
                    }
                    tryingCount = 0;
                } catch (TelegramApiException ex) {
                    if (ex.getHttpStatus() != null && ex.getHttpStatus().equals(HttpStatus.TOO_MANY_REQUESTS)) {
                        if (tryingCount > 3) {
                            logger.error("Chat is skipped " + chatId, ex);
                        } else {
                            tryingCount++;
                            i--;
                            try {
                                Thread.sleep(3000);
                            } catch (InterruptedException e) {
                                logger.error("interrupted", e);
                                Thread.interrupted();
                                return;
                            }
                        }
                    } else if (ex.getHttpStatus() != null
                            && (ex.getHttpStatus().equals(HttpStatus.FORBIDDEN) || ex.getHttpStatus()
                            .equals(HttpStatus.BAD_REQUEST))) {
                        chatService.deactivateChat(chatId);
                    } else {
                        logger.error("Api exception. Skip chat " + chatId, ex);
                    }
                } catch (RuntimeException ex) {
                    logger.error("Unexpected exception. Skip chat " + chatId, ex);
                }
                if (logger.isInfoEnabled()) {
                    logger.info("Broadcast sending is processed for chat " + chatId);
                }
                broadcastStatus.setFinished(i);
            }
            broadcastStatus.setStatus(BroadcastStatus.Status.FINISHED);
            logger.info("<<<Broadcast is finished");
        }).start();
        return broadcastStatus;
    }

}
