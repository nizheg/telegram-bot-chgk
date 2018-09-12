package me.nizheg.telegram.bot.chgk.command;

import java.util.Optional;

import javax.annotation.Nonnull;

import lombok.RequiredArgsConstructor;
import me.nizheg.telegram.bot.api.model.Message;
import me.nizheg.telegram.bot.chgk.dto.Role;
import me.nizheg.telegram.bot.chgk.service.MessageService;
import me.nizheg.telegram.bot.chgk.service.TelegramUserService;
import me.nizheg.telegram.bot.chgk.work.data.ForwardMessageData;
import me.nizheg.telegram.bot.command.CommandContext;
import me.nizheg.telegram.bot.command.NonCommandMessageProcessor;

@RequiredArgsConstructor
public class SaveForwardedMessage implements NonCommandMessageProcessor {

    private final TelegramUserService telegramUserService;
    private final MessageService messageService;

    @Override
    public void process(CommandContext ctx) {
        Optional.ofNullable(ctx.getMessage())
                .filter(message -> ctx.getFrom() != null)
                .filter(message -> message.getForwardFromChat() != null)
                .filter(message -> telegramUserService.userHasRole(ctx.getFrom().getId(), Role.SUPER_ADMIN))
                .ifPresent(message -> {
                    this.saveForwardedMessage(message);
                    ctx.setText(null);
                });
    }

    private void saveForwardedMessage(@Nonnull Message message) {
        ForwardMessageData forwardMessageData = new ForwardMessageData();
        forwardMessageData.setFromChatId(message.getForwardFromChat().getId());
        forwardMessageData.setMessageId(message.getForwardFromMessageId());
        forwardMessageData.setText(message.getText());
        messageService.setMessageForForwarding(forwardMessageData);
    }
}
