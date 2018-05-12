package me.nizheg.telegram.bot.chgk.service.impl;

import com.googlecode.concurrentlinkedhashmap.ConcurrentLinkedHashMap;
import com.googlecode.concurrentlinkedhashmap.EvictionListener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.ConcurrentMap;

import javax.annotation.PostConstruct;

import me.nizheg.telegram.bot.chgk.domain.AutoChatGame;
import me.nizheg.telegram.bot.chgk.domain.ChatGame;
import me.nizheg.telegram.bot.chgk.domain.ChatGameFactory;
import me.nizheg.telegram.bot.chgk.dto.Chat;
import me.nizheg.telegram.bot.chgk.exception.DuplicationException;
import me.nizheg.telegram.bot.chgk.service.ChatGameService;
import me.nizheg.telegram.bot.chgk.service.ChatService;
import me.nizheg.telegram.bot.chgk.service.Properties;
import me.nizheg.telegram.bot.service.PropertyService;

@Service
public class ChatGameServiceImpl implements ChatGameService {

    static {
        int maxSize;
        try {
            maxSize = Integer.parseInt(System.getProperty("chat.cache.size", "500"));
        } catch (RuntimeException ex) {
            maxSize = 500;
        }
        MAX_SIZE = maxSize;
        int concurrencyLevel;
        try {
            concurrencyLevel = Integer.parseInt(System.getProperty("chat.cache.connections.count", "40"));
        } catch (RuntimeException ex) {
            concurrencyLevel = 40;
        }
        CONCURRENCY_LEVEL = concurrencyLevel;
    }

    private final static int MAX_SIZE;
    private final static int CONCURRENCY_LEVEL;
    private final Log logger = LogFactory.getLog(getClass());
    private final ConcurrentMap<Long, ChatGame> chatGames;
    private final PropertyService propertyService;
    private final ChatService chatService;
    private final ChatGameFactory chatGameFactory;

    public ChatGameServiceImpl(
            PropertyService propertyService,
            ChatService chatService, ChatGameFactory chatGameFactory) {
        this.propertyService = propertyService;
        this.chatService = chatService;
        this.chatGameFactory = chatGameFactory;
        EvictionListener<Long, ChatGame> evictionListener = (key, chatGame) -> {
            if (chatGame instanceof AutoChatGame) {
                ((AutoChatGame) chatGame).pause();
            }
        };
        chatGames = new ConcurrentLinkedHashMap.Builder<Long, ChatGame>()//
                .maximumWeightedCapacity(MAX_SIZE) //
                .concurrencyLevel(CONCURRENCY_LEVEL) //
                .listener(evictionListener) //
                .build();
    }

    @PostConstruct
    public void init() {
        List<Chat> scheduledChats = chatService.getChatsWithScheduledOperation();
        for (Chat scheduledChat : scheduledChats) {
            putInCache(scheduledChat);
        }
    }

    @Override
    public void setTimer(long chatId, int timeoutSeconds) {
        chatService.setTimer(chatId, timeoutSeconds);
        removeChatGameFromCache(chatId);
    }

    @Override
    public void clearTimer(long chatId) {
        chatService.clearTimer(chatId);
        removeChatGameFromCache(chatId);
    }

    @Override
    public void stopChatGame(long chatId) {
        removeChatGameFromCache(chatId);
    }

    private ChatGame removeChatGameFromCache(long chatId) {
        ChatGame removed = chatGames.remove(chatId);
        if (removed instanceof AutoChatGame) {
            ((AutoChatGame) removed).stop();
        }
        return removed;
    }


    @Override
    @Transactional
    public ChatGame getGame(final Chat chat) {
        long start = System.currentTimeMillis();
        long chatId = chat.getId();
        ChatGame chatGame = chatGames.get(chatId);
        if (chatGame == null) {
            if (logger.isTraceEnabled()) {
                logger.trace("Put in cache chat " + chatId);
            }
            try {
                chatService.createOrUpdate(chat);
            } catch (DuplicationException ex) {
                logger.error("Chat is created yet " + chat.getId(), ex);
            }
            chatGame = putInCache(chat);
        }
        if (logger.isTraceEnabled()) {
            logger.trace("Get game is executed in " + (System.currentTimeMillis() - start) + " ms for chat " + chatId);
        }
        return chatGame;
    }

    private ChatGame putInCache(Chat chat) {
        long chatId = chat.getId();
        ChatGame chatGame = createChatGame(chat);
        ChatGame previousValue = chatGames.putIfAbsent(chatId, chatGame);
        if (previousValue != null) {
            chatGame = previousValue;
        }
        return chatGame;
    }

    private ChatGame createChatGame(Chat chat) {
        Integer timeout = propertyService.getIntegerValueForChat(Properties.CHAT_TIMER, chat.getId());
        if (timeout != null && timeout > 0) {
            //return (AutoChatGame) applicationContext.getBean("autoChatGame", chat, timeout);
            return chatGameFactory.createAutoChatGame(chat, timeout);
        }
        //return (ChatGame) applicationContext.getBean("chatGame", chat);
        return chatGameFactory.createChatGame(chat);
    }
}
