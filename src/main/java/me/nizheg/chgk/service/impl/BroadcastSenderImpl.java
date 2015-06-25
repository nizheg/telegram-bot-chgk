package me.nizheg.chgk.service.impl;

import me.nizheg.chgk.dto.BroadcastStatus;
import me.nizheg.chgk.service.BroadcastSender;
import me.nizheg.chgk.service.ChatService;
import me.nizheg.telegram.service.TelegramApiClient;
import me.nizheg.telegram.service.TelegramApiException;
import me.nizheg.telegram.service.param.Message;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * //todo add comments
 *
 * @author Nikolay Zhegalin
 */
@Service
public class BroadcastSenderImpl implements BroadcastSender {

    @Autowired
    private TelegramApiClient telegramApiClient;
    @Autowired
    private ChatService chatService;

    private Log logger = LogFactory.getLog(getClass());

    @Override
    public BroadcastStatus sendMessage(String message) {
        if (StringUtils.isEmpty(message)) {
            return new BroadcastStatus(BroadcastStatus.Status.REJECTED, "Пустой текст");
        }
        final String sendingMessage = message + "\n\nЕсли вы хотите отписаться от рассылок, выключите бота командой /stop";
        final List<Long> activeChats = chatService.getActiveChats();
        logger.info(">>>There was found " + activeChats.size() + " active chats for broadcast");
        final BroadcastStatus broadcastStatus = new BroadcastStatus(BroadcastStatus.Status.IN_PROCESS);
        broadcastStatus.setTotalCount(activeChats.size());
        broadcastStatus.setSendingMessage(sendingMessage);

        new Thread(new Runnable() {
            @Override
            public void run() {
                int tryingCount = 0;
                for (int i = 0; i < activeChats.size();) {
                    if (BroadcastStatus.Status.CANCELLED.equals(broadcastStatus.getStatus())) {
                        logger.info("<<<Broadcast is cancelled. Sent " + i + " of " + broadcastStatus.getTotalCount());
                        return;
                    }
                    Long chatId = activeChats.get(i);
                    i++;
                    try {
                        telegramApiClient.sendMessage(new Message(sendingMessage, chatId, null, true));
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
                                && (ex.getHttpStatus().equals(HttpStatus.FORBIDDEN) || ex.getHttpStatus().equals(HttpStatus.BAD_REQUEST))) {
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
            }
        }).start();
        return broadcastStatus;
    }
}
