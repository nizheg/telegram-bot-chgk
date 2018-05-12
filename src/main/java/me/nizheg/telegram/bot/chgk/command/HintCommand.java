package me.nizheg.telegram.bot.chgk.command;

import java.util.function.Supplier;

import me.nizheg.telegram.bot.api.model.ParseMode;
import me.nizheg.telegram.bot.api.service.TelegramApiClient;
import me.nizheg.telegram.bot.api.service.TelegramApiException;
import me.nizheg.telegram.bot.api.service.param.Message;
import me.nizheg.telegram.bot.chgk.domain.ChatGame;
import me.nizheg.telegram.bot.chgk.domain.HintResult;
import me.nizheg.telegram.bot.chgk.dto.Chat;
import me.nizheg.telegram.bot.chgk.dto.composite.Task;
import me.nizheg.telegram.bot.chgk.exception.NoTaskException;
import me.nizheg.telegram.bot.chgk.service.ChatService;
import me.nizheg.telegram.bot.chgk.util.AnswerSender;
import me.nizheg.telegram.bot.command.CommandContext;
import me.nizheg.telegram.bot.command.CommandException;

/**
 * @author Nikolay Zhegalin
 */
public class HintCommand extends ChatGameCommand {

    private final ChatService chatService;
    private final AnswerSender answerSender;

    public HintCommand(
            TelegramApiClient telegramApiClient,
            ChatService chatService,
            AnswerSender answerSender) {
        super(telegramApiClient);
        this.chatService = chatService;
        this.answerSender = answerSender;
    }

    public HintCommand(
            Supplier<TelegramApiClient> telegramApiClientSupplier,
            ChatService chatService, AnswerSender answerSender) {
        super(telegramApiClientSupplier);
        this.chatService = chatService;
        this.answerSender = answerSender;
    }

    @Override
    protected ChatService getChatService() {
        return chatService;
    }

    @Override
    protected void executeChatGame(CommandContext ctx, ChatGame chatGame) throws CommandException {
        Long chatId = ctx.getChatId();
        Long taskId = parseTaskId(ctx);
        HintResult hintForTask = chatGame.getHintForTask(new Chat(ctx.getFrom()), taskId);
        if (hintForTask.getTask() != null) {
            sendAnswerToUser(ctx, hintForTask.getTask());
        } else {
            throw new NoTaskException(chatId);
        }
    }

    private void sendAnswerToUser(CommandContext ctx, Task currentTask) {
        try {
            StringBuilder messageBuilder = new StringBuilder("<b>Ответ:</b>\n");
            answerSender.sendAnswerOfTask(messageBuilder, currentTask, ctx.getFrom().getId(), null);
        } catch (TelegramApiException ex) {
            getTelegramApiClient()
                    .sendMessage(new Message(
                            "<i>Не удалось отправить подсказку. Проверьте, что бот знает вас (необходимо выполнить</i> /start <i>в личке с ним) и он не заблокирован.</i>",
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
