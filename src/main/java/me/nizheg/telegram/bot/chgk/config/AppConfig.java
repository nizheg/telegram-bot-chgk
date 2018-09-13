package me.nizheg.telegram.bot.chgk.config;

import org.apache.commons.lang3.StringUtils;
import org.ehcache.Cache;
import org.ehcache.CacheManager;
import org.ehcache.config.builders.CacheConfigurationBuilder;
import org.ehcache.config.builders.CacheManagerBuilder;
import org.ehcache.config.builders.ExpiryPolicyBuilder;
import org.ehcache.config.builders.ResourcePoolsBuilder;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Scope;
import org.springframework.context.support.SimpleThreadScope;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import java.time.Clock;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;

import javax.annotation.Nonnull;

import me.nizheg.payments.service.PaymentService;
import me.nizheg.telegram.bot.api.model.AtomicResponse;
import me.nizheg.telegram.bot.api.model.User;
import me.nizheg.telegram.bot.api.service.TelegramApiClient;
import me.nizheg.telegram.bot.api.service.impl.TelegramApiClientImpl;
import me.nizheg.telegram.bot.chgk.command.AnswerCommand;
import me.nizheg.telegram.bot.chgk.command.CategoryCommand;
import me.nizheg.telegram.bot.chgk.command.ClearCurrentTaskAndSendNextCommand;
import me.nizheg.telegram.bot.chgk.command.DefaultCommand;
import me.nizheg.telegram.bot.chgk.command.DonateCommand;
import me.nizheg.telegram.bot.chgk.command.HintCommand;
import me.nizheg.telegram.bot.chgk.command.MigrateCommand;
import me.nizheg.telegram.bot.chgk.command.NextCommand;
import me.nizheg.telegram.bot.chgk.command.RatingCommand;
import me.nizheg.telegram.bot.chgk.command.RepeatCommand;
import me.nizheg.telegram.bot.chgk.command.SaveForwardedMessage;
import me.nizheg.telegram.bot.chgk.command.StartCommand;
import me.nizheg.telegram.bot.chgk.command.StatCommand;
import me.nizheg.telegram.bot.chgk.command.StopCommand;
import me.nizheg.telegram.bot.chgk.command.TimerCommand;
import me.nizheg.telegram.bot.chgk.command.TourCommand;
import me.nizheg.telegram.bot.chgk.command.TournamentCommand;
import me.nizheg.telegram.bot.chgk.command.exception.ChgkCommandExceptionHandler;
import me.nizheg.telegram.bot.chgk.domain.AutoChatGame;
import me.nizheg.telegram.bot.chgk.domain.ChatGame;
import me.nizheg.telegram.bot.chgk.domain.ChatGameFactory;
import me.nizheg.telegram.bot.chgk.dto.Chat;
import me.nizheg.telegram.bot.chgk.service.AnswerLogService;
import me.nizheg.telegram.bot.chgk.service.CategoryService;
import me.nizheg.telegram.bot.chgk.service.ChatGameService;
import me.nizheg.telegram.bot.chgk.service.ChatService;
import me.nizheg.telegram.bot.chgk.service.Cipher;
import me.nizheg.telegram.bot.chgk.service.MessageService;
import me.nizheg.telegram.bot.chgk.service.PictureService;
import me.nizheg.telegram.bot.chgk.service.ScheduledOperationService;
import me.nizheg.telegram.bot.chgk.service.TaskRatingService;
import me.nizheg.telegram.bot.chgk.service.TaskService;
import me.nizheg.telegram.bot.chgk.service.TelegramUserService;
import me.nizheg.telegram.bot.chgk.service.TourService;
import me.nizheg.telegram.bot.chgk.service.impl.ChatGameServiceImpl;
import me.nizheg.telegram.bot.chgk.service.impl.CheckChatActive;
import me.nizheg.telegram.bot.chgk.service.impl.CheckMessageNotBlank;
import me.nizheg.telegram.bot.chgk.service.impl.CheckUserInChannel;
import me.nizheg.telegram.bot.chgk.util.AnswerSender;
import me.nizheg.telegram.bot.chgk.util.BotInfo;
import me.nizheg.telegram.bot.chgk.util.NextTaskSender;
import me.nizheg.telegram.bot.chgk.util.RatingHelper;
import me.nizheg.telegram.bot.chgk.util.TaskSender;
import me.nizheg.telegram.bot.chgk.util.TourList;
import me.nizheg.telegram.bot.chgk.util.WarningSender;
import me.nizheg.telegram.bot.chgk.work.WorkConfig;
import me.nizheg.telegram.bot.command.HelpCommand;
import me.nizheg.telegram.bot.command.NonCommandMessageProcessor;
import me.nizheg.telegram.bot.event.ChatEventListener;
import me.nizheg.telegram.bot.service.CallbackQueryParser;
import me.nizheg.telegram.bot.service.CommandExecutor;
import me.nizheg.telegram.bot.service.CommandsHolder;
import me.nizheg.telegram.bot.service.EventsProcessor;
import me.nizheg.telegram.bot.service.ExceptionHandler;
import me.nizheg.telegram.bot.service.MessageParser;
import me.nizheg.telegram.bot.service.NonCommandExecutor;
import me.nizheg.telegram.bot.service.PropertyService;
import me.nizheg.telegram.bot.service.UpdateHandler;
import me.nizheg.telegram.bot.service.impl.CallbackQueryParserImpl;
import me.nizheg.telegram.bot.service.impl.CommandExecutorImpl;
import me.nizheg.telegram.bot.service.impl.CommandExecutorWithPrecondition;
import me.nizheg.telegram.bot.service.impl.CommandsHolderImpl;
import me.nizheg.telegram.bot.service.impl.EventsProcessorImpl;
import me.nizheg.telegram.bot.service.impl.MessageParserImpl;
import me.nizheg.telegram.bot.service.impl.MessageReceiver;
import me.nizheg.telegram.bot.service.impl.NonCommandExecutorImpl;
import me.nizheg.telegram.bot.service.impl.PreconditionChainStep;
import me.nizheg.telegram.bot.service.impl.UpdateHandlerImpl;

