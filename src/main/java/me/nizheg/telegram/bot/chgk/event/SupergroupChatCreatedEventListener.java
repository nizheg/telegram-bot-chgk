package me.nizheg.telegram.bot.chgk.event;

import org.springframework.stereotype.Component;

import me.nizheg.telegram.bot.chgk.service.ChatService;
import me.nizheg.telegram.bot.event.ChatEvent;
import me.nizheg.telegram.bot.event.ChatEventListener;
import me.nizheg.telegram.bot.event.SupergroupChatCreatedEvent;

/**

 *
 * @author Nikolay Zhegalin
 */
@Component
public class SupergroupChatCreatedEventListener implements ChatEventListener {

    private final ChatService chatService;

    public SupergroupChatCreatedEventListener(ChatService chatService) {
        this.chatService = chatService;
    }

    @Override
    public boolean supports(Class<? extends ChatEvent> eventClass) {
        return SupergroupChatCreatedEvent.class.isAssignableFrom(eventClass);
    }

    @Override
    public void handleEvent(ChatEvent chatEvent) {
        if (!supports(chatEvent.getClass())) {
            return;
        }
        SupergroupChatCreatedEvent supergroupChatCreatedEvent = (SupergroupChatCreatedEvent) chatEvent;
        chatService.handleGroupToSuperGroupConverting(supergroupChatCreatedEvent.getGroupId(),
                supergroupChatCreatedEvent.getSuperGroupId());
    }
}
