package me.nizheg.telegram.bot.chgk.command;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;

import java.util.function.Supplier;

import javax.annotation.Nonnull;

import me.nizheg.telegram.bot.api.model.ParseMode;
import me.nizheg.telegram.bot.api.service.TelegramApiClient;
import me.nizheg.telegram.bot.api.service.param.Message;
import me.nizheg.telegram.bot.chgk.service.ChatGameService;
import me.nizheg.telegram.bot.command.ChatCommand;
import me.nizheg.telegram.bot.command.CommandContext;
import me.nizheg.telegram.bot.command.CommandException;
import me.nizheg.telegram.util.Emoji;

/**
 * @author Nikolay Zhegalin
 */
@UserInChannel
public class TimerCommand extends ChatCommand {

    private final static String COMMAND_NAME_TIMER_SET = "timer";
    private final static String COMMAND_NAME_TIMER_RESET = "timer_stop";
    private static final int MINUTE = 60;
    private static final int DEFAULT_TIMEOUT_MINUTES = 1;
    private static final int MAX_TIMEOUT_MINUTES = 24 * 60;

    private final ChatGameService chatGameService;

    public TimerCommand(
            @Nonnull TelegramApiClient telegramApiClient,
            @Nonnull ChatGameService chatGameService) {
        super(telegramApiClient);
        Validate.notNull(chatGameService, "chatGameService should be defined");
        this.chatGameService = chatGameService;
    }

    public TimerCommand(
            @Nonnull Supplier<TelegramApiClient> telegramApiClientSupplier,
            @Nonnull ChatGameService chatGameService) {
        super(telegramApiClientSupplier);
        Validate.notNull(chatGameService, "chatGameService should be defined");
        this.chatGameService = chatGameService;
    }

    @Override
    public void execute(CommandContext ctx) throws CommandException {
        if (COMMAND_NAME_TIMER_SET.equals(ctx.getCommand())) {
            setTimer(ctx);
        } else if (COMMAND_NAME_TIMER_RESET.equals(ctx.getCommand())) {
            resetTimer(ctx);
        }
    }

    private void setTimer(CommandContext ctx) throws CommandException {
        String option = ctx.getText();
        int timeoutMinutes = DEFAULT_TIMEOUT_MINUTES;
        if (StringUtils.isNotBlank(option)) {
            boolean isValueCorrect = true;
            try {
                timeoutMinutes = Integer.parseInt(option);
            } catch (NumberFormatException ex) {
                isValueCorrect = false;
            }
            if (!isValueCorrect || timeoutMinutes < 0 || timeoutMinutes > MAX_TIMEOUT_MINUTES) {
                throw new CommandException("Введите в качестве значения целое число не больше " + MAX_TIMEOUT_MINUTES);
            }
        }
        chatGameService.setTimer(ctx.getChatId(), timeoutMinutes * MINUTE);
        getTelegramApiClient().sendMessage(new Message(
                Emoji.BELL + " <i>Установлен таймер автоматической выдачи вопросов в " + timeoutMinutes + " мин.</i>",
                ctx.getChatId(), ParseMode.HTML));
    }

    private void resetTimer(CommandContext ctx) {
        chatGameService.clearTimer(ctx.getChatId());
        getTelegramApiClient().sendMessage(
                new Message(Emoji.BELL_WITH_CANCELLATION_STROKE + " <i>Автоматическая выдача вопросов выключена</i>",
                        ctx.getChatId(),
                        ParseMode.HTML, false));
    }

    @Override
    public String getCommandName() {
        return "timer(_stop)?";
    }

    @Override
    public String getDescription() {
        return "/" + COMMAND_NAME_TIMER_SET
                + " <количество минут> - запустить таймер (по умолчанию количество минут = 1)\n" //
                + "/" + COMMAND_NAME_TIMER_RESET + " - выключить автоматическую выдачу вопросов";
    }
}
