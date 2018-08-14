package me.nizheg.telegram.bot.chgk.command;

import lombok.RequiredArgsConstructor;
import me.nizheg.telegram.bot.api.model.AbstractCallback;
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
        if (ctx.isCallbackQuery()) {
            AnswerCallbackRequest answerCallbackRequest = new AnswerCallbackRequest();
            answerCallbackRequest.setCallBackQueryId(ctx.getCallbackQueryId());
            telegramApiClient.answerCallbackQuery(answerCallbackRequest);
        }
    }

    @Override
    protected void handleBotBlocked() {
        if (ctx.isCallbackQuery() && ctx.isPrivateChat()) {
            sendCallbackQueryResponse("Вы заблокировали бота средствами Telegram. Сделайте Unblock bot в настройках");
        }
    }

    @Override
    protected void handleUnknownUser() {
        if (ctx.isCallbackQuery() && ctx.isPrivateChat()) {
            sendCallbackQueryResponse("Активируйте бота с помощью команды /start");
        }
    }

    private void sendCallbackQueryResponse(String message) {
        AnswerCallbackRequest answerCallbackRequest = new AnswerCallbackRequest();
        answerCallbackRequest.setCallBackQueryId(ctx.getCallbackQueryId());
        answerCallbackRequest.setShowAlert(true);
        answerCallbackRequest.setText(message);
        telegramApiClient.answerCallbackQuery(answerCallbackRequest);
    }
}
