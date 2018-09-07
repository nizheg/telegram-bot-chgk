package me.nizheg.telegram.bot.chgk.service.impl;

import org.ehcache.Cache;
import org.ehcache.event.CacheEventListener;
import org.ehcache.event.EventFiring;
import org.ehcache.event.EventOrdering;
import org.ehcache.event.EventType;
import org.ehcache.impl.events.CacheEventAdapter;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import lombok.RequiredArgsConstructor;
import lombok.extern.apachecommons.CommonsLog;
import me.nizheg.telegram.bot.chgk.domain.AutoChatGame;
import me.nizheg.telegram.bot.chgk.domain.ChatGame;
import me.nizheg.telegram.bot.chgk.domain.ChatGameFactory;
import me.nizheg.telegram.bot.chgk.dto.Chat;
import me.nizheg.telegram.bot.chgk.exception.DuplicationException;
import me.nizheg.telegram.bot.chgk.service.ChatGameService;
import me.nizheg.telegram.bot.chgk.service.ChatService;
import me.nizheg.telegram.bot.chgk.service.Properties;
import me.nizheg.telegram.bot.service.PropertyService;

@CommonsLog
@RequiredArgsConstructor
public class ChatGameServiceImpl implements ChatGameService {

    private final Map<Long, AutoChatGame> autoChatGamesStorage = new ConcurrentHashMap<>();
    private final PropertyService propertyService;
    private final ChatService chatService;
    private final Cache<Long, ChatGame> chatGamesCache;
    private final ChatGameFactory chatGameFactory;
    private CacheEventListener<Long, ChatGame> cacheListener;

    @PostConstruct
    public void init() {
        cacheListener = new CacheListener();
        this.chatGamesCache.getRuntimeConfiguration().registerCacheEventListener(
                cacheListener, EventOrdering.UNORDERED, EventFiring.ASYNCHRONOUS,
                EventType.CREATED, EventType.UPDATED, EventType.EVICTED, EventType.REMOVED, EventType.EXPIRED);
        List<Chat> scheduledChats = chatService.getChatsWithScheduledOperation();
        for (Chat scheduledChat : scheduledChats) {
            putInCache(scheduledChat);
        }
    }

    @PreDestroy
    public void destroy() {
        this.chatGamesCache.getRuntimeConfiguration().deregisterCacheEventListener(cacheListener);
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

    private void removeChatGameFromCache(long chatId) {
        chatGamesCache.remove(chatId);
    }

    @Override
    @Transactional
    public ChatGame getGame(final Chat chat) {
        long start = System.nanoTime();
        long chatId = chat.getId();
        ChatGame chatGame = chatGamesCache.get(chatId);
        if (chatGame == null) {
            if (log.isDebugEnabled()) {
                log.debug("Put in cache chat " + chatId);
            }
            try {
                chatService.createOrUpdate(chat);
            } catch (DuplicationException ex) {
                log.error("Chat is created yet " + chat.getId(), ex);
            }
            chatGame = putInCache(chat);
        }
        if (log.isDebugEnabled()) {
            long elapsedTime = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start);
            log.debug("Get game is executed in " + elapsedTime + " ms for chat " + chatId);
        }
        return chatGame;
    }

    private ChatGame putInCache(Chat chat) {
        long chatId = chat.getId();
        ChatGame chatGame = autoChatGamesStorage.get(chatId);
        if (chatGame == null) {
            chatGame = createChatGame(chat);
        }
        ChatGame previousValue = chatGamesCache.putIfAbsent(chatId, chatGame);
        if (previousValue != null) {
            if (chatGame instanceof AutoChatGame) {
                ((AutoChatGame) chatGame).stop();
            }
            chatGame = previousValue;
        }
        return chatGame;
    }

    private ChatGame createChatGame(Chat chat) {
        Integer timeout = propertyService.getIntegerValueForChat(Properties.CHAT_TIMER, chat.getId());
        if (timeout != null && timeout > 0) {
            return chatGameFactory.createAutoChatGame(chat, timeout);
        }
        return chatGameFactory.createChatGame(chat);
    }

    private class CacheListener extends CacheEventAdapter<Long, ChatGame> {

        @Override
        protected void onCreation(Long key, ChatGame newValue) {
            if (log.isDebugEnabled()) {
                log.debug("Created new value in cache " + key + " " + newValue);
            }
            if (newValue instanceof AutoChatGame) {
                removeFromBackup(key);
            }
        }

        @Override
        protected void onUpdate(Long key, ChatGame oldValue, ChatGame newValue) {
            if (log.isDebugEnabled()) {
                log.debug("Updated value in cache " + key + " " + newValue);
            }
            if (newValue instanceof AutoChatGame) {
                removeFromBackup(key);
            }
        }

        private void removeFromBackup(Long key) {
            AutoChatGame removedFromStorage = autoChatGamesStorage.remove(key);
            if (log.isDebugEnabled()) {
                log.debug("Removed from storage " + removedFromStorage);
            }
        }

        @Override
        protected void onEviction(Long key, ChatGame evictedValue) {
            if (log.isDebugEnabled()) {
                log.debug("Evicted value in cache " + key + " " + evictedValue);
            }
            if (evictedValue instanceof AutoChatGame) {
                backupAutoChatGame(key, (AutoChatGame) evictedValue);
            }
        }

        @Override
        protected void onExpiry(Long key, ChatGame expiredValue) {
            if (log.isDebugEnabled()) {
                log.debug("Expired value in cache " + key + " " + expiredValue);
            }
            if (expiredValue instanceof AutoChatGame) {
                backupAutoChatGame(key, (AutoChatGame) expiredValue);
            }
        }

        private void backupAutoChatGame(Long key, AutoChatGame expiredValue) {
            AutoChatGame previousValue = autoChatGamesStorage.put(key, expiredValue);
            if (log.isDebugEnabled()) {
                log.debug("Put " + expiredValue + " in storage. Previous value " + previousValue);
            }
            if (previousValue != null) {
                previousValue.stop();
            }
        }

        @Override
        protected void onRemoval(Long key, ChatGame removedValue) {
            if (log.isDebugEnabled()) {
                log.debug("Removed value in cache " + key + " " + removedValue);
            }
            if (removedValue instanceof AutoChatGame) {
                AutoChatGame removedFromStorage = autoChatGamesStorage.remove(key);
                if (log.isDebugEnabled()) {
                    log.debug("Removed from storage " + removedFromStorage);
                }
                if (removedFromStorage != null) {
                    removedFromStorage.stop();
                }
            }
        }
    }
}
