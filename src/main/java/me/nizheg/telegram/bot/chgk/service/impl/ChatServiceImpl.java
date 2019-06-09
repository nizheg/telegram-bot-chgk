package me.nizheg.telegram.bot.chgk.service.impl;

import org.ehcache.Cache;
import org.ehcache.event.CacheEventListener;
import org.ehcache.event.EventFiring;
import org.ehcache.event.EventOrdering;
import org.ehcache.event.EventType;
import org.ehcache.impl.events.CacheEventAdapter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.Date;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import lombok.RequiredArgsConstructor;
import lombok.extern.apachecommons.CommonsLog;
import me.nizheg.telegram.bot.chgk.domain.ChatSettings;
import me.nizheg.telegram.bot.chgk.dto.Chat;
import me.nizheg.telegram.bot.chgk.dto.ChatError;
import me.nizheg.telegram.bot.chgk.dto.ChatMapping;
import me.nizheg.telegram.bot.chgk.exception.DuplicationException;
import me.nizheg.telegram.bot.chgk.repository.AnswerLogDao;
import me.nizheg.telegram.bot.chgk.repository.ChatDao;
import me.nizheg.telegram.bot.chgk.repository.ChatErrorDao;
import me.nizheg.telegram.bot.chgk.repository.ChatMappingDao;
import me.nizheg.telegram.bot.chgk.repository.TaskDao;
import me.nizheg.telegram.bot.chgk.service.ChatService;
import me.nizheg.telegram.bot.chgk.service.Properties;
import me.nizheg.telegram.bot.service.PropertyService;

/**
 * @author Nikolay Zhegalin
 */
@Service
@RequiredArgsConstructor
@CommonsLog
public class ChatServiceImpl implements ChatService {

    private final Cache<Long, Long> activeChatsCache;
    private final PropertyService propertyService;
    private final ChatDao chatDao;
    private final ChatErrorDao chatErrorDao;
    private final AnswerLogDao answerLogDao;
    private final ChatMappingDao chatMappingDao;
    private final TaskDao taskDao;
    private CacheEventListener<Long, Long> activeChatsCacheListener;
    private TransactionTemplate newTransaction;

    private final ConcurrentMap<Long, ChatSettings> chatSettings = new ConcurrentHashMap<>();

    @Autowired
    public void setTransactionPlatformManager(PlatformTransactionManager txManager) {
        newTransaction = new TransactionTemplate(txManager);
        newTransaction.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
    }

    @PostConstruct
    public void init() {
        activeChatsCacheListener = new CacheListener();
        this.activeChatsCache.getRuntimeConfiguration().registerCacheEventListener(
                activeChatsCacheListener, EventOrdering.UNORDERED, EventFiring.ASYNCHRONOUS,
                EventType.CREATED, EventType.UPDATED, EventType.EVICTED, EventType.REMOVED, EventType.EXPIRED);
    }

    @PreDestroy
    public void destroy() {
        this.activeChatsCache.getRuntimeConfiguration().deregisterCacheEventListener(activeChatsCacheListener);
    }

    @Override
    public ChatSettings getSettings(long chatId) {
        ChatSettings givenChatSettings = chatSettings.get(chatId);
        if (givenChatSettings == null) {
            if (log.isDebugEnabled()) {
                log.debug("Initialize settings for chat " + chatId);
            }
            ChatSettings newChatSettings = new ChatSettings(propertyService, chatId);
            givenChatSettings = chatSettings.putIfAbsent(chatId, newChatSettings);
            if (givenChatSettings == null) {
                givenChatSettings = newChatSettings;
            }
        }
        return givenChatSettings;
    }

