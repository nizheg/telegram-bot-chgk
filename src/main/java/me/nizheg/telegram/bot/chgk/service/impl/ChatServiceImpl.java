package me.nizheg.telegram.bot.chgk.service.impl;

import org.ehcache.Cache;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.Date;
import java.util.List;

import lombok.RequiredArgsConstructor;
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
public class ChatServiceImpl implements ChatService {

    private final Cache<Long, Long> activeChatsCache;
    private final PropertyService propertyService;
    private final ChatDao chatDao;
    private final ChatErrorDao chatErrorDao;
    private final AnswerLogDao answerLogDao;
    private final ChatMappingDao chatMappingDao;
    private final TaskDao taskDao;
    private TransactionTemplate newTransaction;

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
    }

    @Override
    public void clearTimer(long chatId) {
        propertyService.setValueForChat(Properties.CHAT_TIMER, (Integer) null, chatId);
    }

    @Override
    public void deactivateChat(long chatId) {
        propertyService.setValueForChat(Properties.CHAT_ACTIVE_KEY, false, chatId);
        activeChatsCache.remove(chatId);
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
        taskDao.deleteUsedTasks(toChatId);
        taskDao.copyUsedTasks(fromChatId, toChatId);
        answerLogDao.deleteByChatId(toChatId);
        answerLogDao.copy(fromChatId, toChatId);
    }

    @Override
    public List<Chat> getChatsWithScheduledOperation() {
        return chatDao.getChatsWithScheduledOperation();
    }
}
