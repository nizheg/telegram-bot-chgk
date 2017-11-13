package me.nizheg.telegram.bot.chgk.event;

import me.nizheg.telegram.bot.chgk.service.ChatService;
import me.nizheg.telegram.bot.event.ChatEventListener;
import me.nizheg.telegram.bot.event.Events;
import me.nizheg.telegram.bot.event.SupergroupChatCreatedEvent;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * //todo add comments
 *
 * @author Nikolay Zhegalin
 */
@Component
public class SupergroupChatCreatedEventListener implements ChatEventListener<SupergroupChatCreatedEvent> {
    @Autowired
    private ChatService chatService;

    @Override
    public String getListeningEventId() {
        return Events.SUPER_GROUP_CREATED_ID;
    }

    @Override
    public void handleEvent(SupergroupChatCreatedEvent chatEvent) {
        chatService.handleGroupToSuperGroupConverting(chatEvent.getGroupId(), chatEvent.getSuperGroupId());
    }
}
