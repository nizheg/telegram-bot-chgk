package me.nizheg.telegram.bot.chgk.service.impl;

import lombok.RequiredArgsConstructor;
import me.nizheg.telegram.bot.chgk.command.ChatActive;
import me.nizheg.telegram.bot.chgk.command.exception.BotIsNotStartedException;
import me.nizheg.telegram.bot.chgk.service.ChatService;
import me.nizheg.telegram.bot.command.ChatCommand;
import me.nizheg.telegram.bot.command.CommandContext;
import me.nizheg.telegram.bot.command.CommandException;
import me.nizheg.telegram.bot.service.impl.PreconditionChainStep;
import me.nizheg.telegram.bot.service.impl.PreconditionResult;

/**
 * @author Nikolay Zhegalin
 */
@RequiredArgsConstructor
public class CheckChatActive extends PreconditionChainStep {

    private final ChatService chatService;

    @Override
    protected PreconditionResult doCheck(ChatCommand commandHandler, CommandContext context) throws CommandException {
        boolean isSuccess = true;
        ChatActive chatActiveMarker;
        if (commandHandler != null &&
                (chatActiveMarker = commandHandler.getClass().getAnnotation(ChatActive.class)) != null) {
            isSuccess = chatService.isChatActive(context.getChatId());
            if (!isSuccess && chatActiveMarker.notifyUser()) {
                throw new BotIsNotStartedException();
            }
        }
        return new PreconditionResult(isSuccess, "Check chat active");
    }

}
