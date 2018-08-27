package me.nizheg.telegram.bot.chgk.work;

import org.springframework.http.HttpStatus;

import java.util.function.Supplier;

import lombok.RequiredArgsConstructor;
import lombok.extern.apachecommons.CommonsLog;
import me.nizheg.telegram.bot.api.model.AtomicResponse;
import me.nizheg.telegram.bot.api.model.Message;
import me.nizheg.telegram.bot.api.model.TelegramApiCall;
import me.nizheg.telegram.bot.api.service.TelegramApiClient;
import me.nizheg.telegram.bot.api.service.param.ChatId;
import me.nizheg.telegram.bot.chgk.service.ChatService;

@CommonsLog
@RequiredArgsConstructor
public class ForwardMessageWorker implements Worker {

    private final ChatService chatService;
    private final Supplier<TelegramApiClient> telegramApiClientSupplier;

    @Override
    public boolean canDo(WorkDescription workDescription) {
        return workDescription instanceof ForwardMessageWork;
    }

    @Override
    public void doWork(WorkDescription workDescription) {
        if (!canDo(workDescription)) {
            throw new IllegalArgumentException("Unsupported work");
        }
        ForwardMessageWork forwardMessageWork = (ForwardMessageWork) workDescription;
        me.nizheg.telegram.bot.api.service.param.ForwardingMessage forwardingMessage = new me.nizheg.telegram.bot.api
                .service.param.ForwardingMessage();
        forwardingMessage.setFromChatId(new ChatId(forwardMessageWork.getFromChatId()));
        forwardingMessage.setMessageId(forwardMessageWork.getMessageId());
        long chatId = forwardMessageWork.getChatId();
        forwardingMessage.setChatId(new ChatId(chatId));
        forwardingMessage.setDisableNotification(forwardMessageWork.getDisableNotification());
        TelegramApiCall<AtomicResponse<Message>> messageResponse = telegramApiClientSupplier.get()
                .forwardMessage(forwardingMessage);
        messageResponse.setCallback((errorResponse, httpStatus) -> this.handleError(httpStatus, chatId));
        messageResponse.await();
        if (log.isInfoEnabled()) {
            log.info("Broadcast sending is processed for chat " + chatId);
        }
    }

    private void handleError(HttpStatus httpStatus, long chatId) {
        switch (httpStatus) {
            case FORBIDDEN:
            case BAD_REQUEST:
                chatService.deactivateChat(chatId);
                break;
            default:
                log.error("Api exception. Skip chat " + chatId);
        }

    }
}
