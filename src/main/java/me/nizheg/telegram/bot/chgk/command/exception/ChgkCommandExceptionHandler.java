package me.nizheg.telegram.bot.chgk.command.exception;

import org.apache.commons.lang3.Validate;

import javax.annotation.Nonnull;

import me.nizheg.telegram.bot.api.model.ParseMode;
import me.nizheg.telegram.bot.api.service.TelegramApiClient;
import me.nizheg.telegram.bot.api.service.param.AnswerCallbackRequest;
import me.nizheg.telegram.bot.api.service.param.Message;
import me.nizheg.telegram.bot.api.util.TelegramHtmlUtil;
import me.nizheg.telegram.bot.command.CommandContext;
import me.nizheg.telegram.bot.command.CommandException;
import me.nizheg.telegram.bot.service.ExceptionHandler;
import me.nizheg.telegram.util.TelegramApiUtil;

public class ChgkCommandExceptionHandler implements ExceptionHandler {

    private static final String NO_TASK_MESSAGE = "Активных вопросов не обнаружено.";
    private final TelegramApiClient telegramApiClient;

    public ChgkCommandExceptionHandler(@Nonnull TelegramApiClient telegramApiClient) {
        Validate.notNull(telegramApiClient);
        this.telegramApiClient = telegramApiClient;
    }

    @Override
    public void handleMessageException(CommandException commandException, CommandContext context) {
        if (commandException instanceof NoTaskException) {
            handleMessageException((NoTaskException) commandException, context);
        } else if (commandException instanceof BotIsNotStartedException) {
            handleMessageException((BotIsNotStartedException) commandException, context);
        } else {
            telegramApiClient.sendMessage(
                    new Message("<i>" +
                            TelegramHtmlUtil.escape(commandException.getMessage()) +
                            "</i>", context.getChatId(),
                            ParseMode.HTML));
        }
    }

    private void handleMessageException(NoTaskException commandException, CommandContext context) {
        telegramApiClient.sendMessage(
                new Message("<i>" + NO_TASK_MESSAGE + "</i>",
                        context.getChatId(), ParseMode.HTML, null, null,
                        TelegramApiUtil.createInlineButtonMarkup("Получить вопрос", "next")));
    }

    private void handleMessageException(BotIsNotStartedException commandException, CommandContext context) {
        telegramApiClient.sendMessage(
                new Message("<i>Необходимо активировать бота с помощью команды</i> /start",
                        context.getChatId(), ParseMode.HTML));
    }

    @Override
    public void handleMessageException(RuntimeException runtimeException, CommandContext context) {
        telegramApiClient.sendMessage(
                new Message("<i>" + getErrorDefaultMessage() + "</i>", context.getChatId(), ParseMode.HTML));
    }

    @Override
    public void handleCallbackQueryException(CommandException commandException, CommandContext context) {
        if (commandException instanceof NoTaskException) {
            sendCallbackAnswer(NO_TASK_MESSAGE, context);
        } else if (commandException instanceof BotIsNotStartedException) {
            sendCallbackAnswer("Необходимо активировать бота с помощью команды /start", context);
        } else {
            sendCallbackAnswer(commandException.getMessage(), context);
        }
    }

    @Override
    public void handleCallbackQueryException(RuntimeException runtimeException, CommandContext context) {
        sendCallbackAnswer(getErrorDefaultMessage(), context);
    }

    private void sendCallbackAnswer(String text, CommandContext context) {
        AnswerCallbackRequest answerCallbackRequest = new AnswerCallbackRequest();
        answerCallbackRequest.setCallBackQueryId(context.getCallbackQueryId());
        answerCallbackRequest.setText(text);
        answerCallbackRequest.setShowAlert(true);
        telegramApiClient.answerCallbackQuery(answerCallbackRequest);
    }


    protected String getErrorDefaultMessage() {
        return "К сожалению, по какой-то причине мы не смогли обработать ваше сообщение. "
                + "Попробуйте позднее или свяжитесь с разработчиком.";
    }
}
