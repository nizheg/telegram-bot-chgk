package me.nizheg.telegram.bot.chgk.event;

import org.springframework.stereotype.Component;

import me.nizheg.telegram.bot.chgk.service.ChatGameService;
import me.nizheg.telegram.bot.chgk.service.ChatService;
import me.nizheg.telegram.bot.event.BotKickedEvent;
import me.nizheg.telegram.bot.event.ChatEvent;
import me.nizheg.telegram.bot.event.ChatEventListener;

/**
 * @author Nikolay Zhegalin
 */
@Component
public class BotKickedEventListener implements ChatEventListener {

    private final ChatService chatService;
    private final ChatGameService chatGameService;

    public BotKickedEventListener(
            ChatService chatService,
            ChatGameService chatGameService) {
        this.chatService = chatService;
        this.chatGameService = chatGameService;
    }

    @Override
    public boolean supports(Class<? extends ChatEvent> eventClass) {
        return BotKickedEvent.class.isAssignableFrom(eventClass);
    }

    @Override
    public void handleEvent(ChatEvent chatEvent) {
        if (!supports(chatEvent.getClass())) {
            return;
        }
        BotKickedEvent botKickedEvent = (BotKickedEvent) chatEvent;
        chatGameService.stopChatGame(botKickedEvent.getFromChatId());
        chatService.deactivateChat(botKickedEvent.getFromChatId());
    }
}
