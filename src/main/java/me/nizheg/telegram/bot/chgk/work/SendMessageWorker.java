package me.nizheg.telegram.bot.chgk.work;

import org.springframework.http.HttpStatus;
import org.springframework.util.StringUtils;

import java.util.Optional;
import java.util.function.Supplier;

import lombok.RequiredArgsConstructor;
import lombok.extern.apachecommons.CommonsLog;
import me.nizheg.telegram.bot.api.model.AtomicResponse;
import me.nizheg.telegram.bot.api.model.ParseMode;
import me.nizheg.telegram.bot.api.model.TelegramApiCall;
import me.nizheg.telegram.bot.api.service.TelegramApiClient;
import me.nizheg.telegram.bot.api.service.param.Message;
import me.nizheg.telegram.bot.chgk.service.ChatService;

@CommonsLog
@RequiredArgsConstructor
public class SendMessageWorker implements Worker {

    private final ChatService chatService;
    private final Supplier<TelegramApiClient> telegramApiClientSupplier;

    @Override
    public boolean canDo(WorkDescription workDescription) {
        return workDescription instanceof SendMessageWork;
    }

    @Override
    public void doWork(WorkDescription workDescription) {
        if (!canDo(workDescription)) {
            throw new IllegalArgumentException("Unsupported work");
        }
        SendMessageWork sendMessageWork = (SendMessageWork) workDescription;
        if (StringUtils.isEmpty(sendMessageWork.getText())) {
            return;
        }
        Long chatId = sendMessageWork.getChatId();
        Message telegramMessage = new Message(
                sendMessageWork.getText(),
                chatId,
                Optional.ofNullable(sendMessageWork.getParseMode())
                        .map(ParseMode::valueOf)
                        .orElse(null),
                sendMessageWork.getDisableWebPagePreview());


        TelegramApiCall<AtomicResponse<me.nizheg.telegram.bot.api.model.Message>> messageResponse =
                telegramApiClientSupplier.get().sendMessage(telegramMessage);
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