/**
 * @author Nikolay Zhegalin
 */
@Configuration
@Import(WorkConfig.class)
@ComponentScan({"info.chgk", "me.nizheg.payments"})
@EnableTransactionManagement
public class AppConfig {

    public static final String SCOPE_THREAD = "thread";
    @Autowired
    private PropertyService propertyService;

    @Bean
    public static BeanFactoryPostProcessor beanFactoryPostProcessor() {
        return beanFactory -> beanFactory.registerScope(SCOPE_THREAD, new SimpleThreadScope());
    }

    @Bean
    @Scope(SCOPE_THREAD)
    @Autowired
    public TelegramApiClient telegramApiClient() {
        String apiToken = propertyService.getValue("api.token");
        return new TelegramApiClientImpl(apiToken);
    }

    @Bean
    @Autowired
    public MessageReceiver messageReceiver(UpdateHandler updateHandler, PropertyService propertyService) {
        return new MessageReceiver(this::telegramApiClient, updateHandler, propertyService);
    }

    @Bean
    @Autowired
    public UpdateHandler updateHandler(
            MessageParser messageParser, CallbackQueryParser callbackQueryParser,
            EventsProcessor eventsProcessor, NonCommandExecutor nonCommandExecutor, CommandsHolder commandsHolder,
            CommandExecutor commandExecutor) {
        return new UpdateHandlerImpl(messageParser, callbackQueryParser, commandsHolder, commandExecutor,
                eventsProcessor, nonCommandExecutor);
    }

