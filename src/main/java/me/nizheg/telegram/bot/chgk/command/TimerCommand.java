package me.nizheg.telegram.bot.chgk.command;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.springframework.stereotype.Component;

import java.util.function.Supplier;

import javax.annotation.Nonnull;

import me.nizheg.telegram.bot.api.model.ChatMemberStatus;
import me.nizheg.telegram.bot.api.model.ParseMode;
import me.nizheg.telegram.bot.api.service.TelegramApiClient;
import me.nizheg.telegram.bot.api.service.param.ChatId;
import me.nizheg.telegram.bot.api.service.param.Message;
import me.nizheg.telegram.bot.chgk.service.ChatGameService;
import me.nizheg.telegram.bot.command.ChatCommand;
import me.nizheg.telegram.bot.command.CommandContext;
import me.nizheg.telegram.bot.command.CommandException;
import me.nizheg.telegram.bot.starter.service.preconditions.Permission;
import me.nizheg.telegram.util.Emoji;

/**
 * @author Nikolay Zhegalin
 */
@UserInChannel
@Permission(chatMemberStatuses = {ChatMemberStatus.ADMINISTRATOR, ChatMemberStatus.CREATOR},
        failOnUnsatisfied = true,
        description = "Только администраторы имеют право управлять таймером")
@Component
public class TimerCommand extends ChatCommand {

    private final static String COMMAND_NAME_TIMER_SET = "timer";
    private final static String COMMAND_NAME_TIMER_RESET = "timer_stop";
    private static final int MINUTE = 60;
    private static final int DEFAULT_TIMEOUT_MINUTES = 1;
    private static final int MAX_TIMEOUT_MINUTES = 24 * 60;

    private final ChatGameService chatGameService;

    public TimerCommand(
            @Nonnull Supplier<TelegramApiClient> telegramApiClientSupplier,
            @Nonnull ChatGameService chatGameService) {
        super(telegramApiClientSupplier);
        Validate.notNull(chatGameService, "chatGameService should be defined");
        this.chatGameService = chatGameService;
    }

    @Override
    public int getPriority() {
        return 110;
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
        getTelegramApiClient().sendMessage(Message.safeMessageBuilder()
                .text(Emoji.BELL +
                        " <i>Установлен таймер автоматической выдачи вопросов в " + timeoutMinutes + " мин.</i>")
                .chatId(new ChatId(ctx.getChatId()))
                .parseMode(ParseMode.HTML)
                .build());
    }

    private void resetTimer(CommandContext ctx) {
        chatGameService.clearTimer(ctx.getChatId());
        getTelegramApiClient().sendMessage(Message.safeMessageBuilder()
                .text(Emoji.BELL_WITH_CANCELLATION_STROKE + " <i>Автоматическая выдача вопросов выключена</i>")
                .chatId(new ChatId(ctx.getChatId()))
                .parseMode(ParseMode.HTML)
                .build());
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
