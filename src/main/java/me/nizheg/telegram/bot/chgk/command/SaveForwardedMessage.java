package me.nizheg.telegram.bot.chgk.command;

import org.springframework.stereotype.Component;

import java.util.Optional;

import javax.annotation.Nonnull;

import me.nizheg.telegram.bot.api.model.Message;
import me.nizheg.telegram.bot.chgk.dto.ForwardingMessage;
import me.nizheg.telegram.bot.chgk.dto.Role;
import me.nizheg.telegram.bot.chgk.service.MessageService;
import me.nizheg.telegram.bot.chgk.service.TelegramUserService;
import me.nizheg.telegram.bot.command.CommandContext;
import me.nizheg.telegram.bot.command.NonCommandMessageProcessor;

@Component
public class SaveForwardedMessage implements NonCommandMessageProcessor {

    private final TelegramUserService telegramUserService;
    private final MessageService messageService;

    public SaveForwardedMessage(
            TelegramUserService telegramUserService,
            MessageService messageService) {
        this.telegramUserService = telegramUserService;
        this.messageService = messageService;
    }

    @Override
    public void process(CommandContext ctx) {
        Optional.ofNullable(ctx.getMessage())
                .filter(message -> message.getForwardFromChat() != null)
                .filter(message -> telegramUserService.userHasRole(ctx.getFrom().getId(), Role.SUPER_ADMIN))
                .ifPresent(this::saveForwardedMessage);
    }

    private void saveForwardedMessage(@Nonnull Message message) {
        ForwardingMessage forwardingMessage = new ForwardingMessage();
        forwardingMessage.setFromChatId(message.getForwardFromChat().getId());
        forwardingMessage.setMessageId(message.getForwardFromMessageId());
        forwardingMessage.setText(message.getText());
        messageService.setMessageForForwarding(forwardingMessage);
    }
}
