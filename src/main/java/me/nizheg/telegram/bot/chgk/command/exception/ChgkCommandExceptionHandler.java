package me.nizheg.telegram.bot.chgk.command.exception;

import java.util.function.Supplier;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import me.nizheg.telegram.bot.api.model.ParseMode;
import me.nizheg.telegram.bot.api.model.User;
import me.nizheg.telegram.bot.api.service.TelegramApiClient;
import me.nizheg.telegram.bot.api.service.param.AnswerCallbackRequest;
import me.nizheg.telegram.bot.api.service.param.ChatId;
import me.nizheg.telegram.bot.api.service.param.Message;
import me.nizheg.telegram.bot.api.util.TelegramHtmlUtil;
import me.nizheg.telegram.bot.command.CommandContext;
import me.nizheg.telegram.bot.command.CommandException;
import me.nizheg.telegram.bot.service.ExceptionHandler;
import me.nizheg.telegram.util.TelegramApiUtil;

import static me.nizheg.telegram.bot.api.model.InlineKeyboardButton.callbackDataButton;
import static me.nizheg.telegram.bot.api.model.InlineKeyboardMarkup.oneButton;

@RequiredArgsConstructor
public class ChgkCommandExceptionHandler implements ExceptionHandler {

    private static final String NO_TASK_MESSAGE = "Активных вопросов не обнаружено.";
    @NonNull
    private final Supplier<TelegramApiClient> telegramApiClientSupplier;

    private TelegramApiClient getTelegramApiClient() {
        return telegramApiClientSupplier.get();
    }

    @Override
    public void handleMessageException(Exception exception, CommandContext context) {
        TelegramApiClient telegramApiClient = getTelegramApiClient();
        if (exception instanceof NoTaskException) {
            handleMessageException((NoTaskException) exception, context);
        } else if (exception instanceof BotIsNotStartedException) {
            handleMessageException((BotIsNotStartedException) exception, context);
        } else if (exception instanceof UserIsNotInChannelException) {
            handleMessageException((UserIsNotInChannelException) exception, context);
        } else if (exception instanceof CommandException) {
            telegramApiClient.sendMessage(Message.safeMessageBuilder()
                    .text("<i>" + TelegramHtmlUtil.escape(exception.getMessage()) + "</i>")
                    .chatId(new ChatId(context.getChatId()))
                    .parseMode(ParseMode.HTML)
                    .build());
        } else {
            telegramApiClient.sendMessage(Message.safeMessageBuilder()
                    .text("<i>" + getErrorDefaultMessage() + "</i>")
                    .chatId(new ChatId(context.getChatId()))
                    .parseMode(ParseMode.HTML)
                    .build());
        }
    }

    private void handleMessageException(UserIsNotInChannelException exception, CommandContext context) {
        User user = exception.getUser();
        String mention = TelegramApiUtil.createUserMention(user);
        getTelegramApiClient().sendMessage(Message.safeMessageBuilder()
                .text(mention + ", <i>для того, чтобы пользоваться ботом, вы должны состоять в канале</i> " +
                        exception.getChannelName())
                .chatId(new ChatId(context.getChatId()))
                .parseMode(ParseMode.HTML)
                .build());
    }

    private void handleMessageException(NoTaskException commandException, CommandContext context) {
        getTelegramApiClient().sendMessage(
                Message.safeMessageBuilder()
                        .text("<i>" + NO_TASK_MESSAGE + "</i>")
                        .chatId(new ChatId(context.getChatId()))
                        .parseMode(ParseMode.HTML)
                        .replyMarkup(oneButton(callbackDataButton("Получить вопрос", "next")))
                        .build());
    }

    private void handleMessageException(BotIsNotStartedException commandException, CommandContext context) {
        getTelegramApiClient().sendMessage(
                Message.safeMessageBuilder()
                        .text("<i>Необходимо активировать бота с помощью команды</i> /start")
                        .chatId(new ChatId(context.getChatId()))
                        .parseMode(ParseMode.HTML)
                        .build());
    }

    @Override
    public void handleCallbackQueryException(Exception commandException, CommandContext context) {
        if (commandException instanceof NoTaskException) {
            sendCallbackAnswer(NO_TASK_MESSAGE, context);
        } else if (commandException instanceof BotIsNotStartedException) {
            sendCallbackAnswer("Необходимо активировать бота с помощью команды /start", context);
        } else if (commandException instanceof UserIsNotInChannelException) {
            sendCallbackAnswer("Для того, чтобы пользоваться ботом, вы должны состоять в канале " +
                    ((UserIsNotInChannelException) commandException).getChannelName(), context);
        } else if (commandException instanceof CommandException) {
            sendCallbackAnswer(commandException.getMessage(), context);
        } else {
            sendCallbackAnswer(getErrorDefaultMessage(), context);
        }
    }

    private void sendCallbackAnswer(String text, CommandContext context) {
        AnswerCallbackRequest answerCallbackRequest = new AnswerCallbackRequest();
        answerCallbackRequest.setCallBackQueryId(context.getCallbackQueryId());
        answerCallbackRequest.setText(text);
        answerCallbackRequest.setShowAlert(true);
        getTelegramApiClient().answerCallbackQuery(answerCallbackRequest);
    }


    private String getErrorDefaultMessage() {
        return "К сожалению, по какой-то причине мы не смогли обработать ваше сообщение. "
                + "Попробуйте позднее или свяжитесь с разработчиком.";
    }
}
