package me.nizheg.telegram.bot.chgk.command;

import java.util.function.Supplier;

import lombok.NonNull;
import me.nizheg.telegram.bot.api.service.TelegramApiClient;
import me.nizheg.telegram.bot.chgk.domain.ChatGame;
import me.nizheg.telegram.bot.chgk.dto.Chat;
import me.nizheg.telegram.bot.chgk.exception.CurrentTaskIsOtherException;
import me.nizheg.telegram.bot.chgk.exception.GameException;
import me.nizheg.telegram.bot.chgk.service.ChatGameService;
import me.nizheg.telegram.bot.chgk.service.ChatService;
import me.nizheg.telegram.bot.chgk.util.NextTaskSender;
import me.nizheg.telegram.bot.command.ChatCommand;
import me.nizheg.telegram.bot.command.CommandContext;
import me.nizheg.telegram.bot.command.CommandException;

/**
 * @author Nikolay Zhegalin
 */
@UserInChannel
@ChatActive
public class NextCommand extends ChatCommand {

    private final ChatService chatService;
    private final ChatGameService chatGameService;
    private final NextTaskSender nextTaskSender;

    public NextCommand(
            @NonNull Supplier<TelegramApiClient> telegramApiClientSupplier,
            @NonNull ChatService chatService,
            @NonNull ChatGameService chatGameService,
            @NonNull NextTaskSender nextTaskSender) {
        super(telegramApiClientSupplier);
        this.chatService = chatService;
        this.chatGameService = chatGameService;
        this.nextTaskSender = nextTaskSender;
    }

    protected ChatService getChatService() {
        return chatService;
    }

    protected ChatGameService getChatGameService() {
        return chatGameService;
    }

    @Override
    public void execute(CommandContext ctx) throws CommandException {
        Long currentTaskId = null;
        if (ctx.isCallbackQuery()) {
            try {
                currentTaskId = Long.valueOf(ctx.getText());
            } catch (NumberFormatException ex) {
                //ignore
            }
        }
        try {
            TelegramApiClient telegramApiClient = getTelegramApiClient();
            ChatGame chatGame = chatGameService.getGame(new Chat(ctx.getChat()));
            nextTaskSender.sendNextTask(chatGame, currentTaskId, new CallbackRequestDefaultCallback<>(ctx,
                    telegramApiClient));
        } catch (CurrentTaskIsOtherException e) {
            throw new CommandException("Данная кнопка устарела. Воспользуйтесь актуальной или командой /next", e);
        } catch (GameException e) {
            throw new CommandException(e.getMessage(), e);
        }
    }

    @Override
    public void sendCallbackResponse(CommandContext ctx) {
    }

    @Override
    public String getCommandName() {
        return "next";
    }

    @Override
    public String getDescription() {
        return "/next - получить следующий вопрос";
    }
}
