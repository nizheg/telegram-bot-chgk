package me.nizheg.telegram.bot.chgk.service.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.List;
import java.util.function.Consumer;

import me.nizheg.telegram.bot.api.service.TelegramApiException;
import me.nizheg.telegram.bot.api.service.param.ChatId;
import me.nizheg.telegram.bot.chgk.dto.BroadcastStatus;

/**
 * @author Nikolay Zhegalin
 */
public class SendingTask implements Runnable {

    private final Log logger = LogFactory.getLog(getClass());
    private final List<Long> receivers;
    private final BroadcastStatus broadcastStatus;
    private final Consumer<ChatId> messageSender;
    private final Consumer<Long> sendingFailedCallback;

    public SendingTask(
            List<Long> receivers,
            BroadcastStatus broadcastStatus,
            Consumer<ChatId> sendOperation,
            Consumer<Long> sendingFailedCallback) {
        this.receivers = receivers;
        this.broadcastStatus = broadcastStatus;
        this.messageSender = sendOperation;
        this.sendingFailedCallback = sendingFailedCallback;
    }

    @Override
    public void run() {
        int tryingCount = 0;
        for (int i = 0; i < receivers.size(); ) {
            if (BroadcastStatus.Status.CANCELLED.equals(broadcastStatus.getStatus())) {
                logger.info("<<<Broadcast is cancelled. Sent " + i + " of " + broadcastStatus.getTotalCount());
                return;
            }
            Long chatId = receivers.get(i);
            i++;
            try {
                messageSender.accept(new ChatId(chatId));
                tryingCount = 0;
            } catch (TelegramApiException ex) {
                switch (ex.getHttpStatus()) {
                    case TOO_MANY_REQUESTS:
                        logger.warn("Too many requests detected");
                        if (tryingCount > 3) {
                            logger.error("Chat is skipped " + chatId, ex);
                        } else {
                            tryingCount++;
                            i--;
                            try {
                                Thread.sleep(3000);
                            } catch (InterruptedException e) {
                                logger.error("interrupted", e);
                                Thread.currentThread().interrupt();
                                return;
                            }
                        }
                    case FORBIDDEN:
                    case BAD_REQUEST:
                        sendingFailedCallback.accept(chatId);
                        break;
                    default:
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
    }
}
