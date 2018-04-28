package me.nizheg.telegram.bot.chgk.event;

import org.springframework.stereotype.Component;

import me.nizheg.telegram.bot.chgk.service.ChatService;
import me.nizheg.telegram.bot.event.ChatEventListener;
import me.nizheg.telegram.bot.event.Events;
import me.nizheg.telegram.bot.event.SupergroupChatCreatedEvent;

/**
 * //todo add comments
 *
 * @author Nikolay Zhegalin
 */
@Component
public class SupergroupChatCreatedEventListener implements ChatEventListener<SupergroupChatCreatedEvent> {

    private final ChatService chatService;

    public SupergroupChatCreatedEventListener(ChatService chatService) {
        this.chatService = chatService;
    }

    @Override
    public String getListeningEventId() {
        return Events.SUPER_GROUP_CREATED_ID;
    }

    @Override
    public Class<SupergroupChatCreatedEvent> getListeningEventClass() {
        return SupergroupChatCreatedEvent.class;
    }

    @Override
    public void handleEvent(SupergroupChatCreatedEvent chatEvent) {
        chatService.handleGroupToSuperGroupConverting(chatEvent.getGroupId(), chatEvent.getSuperGroupId());
    }
}
