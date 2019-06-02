package me.nizheg.telegram.bot.chgk.config;

import org.ehcache.Cache;
import org.ehcache.CacheManager;
import org.ehcache.config.builders.CacheConfigurationBuilder;
import org.ehcache.config.builders.CacheManagerBuilder;
import org.ehcache.config.builders.ExpiryPolicyBuilder;
import org.ehcache.config.builders.ResourcePoolsBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Scope;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import java.time.Clock;
import java.time.Duration;
import java.util.function.Supplier;

import javax.annotation.Nonnull;

import me.nizheg.telegram.bot.api.service.TelegramApiClient;
import me.nizheg.telegram.bot.chgk.domain.AutoChatGame;
import me.nizheg.telegram.bot.chgk.domain.ChatGame;
import me.nizheg.telegram.bot.chgk.domain.ChatGameFactory;
import me.nizheg.telegram.bot.chgk.dto.Chat;
import me.nizheg.telegram.bot.chgk.service.AnswerLogService;
import me.nizheg.telegram.bot.chgk.service.CategoryService;
import me.nizheg.telegram.bot.chgk.service.ChatService;
import me.nizheg.telegram.bot.chgk.service.ScheduledOperationService;
import me.nizheg.telegram.bot.chgk.service.TaskService;
import me.nizheg.telegram.bot.chgk.service.TelegramUserService;
import me.nizheg.telegram.bot.chgk.service.TourService;
import me.nizheg.telegram.bot.chgk.service.impl.CheckChatActive;
import me.nizheg.telegram.bot.chgk.service.impl.CheckMessageNotBlank;
import me.nizheg.telegram.bot.chgk.service.impl.CheckUserInChannel;
import me.nizheg.telegram.bot.chgk.util.AnswerSender;
import me.nizheg.telegram.bot.chgk.util.WarningSender;
import me.nizheg.telegram.bot.chgk.work.WorkConfig;
import me.nizheg.telegram.bot.service.PropertyService;
import me.nizheg.telegram.bot.starter.config.AppConfig;
import me.nizheg.telegram.bot.starter.util.BotInfo;

/**
 * @author Nikolay Zhegalin
 */
@Configuration
@Import({AppConfig.class, WorkConfig.class})
@ComponentScan({"info.chgk", "me.nizheg.payments"})
@EnableTransactionManagement
public class ChgkBotConfig {

    public static final String SCOPE_THREAD = "thread";
    @Autowired
    private PropertyService propertyService;

    @Bean
    public CheckChatActive checkChatActive(ChatService chatService) {
        return new CheckChatActive(chatService);
    }

    @Bean
    public CheckMessageNotBlank checkMessageNotBlank() {
        return new CheckMessageNotBlank();
    }

    @Bean
    public CheckUserInChannel checkUserInChannel(
            @Value("${bot.channel}") String channelName,
            BotInfo botInfo, Supplier<TelegramApiClient> telegramApiClientSupplier) {
        return new CheckUserInChannel(botInfo, channelName, telegramApiClientSupplier, usersInChannelCache());
    }

    @Bean(initMethod = "init", destroyMethod = "close")
    public CacheManager cacheManager() {
        return CacheManagerBuilder.newCacheManagerBuilder().build();
    }

    @Bean
    public Cache<Long, Boolean> usersInChannelCache() {
        Long lifeTimeInSeconds = propertyService.getLongValue("bot.channel.users.cache.live.seconds");
        if (lifeTimeInSeconds == null) {
            lifeTimeInSeconds = 300L;
        }
        Long cacheCapacity = propertyService.getLongValue("bot.channel.users.cache.capacity");
        if (cacheCapacity == null) {
            cacheCapacity = 100L;
        }
        CacheConfigurationBuilder<Long, Boolean> cacheConfiguration =
                CacheConfigurationBuilder.newCacheConfigurationBuilder(Long.class, Boolean.class,
                        ResourcePoolsBuilder.heap(cacheCapacity))
                        .withExpiry(ExpiryPolicyBuilder.timeToLiveExpiration(Duration.ofSeconds(lifeTimeInSeconds)));
        return cacheManager().createCache("usersInChannelCache", cacheConfiguration);
    }

    @Bean
    public Cache<Long, Long> activeChatsCache() {
        Long cacheCapacity = propertyService.getLongValue("chat.cache.capacity");
        if (cacheCapacity == null) {
            cacheCapacity = 1000L;
        }
        CacheConfigurationBuilder<Long, Long> cacheConfiguration =
                CacheConfigurationBuilder.newCacheConfigurationBuilder(Long.class, Long.class,
                        ResourcePoolsBuilder.heap(cacheCapacity));
        return cacheManager().createCache("chatCache", cacheConfiguration);
    }

    @Bean
    public Cache<Long, ChatGame> chatGamesCache() {
        Long cacheCapacity = propertyService.getLongValue("chat.cache.capacity");
        if (cacheCapacity == null) {
            cacheCapacity = 1000L;
        }
        CacheConfigurationBuilder<Long, ChatGame> cacheConfiguration =
                CacheConfigurationBuilder.newCacheConfigurationBuilder(Long.class, ChatGame.class,
                        ResourcePoolsBuilder.heap(cacheCapacity));
        return cacheManager().createCache("chatGamesCache", cacheConfiguration);
    }

    @Bean
    @Autowired
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    @Nonnull
    public ChatGame chatGame(
            Chat chat, CategoryService categoryService, TourService tourService, TaskService
            taskService, AnswerLogService answerLogService, TelegramUserService telegramUserService, BotInfo botInfo) {
        return new ChatGame(chat, propertyService, categoryService, tourService, taskService,
                answerLogService, telegramUserService, botInfo, clock());
    }

    @Bean
    @Autowired
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    @Nonnull
    public AutoChatGame autoChatGame(
            Chat chat,
            int timeout,
            CategoryService categoryService,
            TourService tourService,
            TaskService taskService,
            AnswerLogService answerLogService,
            BotInfo botInfo,
            TelegramUserService telegramUserService,
            TaskScheduler taskScheduler,
            AnswerSender answerSender,
            WarningSender warningSender,
            ScheduledOperationService scheduledOperationService) {
        return new AutoChatGame(chat, timeout, propertyService, categoryService, tourService, taskService,
                answerLogService, botInfo, telegramUserService, taskScheduler, answerSender, warningSender,
                scheduledOperationService, clock());
    }

    @Bean
    @Autowired
    public ChatGameFactory chatGameFactory(
            CategoryService categoryService,
            TourService tourService,
            TaskService taskService,
            AnswerLogService answerLogService,
            BotInfo botInfo,
            TelegramUserService telegramUserService,
            TaskScheduler taskScheduler,
            AnswerSender answerSender,
            WarningSender warningSender,
            ScheduledOperationService scheduledOperationService) {
        return new ChatGameFactory() {
            @Nonnull
            @Override
            public ChatGame createChatGame(Chat chat) {
                return chatGame(chat, categoryService, tourService, taskService, answerLogService,
                        telegramUserService, botInfo);
            }

            @Nonnull
            @Override
            public AutoChatGame createAutoChatGame(Chat chat, int timeout) {
                return autoChatGame(chat, timeout, categoryService, tourService, taskService, answerLogService,
                        botInfo, telegramUserService, taskScheduler, answerSender, warningSender,
                        scheduledOperationService);
            }
        };
    }

    @Bean
    public Clock clock() {
        return Clock.systemUTC();
    }
}
