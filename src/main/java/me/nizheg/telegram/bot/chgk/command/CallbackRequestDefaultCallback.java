package me.nizheg.telegram.bot.chgk.command;

import org.springframework.http.HttpStatus;

import lombok.RequiredArgsConstructor;
import me.nizheg.telegram.bot.api.model.AbstractCallback;
import me.nizheg.telegram.bot.api.model.ErrorResponse;
import me.nizheg.telegram.bot.api.model.Response;
import me.nizheg.telegram.bot.api.service.TelegramApiClient;
import me.nizheg.telegram.bot.api.service.param.AnswerCallbackRequest;
import me.nizheg.telegram.bot.command.CommandContext;

/**
 * @author Nikolay Zhegalin
 */
@RequiredArgsConstructor
public class CallbackRequestDefaultCallback<T extends Response> extends AbstractCallback<T> {

    private final CommandContext ctx;
    private final TelegramApiClient telegramApiClient;

    @Override
    public void onSuccessResult(T result) {
        if (ctx.getCallbackQueryId() != null) {
            AnswerCallbackRequest answerCallbackRequest = new AnswerCallbackRequest();
            answerCallbackRequest.setCallBackQueryId(ctx.getCallbackQueryId());
            telegramApiClient.answerCallbackQuery(answerCallbackRequest);
        }
    }

    @Override
    public void onFailure(
            ErrorResponse errorResponse, HttpStatus httpStatus) {
        if (ctx.getCallbackQueryId() != null && ctx.isPrivateChat()) {
            AnswerCallbackRequest answerCallbackRequest = new AnswerCallbackRequest();
            answerCallbackRequest.setCallBackQueryId(ctx.getCallbackQueryId());
            answerCallbackRequest.setShowAlert(true);
            if (isNewUser(errorResponse, httpStatus)) {
                answerCallbackRequest.setText("Активируйте бота с помощью команды /start");
            } else if (isBotBlocked(errorResponse, httpStatus)) {
                answerCallbackRequest.setText("Вы заблокировали бота средствами Telegram. Сделайте Unblock bot в "
                        + "настройках");
            } else {
                answerCallbackRequest.setText(
                        "По какой-то причине не удалось отправить подсказку. Свяжитесь с администратором");
            }
            telegramApiClient.answerCallbackQuery(answerCallbackRequest);
        }
    }


}