    @Override
    public Chat getChat(long chatId) {
        return chatDao.read(chatId);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Override
    public Chat createOrUpdate(Chat chat) throws DuplicationException {
        if (chatDao.isExist(chat.getId())) {
            return chatDao.update(chat);
        } else {
            return chatDao.create(chat);
        }
    }

    @Override
    public List<Long> getActiveChats() {
        return propertyService.readChatIdsByKeyAndValue(Properties.CHAT_ACTIVE_KEY, Boolean.TRUE.toString());
    }

    @Override
    public boolean isChatActive(long chatId) {
        boolean isActive = activeChatsCache.containsKey(chatId);
        if (isActive) {
            return true;
        }
        Boolean value = propertyService.getBooleanValueForChat(Properties.CHAT_ACTIVE_KEY, chatId);
        isActive = value != null && value;
        if (isActive) {
            activeChatsCache.put(chatId, chatId);
        }
        return isActive;
    }

    @Override
    public void activateChat(long chatId) {
        propertyService.setValueForChat(Properties.CHAT_ACTIVE_KEY, true, chatId);
        activeChatsCache.put(chatId, chatId);
    }

    @Override
    public void deactivateChat(long chatId) {
        propertyService.setValueForChat(Properties.CHAT_ACTIVE_KEY, false, chatId);
        activeChatsCache.remove(chatId);
    }

    @Override
    public void setTimer(long chatId, int timeoutSeconds) {
        propertyService.setValueForChat(Properties.CHAT_TIMER, timeoutSeconds, chatId);
    }

    @Override
    public void clearTimer(long chatId) {
        propertyService.setValueForChat(Properties.CHAT_TIMER, (Integer) null, chatId);
    }

    @Override
    public ChatError storeChatError(ChatError chatError) {
        if (chatError.getTime() == null) {
            chatError.setTime(new Date());
        }
        return chatErrorDao.create(chatError);
    }

    @Override
    public void createMappingToSupergroup(final Long groupId, final Long superGroupId) {
        ChatMapping chatMapping = new ChatMapping();
        chatMapping.setGroupId(groupId);
        chatMapping.setSuperGroupId(superGroupId);
        chatMappingDao.create(chatMapping);
    }

    @Override
    @Transactional
    public void migrateChatToAnother(final Long fromChatId, final Long toChatId) {
        propertyService.deletePropertiesForChat(toChatId);
        propertyService.copyProperties(fromChatId, toChatId);
        chatSettings.remove(fromChatId);
        chatSettings.remove(toChatId);
        taskDao.deleteUsedTasks(toChatId);
        taskDao.copyUsedTasks(fromChatId, toChatId);
        answerLogDao.deleteByChatId(toChatId);
        answerLogDao.copy(fromChatId, toChatId);
    }

    @Override
    public List<Chat> getChatsWithScheduledOperation() {
        return chatDao.getChatsWithScheduledOperation();
    }

    private class CacheListener extends CacheEventAdapter<Long, Long> {

        @Override
        protected void onCreation(Long key, Long newValue) {
            if (log.isDebugEnabled()) {
                log.debug("Created new value in cache " + key + " " + newValue);
            }
            cleanSettingsCacheForChat(key);
        }

        @Override
        protected void onUpdate(Long key, Long oldValue, Long newValue) {
            if (log.isDebugEnabled()) {
                log.debug("Updated value in cache " + key + " " + newValue);
            }
            cleanSettingsCacheForChat(key);
        }

        @Override
        protected void onEviction(Long key, Long evictedValue) {
            if (log.isDebugEnabled()) {
                log.debug("Evicted value in cache " + key + " " + evictedValue);
            }
            cleanSettingsCacheForChat(key);
        }

        @Override
        protected void onExpiry(Long key, Long expiredValue) {
            if (log.isDebugEnabled()) {
                log.debug("Expired value in cache " + key + " " + expiredValue);
            }
            cleanSettingsCacheForChat(key);
        }

        @Override
        protected void onRemoval(Long key, Long removedValue) {
            if (log.isDebugEnabled()) {
                log.debug("Removed value in cache " + key + " " + removedValue);
            }
            cleanSettingsCacheForChat(key);
        }

        private ChatSettings cleanSettingsCacheForChat(Long key) {
            return chatSettings.remove(key);
        }
    }
}
