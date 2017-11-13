package me.nizheg.telegram.bot.chgk.service.impl;

import java.util.Calendar;
import java.util.Date;

import me.nizheg.telegram.bot.chgk.dto.FeedbackMessage;
import me.nizheg.telegram.bot.chgk.dto.FeedbackResult;
import me.nizheg.telegram.bot.chgk.dto.TelegramUser;
import me.nizheg.telegram.bot.chgk.repository.FeedbackMessageDao;
import me.nizheg.telegram.bot.chgk.service.FeedbackService;
import me.nizheg.telegram.bot.chgk.service.Properties;
import me.nizheg.telegram.bot.chgk.service.TelegramUserService;
import me.nizheg.telegram.bot.service.PropertyService;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.vk.VkApi;

/**
 * //todo add comments
 *
 * @author Nikolay Zhegalin
 */
@Service
public class FeedbackServiceImpl implements FeedbackService {

    private static final int MAX_LENGTH = 2000;
    private static final int MAX_NUMBER_OF_MESSAGES_PER_DAY = 100;
    private Log logger = LogFactory.getLog(getClass());
    @Autowired
    private TelegramUserService telegramUserService;
    @Autowired
    private FeedbackMessageDao feedbackMessageDao;
    @Autowired
    private PropertyService propertyService;
    @Autowired
    private VkApi vkApi;

    @Transactional
    @Override
    public FeedbackResult registerFeedback(TelegramUser telegramUser, String text) {
        telegramUser = telegramUserService.createOrUpdate(telegramUser);
        FeedbackResult feedbackResult = new FeedbackResult();
        if (feedbackMessageDao.countForUserFromDate(telegramUser.getId(), getStartOfDay()) > MAX_NUMBER_OF_MESSAGES_PER_DAY) {
            feedbackResult.setErrorDescription("Слишком много сообщений за день. Попробуйте позднее.");
            return feedbackResult;
        }
        FeedbackMessage feedbackMessage = new FeedbackMessage();
        feedbackMessage.setTime(new Date());
        feedbackMessage.setTelegramUserId(telegramUser.getId());
        // FIXME: divide into several messages
        feedbackMessage.setMessage(StringUtils.abbreviate(text, MAX_LENGTH));
        try {
            feedbackMessageDao.create(feedbackMessage);
        } catch (RuntimeException ex) {
            logger.error("Unable to save feedback " + text, ex);
            feedbackResult.setErrorDescription("Не удалось сохранить отзыв.");
            return feedbackResult;
        }
        Long vkGroupId = propertyService.getLongValue(Properties.VK_GROUP_ID);
        Long vkTopicId = propertyService.getLongValue(Properties.VK_TOPIC_ID);
        if (vkGroupId != null & vkTopicId != null) {
            String link =
                    vkApi.createCommentOnBoard(vkGroupId, vkTopicId, "Сообщение от " + telegramUser.getFirstname() + ":\n" + feedbackMessage.getMessage(), true);
            feedbackResult.setLink(link);
        }

        return feedbackResult;
    }

    private Date getStartOfDay() {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DATE);
        calendar.set(year, month, day, 0, 0, 0);
        return calendar.getTime();
    }
}
