package me.nizheg.telegram.bot.chgk.command;

import org.springframework.stereotype.Component;

import java.util.function.Supplier;

import lombok.NonNull;
import me.nizheg.telegram.bot.api.model.AbstractCallback;
import me.nizheg.telegram.bot.api.model.AtomicResponse;
import me.nizheg.telegram.bot.api.model.ParseMode;
import me.nizheg.telegram.bot.api.service.TelegramApiClient;
import me.nizheg.telegram.bot.api.service.param.AnswerCallbackRequest;
import me.nizheg.telegram.bot.api.service.param.ChatId;
import me.nizheg.telegram.bot.api.service.param.Message;
import me.nizheg.telegram.bot.chgk.command.exception.NoTaskException;
import me.nizheg.telegram.bot.chgk.domain.ChatGame;
import me.nizheg.telegram.bot.chgk.domain.HintResult;
import me.nizheg.telegram.bot.chgk.dto.Chat;
import me.nizheg.telegram.bot.chgk.dto.composite.Task;
import me.nizheg.telegram.bot.chgk.exception.GameException;
import me.nizheg.telegram.bot.chgk.service.ChatGameService;
import me.nizheg.telegram.bot.chgk.service.ChatService;
import me.nizheg.telegram.bot.chgk.util.AnswerSender;
import me.nizheg.telegram.bot.command.ChatCommand;
import me.nizheg.telegram.bot.command.CommandContext;
import me.nizheg.telegram.bot.command.CommandException;

/**
 * @author Nikolay Zhegalin
 */
@UserInChannel
@ChatActive
@Component
public class HintCommand extends ChatCommand {

    private final ChatService chatService;
    private final ChatGameService chatGameService;
    private final AnswerSender answerSender;

    @Override
    public int getPriority() {
        return 50;
    }

    public HintCommand(
            @NonNull Supplier<TelegramApiClient> telegramApiClientSupplier,
            @NonNull ChatService chatService,
            @NonNull ChatGameService chatGameService,
            @NonNull AnswerSender answerSender) {
        super(telegramApiClientSupplier);
        this.chatService = chatService;
        this.chatGameService = chatGameService;
        this.answerSender = answerSender;
    }

    protected ChatService getChatService() {
        return chatService;
    }

    protected ChatGameService getChatGameService() {
        return chatGameService;
    }

    @Override
    public void execute(CommandContext ctx) throws CommandException {
        Long taskId = parseTaskId(ctx);
        try {
            ChatGame chatGame = chatGameService.getGame(new Chat(ctx.getChat()));
            HintResult hintForTask = chatGame.getHintForTask(new Chat(ctx.getFrom()), taskId);
            Task hintForTaskTask = hintForTask.getTask().orElseThrow(NoTaskException::new);
            sendAnswerToUser(ctx, hintForTaskTask);
        } catch (GameException e) {
            throw new CommandException(e.getMessage(), e);
        }
    }

    private void sendAnswerToUser(CommandContext ctx, Task currentTask) {
        TelegramApiClient telegramApiClient = getTelegramApiClient();
        answerSender.sendAnswer(currentTask, false, ctx.getFrom().getId(), new Callback(ctx, telegramApiClient));
    }

    @Override
    public void sendCallbackResponse(CommandContext ctx) {
    }

    private Long parseTaskId(CommandContext ctx) {
        Long taskId = null;
        if (ctx.getText() != null) {
            try {
                taskId = Long.valueOf(ctx.getText());
            } catch (NumberFormatException ex) {
                // ignore
            }
        }
        return taskId;
    }

    @Override
    public String getCommandName() {
        return "hint";
    }

    @Override
    public String getDescription() {
        return "/hint - получить ответ на текущий вопрос в личку";
    }

    private class Callback extends AbstractCallback<AtomicResponse<me.nizheg.telegram.bot.api.model.Message>> {

        private static final String BOT_BLOCKED_ERROR = "Не удалось отправить подсказку. "
                + "Вы заблокировали бота средствами Telegram. Сделайте Unblock bot в настройках.";
        private static final String UNKNOWN_USER_ERROR = "Не удалось отправить подсказку. Активируйте бота с помощью "
                + "команды /start в личке с ним";
        private final CommandContext ctx;
        private final TelegramApiClient telegramApiClient;

        public Callback(CommandContext ctx, TelegramApiClient telegramApiClient) {
            this.ctx = ctx;
            this.telegramApiClient = telegramApiClient;
        }

        @Override
        protected void handleBotBlocked() {
            if (ctx.isCallbackQuery()) {
                sendCallbackQueryResponse(BOT_BLOCKED_ERROR);
            } else {
                sendErrorMessage(BOT_BLOCKED_ERROR);
            }
        }

        @Override
        protected void handleUnknownUser() {
            if (ctx.isCallbackQuery()) {
                sendCallbackQueryResponse(UNKNOWN_USER_ERROR);
            } else {
                sendErrorMessage(UNKNOWN_USER_ERROR);
            }
        }

        private void sendCallbackQueryResponse(String message) {
            AnswerCallbackRequest answerCallbackRequest = new AnswerCallbackRequest();
            answerCallbackRequest.setCallBackQueryId(ctx.getCallbackQueryId());
            answerCallbackRequest.setShowAlert(true);
            answerCallbackRequest.setText(message);
            telegramApiClient.answerCallbackQuery(answerCallbackRequest);
        }

        private void sendErrorMessage(String message) {
            telegramApiClient.sendMessage(Message.safeMessageBuilder()
                    .text("<i>" + message + "</i>")
                    .chatId(new ChatId(ctx.getChatId()))
                    .parseMode(ParseMode.HTML)
                    .build());
        }

        @Override
        public void onSuccessResult(AtomicResponse<me.nizheg.telegram.bot.api.model.Message> result) {
            if (ctx.isCallbackQuery()) {
                AnswerCallbackRequest answerCallbackRequest = new AnswerCallbackRequest();
                answerCallbackRequest.setCallBackQueryId(ctx.getCallbackQueryId());
                telegramApiClient.answerCallbackQuery(answerCallbackRequest);
            }
        }
    }
}
