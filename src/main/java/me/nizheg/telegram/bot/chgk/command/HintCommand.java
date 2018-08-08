package me.nizheg.telegram.bot.chgk.command;

import java.util.function.Supplier;

import lombok.NonNull;
import me.nizheg.telegram.bot.api.model.ParseMode;
import me.nizheg.telegram.bot.api.service.TelegramApiClient;
import me.nizheg.telegram.bot.api.service.TelegramApiException;
import me.nizheg.telegram.bot.api.service.param.Message;
import me.nizheg.telegram.bot.chgk.domain.ChatGame;
import me.nizheg.telegram.bot.chgk.domain.HintResult;
import me.nizheg.telegram.bot.chgk.dto.Chat;
import me.nizheg.telegram.bot.chgk.dto.composite.Task;
import me.nizheg.telegram.bot.chgk.exception.GameException;
import me.nizheg.telegram.bot.chgk.exception.NoTaskException;
import me.nizheg.telegram.bot.chgk.service.ChatGameService;
import me.nizheg.telegram.bot.chgk.service.ChatService;
import me.nizheg.telegram.bot.chgk.util.AnswerSender;
import me.nizheg.telegram.bot.command.CommandContext;
import me.nizheg.telegram.bot.command.CommandException;

/**
 * @author Nikolay Zhegalin
 */
public class HintCommand extends ChatGameCommand {

    private final ChatService chatService;
    private final ChatGameService chatGameService;
    private final AnswerSender answerSender;

    public HintCommand(
            @NonNull TelegramApiClient telegramApiClient,
            @NonNull ChatService chatService,
            @NonNull ChatGameService chatGameService,
            @NonNull AnswerSender answerSender) {
        super(telegramApiClient);
        this.chatService = chatService;
        this.chatGameService = chatGameService;
        this.answerSender = answerSender;
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

    @Override
    protected ChatService getChatService() {
        return chatService;
    }

    @Override
    protected ChatGameService getChatGameService() {
        return chatGameService;
    }

    @Override
    protected void executeChatGame(CommandContext ctx, ChatGame chatGame) throws CommandException {
        Long chatId = ctx.getChatId();
        Long taskId = parseTaskId(ctx);
        try {
            HintResult hintForTask = chatGame.getHintForTask(new Chat(ctx.getFrom()), taskId);
            Task hintForTaskTask = hintForTask.getTask().orElseThrow(() -> new NoTaskException(chatId));
            sendAnswerToUser(ctx, hintForTaskTask);
        } catch (GameException e) {
            getTelegramApiClient().sendMessage(new Message("<i>" + e.getMessage() + "</i>", chatId, ParseMode.HTML));
        }
    }

    private void sendAnswerToUser(CommandContext ctx, Task currentTask) {
        try {
            answerSender.sendAnswer(currentTask, false, ctx.getFrom().getId());
        } catch (TelegramApiException ex) {
            getTelegramApiClient()
                    .sendMessage(new Message(
                            "<i>Не удалось отправить подсказку. Проверьте, что бот знает вас "
                                    + "(необходимо выполнить</i> /start <i>в личке с ним) и он не заблокирован.</i>",
                            ctx.getChatId(), ParseMode.HTML));
        }
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
}
