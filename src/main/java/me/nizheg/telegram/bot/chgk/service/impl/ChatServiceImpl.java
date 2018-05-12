package me.nizheg.telegram.bot.chgk.service.impl;

import com.googlecode.concurrentlinkedhashmap.ConcurrentLinkedHashMap;
import com.googlecode.concurrentlinkedhashmap.EvictionListener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.Date;
import java.util.List;
import java.util.concurrent.ConcurrentMap;

import javax.annotation.Nonnull;
import javax.annotation.PostConstruct;

import me.nizheg.telegram.bot.chgk.domain.AutoChatGame;
import me.nizheg.telegram.bot.chgk.domain.ChatGame;
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
public class ChatServiceImpl implements ChatService {

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
    private final ConcurrentMap<Long, ChatGame> chats;
    private final ConcurrentMap<Long, Long> activeChatsCache;
    private final Log logger = LogFactory.getLog(getClass());
    private final PropertyService propertyService;
    private final ChatDao chatDao;
    private final ChatErrorDao chatErrorDao;
    private final AnswerLogDao answerLogDao;
    private final ChatMappingDao chatMappingDao;
    private final TaskDao taskDao;
    private TransactionTemplate newTransaction;
    private final ApplicationContext applicationContext;

    public ChatServiceImpl(
            PropertyService propertyService,
            ChatDao chatDao,
            ChatErrorDao chatErrorDao,
            AnswerLogDao answerLogDao,
            ChatMappingDao chatMappingDao,
            TaskDao taskDao,
            ApplicationContext applicationContext) {
        this.propertyService = propertyService;
        this.chatDao = chatDao;
        this.chatErrorDao = chatErrorDao;
        this.answerLogDao = answerLogDao;
        this.chatMappingDao = chatMappingDao;
        this.taskDao = taskDao;
        this.applicationContext = applicationContext;
        EvictionListener<Long, ChatGame> evictionListener = (key, chatGame) -> {
            if (chatGame instanceof AutoChatGame) {
                ((AutoChatGame) chatGame).pause();
            }
        };
        chats = new ConcurrentLinkedHashMap.Builder<Long, ChatGame>()//
                .maximumWeightedCapacity(MAX_SIZE) //
                .concurrencyLevel(CONCURRENCY_LEVEL) //
                .listener(evictionListener) //
                .build();
        activeChatsCache = new ConcurrentLinkedHashMap.Builder<Long, Long>()//
                .maximumWeightedCapacity(MAX_SIZE) //
                .build();
    }

    @PostConstruct
    public void init() {
        List<Chat> scheduledChats = chatDao.getChatsWithScheduledOperation();
        for (Chat scheduledChat : scheduledChats) {
            putInCache(scheduledChat);
        }
    }

    @Autowired
    public void setTransactionPlatformManager(PlatformTransactionManager txManager) {
        newTransaction = new TransactionTemplate(txManager);
        newTransaction.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
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
    public void setTimer(long chatId, int timeoutSeconds) {
        propertyService.setValueForChat(Properties.CHAT_TIMER, timeoutSeconds, chatId);
        removeChatFromCache(chatId);
    }

    @Override
    public void clearTimer(long chatId) {
        propertyService.setValueForChat(Properties.CHAT_TIMER, (Integer) null, chatId);
        removeChatFromCache(chatId);
    }

    @Override
    public void deactivateChat(long chatId) {
        propertyService.setValueForChat(Properties.CHAT_ACTIVE_KEY, false, chatId);
        removeChatFromCache(chatId);
        activeChatsCache.remove(chatId);
    }

    private ChatGame removeChatFromCache(long chatId) {
        ChatGame removed = chats.remove(chatId);
        if (removed instanceof AutoChatGame) {
            ((AutoChatGame) removed).stop();
        }
        return removed;
    }

    @Override
    public ChatError storeChatError(ChatError chatError) {
        if (chatError.getTime() == null) {
            chatError.setTime(new Date());
        }
        return chatErrorDao.create(chatError);
    }

    @Override
    @Transactional
    public void handleGroupToSuperGroupConverting(final Long groupId, final Long superGroupId) {
        newTransaction.execute(new TransactionCallbackWithoutResult() {
            @Override
            protected void doInTransactionWithoutResult(@Nonnull TransactionStatus transactionStatus) {
                ChatMapping chatMapping = new ChatMapping();
                chatMapping.setGroupId(groupId);
                chatMapping.setSuperGroupId(superGroupId);
                chatMappingDao.create(chatMapping);
            }
        });
        propertyService.copyProperties(groupId, superGroupId);
        taskDao.copyUsedTasks(groupId, superGroupId);
        answerLogDao.copy(groupId, superGroupId);
    }

    @Override
    @Transactional
    public ChatGame getGame(final Chat chat) {
        long start = System.currentTimeMillis();
        long chatId = chat.getId();
        ChatGame chatGame = chats.get(chatId);
        if (chatGame == null) {
            if (logger.isTraceEnabled()) {
                logger.trace("Put in cache chat " + chatId);
            }
            try {
                newTransaction.execute(new TransactionCallbackWithoutResult() {
                    @Override
                    protected void doInTransactionWithoutResult(@Nonnull  TransactionStatus transactionStatus) {
                        createOrUpdate(chat);
                    }
                });
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
        ChatGame previousValue = chats.putIfAbsent(chatId, chatGame);
        if (previousValue != null) {
            chatGame = previousValue;
        }
        return chatGame;
    }

    private ChatGame createChatGame(Chat chat) {
        Integer timeout = propertyService.getIntegerValueForChat(Properties.CHAT_TIMER, chat.getId());
        if (timeout != null && timeout > 0) {
            return (AutoChatGame) applicationContext.getBean("autoChatGame", chat, timeout);
        }
        return (ChatGame) applicationContext.getBean("chatGame", chat);
    }
}
