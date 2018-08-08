package me.nizheg.telegram.bot.chgk.command;

import java.util.function.Supplier;

import lombok.NonNull;
import me.nizheg.telegram.bot.api.model.ParseMode;
import me.nizheg.telegram.bot.api.service.TelegramApiClient;
import me.nizheg.telegram.bot.api.service.param.AnswerCallbackRequest;
import me.nizheg.telegram.bot.api.service.param.Message;
import me.nizheg.telegram.bot.chgk.domain.ChatGame;
import me.nizheg.telegram.bot.chgk.exception.CurrentTaskIsOtherException;
import me.nizheg.telegram.bot.chgk.exception.GameException;
import me.nizheg.telegram.bot.chgk.service.ChatGameService;
import me.nizheg.telegram.bot.chgk.service.ChatService;
import me.nizheg.telegram.bot.chgk.util.NextTaskSender;
import me.nizheg.telegram.bot.command.CommandContext;
import me.nizheg.telegram.bot.command.CommandException;

/**
 * @author Nikolay Zhegalin
 */
public class NextCommand extends ChatGameCommand {

    private static final String ATTRIBUTE_ILLEGAL_TASK_ID = "TASK_ID_IS_ILLEGAL";

    private final ChatService chatService;
    private final ChatGameService chatGameService;
    private final NextTaskSender nextTaskSender;

    public NextCommand(
            @NonNull TelegramApiClient telegramApiClient,
            @NonNull ChatService chatService,
            @NonNull ChatGameService chatGameService,
            @NonNull NextTaskSender nextTaskSender) {
        super(telegramApiClient);
        this.chatService = chatService;
        this.chatGameService = chatGameService;
        this.nextTaskSender = nextTaskSender;
    }

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
        Long currentTaskId = null;
        if (ctx.getCallbackQueryId() != null) {
            try {
                currentTaskId = Long.valueOf(ctx.getText());
            } catch (NumberFormatException ex) {
                //ignore
            }
        }
        try {
            nextTaskSender.sendNextTask(chatGame, currentTaskId);
        } catch (CurrentTaskIsOtherException e) {
            if (logger.isDebugEnabled()) {
                logger.debug("Current task is other from " + currentTaskId);
            }
            ctx.setAttribute(ATTRIBUTE_ILLEGAL_TASK_ID, true);
        } catch (GameException e) {
            throw new CommandException(new Message("<i>" + e.getMessage() + "</i>", ctx.getChatId(), ParseMode.HTML),
                    e);
        }
    }

    @Override
    public void sendCallbackResponse(CommandContext ctx) {
        AnswerCallbackRequest answerCallbackRequest = new AnswerCallbackRequest();
        answerCallbackRequest.setCallBackQueryId(ctx.getCallbackQueryId());
        Boolean isTaskIdIllegal = (Boolean) ctx.getAttribute(ATTRIBUTE_ILLEGAL_TASK_ID);
        if (isTaskIdIllegal != null && isTaskIdIllegal) {
            answerCallbackRequest.setText("Данная кнопка устарела. Воспользуйтесь актуальной или командой /next");
            answerCallbackRequest.setShowAlert(true);
        }
        getTelegramApiClient().answerCallbackQuery(answerCallbackRequest);
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