    @Bean
    @Autowired
    public CommandExecutor commandExecutor(BotInfo botInfo, ChatService chatService) {
        ExceptionHandler exceptionHandler = exceptionHandler();
        CommandExecutor commandExecutor = new CommandExecutorImpl(exceptionHandler);
        PreconditionChainStep chainHead = new CheckMessageNotBlank();
        PreconditionChainStep lastChainStep = chainHead.setSuccessor(new CheckChatActive(chatService));
        String channelName = propertyService.getValue("bot.channel");
        if (StringUtils.isNotBlank(channelName)) {
            lastChainStep.setSuccessor(new CheckUserInChannel(botInfo, channelName,
                    this::telegramApiClient, usersInChannelCache()));
        }
        return new CommandExecutorWithPrecondition(commandExecutor, exceptionHandler, chainHead);
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
    public NonCommandExecutor nonCommandExecutor(
            @Autowired(required = false)
                    List<NonCommandMessageProcessor> nonCommandMessageProcessors) {
        return new NonCommandExecutorImpl(exceptionHandler(), nonCommandMessageProcessors);
    }

    @Bean
    public EventsProcessor eventsProcessor(@Autowired(required = false) List<ChatEventListener> eventListeners) {
        return new EventsProcessorImpl(exceptionHandler(), eventListeners);
    }

    @Bean
    public ExceptionHandler exceptionHandler() {
        return new ChgkCommandExceptionHandler(this::telegramApiClient);
    }

    @Bean
    @Autowired
    public CallbackQueryParser callbackQueryParser(CommandsHolder commandsHolder) {
        return new CallbackQueryParserImpl(commandsHolder);
    }

    @Bean
    @Autowired
    public MessageParser messageParser(CommandsHolder commandsHolder) {
        AtomicResponse<User> userResponse = telegramApiClient().getMe().await();
        if (userResponse.isOk()) {
            User botUser = userResponse.getResult();
            return new MessageParserImpl(botUser.getUsername(), commandsHolder);
        } else {
            throw new IllegalStateException(userResponse.getDescription().orElse("Unable to fetch user"));
        }
    }

    @Bean
    @Autowired
    public CommandsHolder commandsHolder(
            StartCommand startCommand,
            CategoryCommand categoryCommand,
            RepeatCommand repeatCommand,
            AnswerCommand answerCommand,
            HintCommand hintCommand,
            NextCommand nextCommand,
            StopCommand stopCommand,
            StatCommand statCommand,
            TourCommand tourCommand,
            TournamentCommand tournamentCommand,
            TimerCommand timerCommand,
            ClearCurrentTaskAndSendNextCommand clearCurrentTaskAndSendNextCommand,
            HelpCommand helpCommand,
            DefaultCommand defaultCommand,
            RatingCommand ratingCommand,
            MigrateCommand migrateCommand,
            DonateCommand donateCommand) {
        return new CommandsHolderImpl(Arrays.asList(
                startCommand,
                categoryCommand,
                repeatCommand,
                answerCommand,
                hintCommand,
                nextCommand,
                stopCommand,
                statCommand,
                tourCommand,
                tournamentCommand,
                timerCommand,
                clearCurrentTaskAndSendNextCommand,
                helpCommand,
                defaultCommand,
                ratingCommand,
                migrateCommand,
                donateCommand
        ));
    }

    @Bean
    @Autowired
    public StartCommand startCommand(ChatService chatService) {
        return new StartCommand(this::telegramApiClient, chatService);
    }

    @Bean
    @Autowired
    public CategoryCommand categoryCommand(
            CategoryService categoryService,
            ChatService chatService,
            ChatGameService chatGameService,
            TaskService taskService,
            TourList tourList) {
        return new CategoryCommand(this::telegramApiClient, categoryService, chatService,
                chatGameService, taskService, tourList);
    }

    @Bean
    @Autowired
    public RepeatCommand repeatCommand(
            ChatService chatService,
            ChatGameService chatGameService,
            TaskSender taskSender) {
        return new RepeatCommand(this::telegramApiClient, chatService, chatGameService, taskSender, warningSender());
    }

    @Bean
    @Autowired
    public AnswerCommand answerCommand(
            ChatService chatService,
            ChatGameService chatGameService,
            AnswerSender answerSender) {
        return new AnswerCommand(this::telegramApiClient, chatService, chatGameService, answerSender);
    }

    @Bean
    @Autowired
    public HintCommand hintCommand(
            ChatService chatService,
            ChatGameService chatGameService,
            AnswerSender answerSender) {
        return new HintCommand(this::telegramApiClient, chatService, chatGameService, answerSender);
    }

    @Bean
    @Autowired
    public NextCommand nextCommand(
            ChatService chatService,
            ChatGameService chatGameService,
            NextTaskSender nextTaskSender) {
        return new NextCommand(this::telegramApiClient, chatService, chatGameService, nextTaskSender);
    }

    @Bean
    @Autowired
    public StopCommand stopCommand(ChatService chatService, ChatGameService chatGameService) {
        return new StopCommand(this::telegramApiClient, chatService, chatGameService);
    }

    @Bean
    @Autowired
    public StatCommand statCommand(AnswerLogService answerLogService, BotInfo botInfo) {
        return new StatCommand(this::telegramApiClient, answerLogService, botInfo);
    }

    @Bean
    @Autowired
    public TourCommand tourCommand(ChatService chatService, ChatGameService chatGameService, TourList tourList) {
        return new TourCommand(this::telegramApiClient, chatService, chatGameService, tourList);
    }

    @Bean
    @Autowired
    public TournamentCommand tournamentCommand(TourList tourList) {
        return new TournamentCommand(this::telegramApiClient, tourList);
    }

    @Bean
    @Autowired
    public TimerCommand timerCommand(ChatGameService chatGameService) {
        return new TimerCommand(this::telegramApiClient, chatGameService);
    }

    @Bean
    @Autowired
    public ClearCurrentTaskAndSendNextCommand clearCurrentTaskAndSendNextCommand(
            ChatGameService chatGameService,
            BeanFactory beanFactory) {
        ClearCurrentTaskAndSendNextCommand clearCurrentTaskAndSendNextCommand = new ClearCurrentTaskAndSendNextCommand(
                this::telegramApiClient, chatGameService);
        clearCurrentTaskAndSendNextCommand.setCommandsHolderSupplier(
                () -> beanFactory.getBean(CommandsHolder.class));
        return clearCurrentTaskAndSendNextCommand;
    }

    @Bean
    @Autowired
    public HelpCommand helpCommand(BeanFactory beanFactory) {
        HelpCommand helpCommand = new HelpCommand(this::telegramApiClient);
        helpCommand.setCommandsHolderSupplier(() -> beanFactory.getBean(CommandsHolder.class));
        return helpCommand;
    }

    @Bean
    @Autowired
    public DefaultCommand defaultCommand(
            ChatService chatService, ChatGameService chatGameService, TaskSender taskSender,
            TelegramUserService telegramUserService, BotInfo botInfo) {
        return new DefaultCommand(this::telegramApiClient, chatService, chatGameService,
                taskSender, telegramUserService, ratingHelper(), botInfo, clock());
    }

    @Bean
    @Autowired
    public RatingCommand ratingCommand(TaskRatingService taskRatingService) {
        return new RatingCommand(this::telegramApiClient, taskRatingService);
    }

    @Bean
    @Autowired
    public DonateCommand donateCommand(PaymentService paymentService) {
        return new DonateCommand(this::telegramApiClient, paymentService, propertyService);
    }

    @Bean
    @Autowired
    public MigrateCommand migrateCommand(ChatService chatService, ChatGameService chatGameService, Cipher cipher) {
        return new MigrateCommand(this::telegramApiClient, chatService, chatGameService, cipher);
    }

    @Bean
    @Autowired
    public SaveForwardedMessage saveForwardedMessage(
            TelegramUserService telegramUserService,
            MessageService messageService) {
        return new SaveForwardedMessage(telegramUserService, messageService);
    }

    @Bean
    @Autowired
    public TourList tourList(TourService tourService) {
        return new TourList(tourService);
    }

    @Bean
    public RatingHelper ratingHelper() {
        return new RatingHelper();
    }

    @Bean
    @Autowired
    public TaskSender taskSender(PictureService pictureService) {
        return new TaskSender(this::telegramApiClient, pictureService);
    }

    @Bean
    @Autowired
    public AnswerSender answerSender(TaskSender taskSender) {
        return new AnswerSender(taskSender, ratingHelper());
    }

    @Bean
    @Autowired
    public NextTaskSender nextTaskSender(
            TaskSender taskSender, AnswerSender answerSender, TourList tourList,
            BotInfo botInfo) {
        return new NextTaskSender(this::telegramApiClient, taskSender, answerSender, ratingHelper(), tourList, botInfo);
    }

    @Bean
    public WarningSender warningSender() {
        return new WarningSender(this::telegramApiClient);
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
            AnswerSender answerSender,
            ScheduledOperationService scheduledOperationService) {
        return new AutoChatGame(chat, timeout, propertyService, categoryService, tourService, taskService,
                answerLogService, botInfo, telegramUserService, taskScheduler(), answerSender, warningSender(),
                scheduledOperationService, clock());
    }

    @Bean
    public ThreadPoolTaskScheduler taskScheduler() {
        ThreadPoolTaskScheduler threadPoolTaskScheduler = new ThreadPoolTaskScheduler();
        threadPoolTaskScheduler.setPoolSize(10);
        return threadPoolTaskScheduler;
    }

    @Bean
    @Autowired
    public ChatGameService chatGameService(ChatService chatService, ChatGameFactory chatGameFactory) {
        return new ChatGameServiceImpl(propertyService, chatService, chatGamesCache(), taskScheduler(),
                chatGameFactory);
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
            AnswerSender answerSender,
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
                        botInfo, telegramUserService, answerSender, scheduledOperationService);
            }
        };
    }

    @Bean
    public Clock clock() {
        return Clock.systemUTC();
    }
}
