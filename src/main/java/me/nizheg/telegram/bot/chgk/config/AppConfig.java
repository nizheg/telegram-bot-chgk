package me.nizheg.telegram.bot.chgk.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.Scope;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.context.support.SimpleThreadScope;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.lookup.JndiDataSourceLookup;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import java.time.Clock;
import java.util.Arrays;
import java.util.List;

import javax.annotation.Resource;
import javax.sql.DataSource;

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
import me.nizheg.telegram.bot.chgk.repository.TaskDao;
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
import me.nizheg.telegram.bot.chgk.service.impl.CheckUserInChannel;
import me.nizheg.telegram.bot.chgk.util.AnswerSender;
import me.nizheg.telegram.bot.chgk.util.BotInfo;
import me.nizheg.telegram.bot.chgk.util.NextTaskSender;
import me.nizheg.telegram.bot.chgk.util.RatingHelper;
import me.nizheg.telegram.bot.chgk.util.TaskSender;
import me.nizheg.telegram.bot.chgk.util.TourList;
import me.nizheg.telegram.bot.chgk.util.WarningSender;
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
import me.nizheg.telegram.bot.service.impl.UpdateHandlerImpl;

/**
 * @author Nikolay Zhegalin
 */
@Configuration
@ComponentScan({"me.nizheg.telegram.bot.chgk.repository", "me.nizheg.telegram.bot.chgk.service",
        "me.nizheg.telegram.bot.chgk.event", "me.nizheg.telegram.bot.chgk.util", "me.nizheg.telegram.bot.chgk.domain",
        "me.nizheg.telegram.bot.chgk.command", "info.chgk", "me.nizheg.payments"})
@PropertySource("classpath:/config.properties")
@EnableTransactionManagement
public class AppConfig {

    public static final String SCOPE_THREAD = "thread";
    @Autowired
    private PropertyService propertyService;
    @Autowired
    private CategoryService categoryService;
    @Autowired
    private TaskService taskService;
    @Autowired
    private TelegramUserService telegramUserService;
    @Autowired
    private PaymentService paymentService;
    @Autowired
    private AnswerLogService answerLogService;
    @Autowired
    private TaskRatingService taskRatingService;
    @Autowired
    private TaskDao taskDao;
    @Autowired
    private TourService tourService;
    @Autowired
    private ChatService chatService;
    @Autowired
    private ScheduledOperationService scheduledOperationService;
    @Autowired
    private BotInfo botInfo;
    @Autowired
    private ChatGameService chatGameService;
    @Autowired
    private MessageService messageService;
    @Autowired
    private PictureService pictureService;
    @Autowired
    private Cipher cipher;

    @Bean
    public static BeanFactoryPostProcessor beanFactoryPostProcessor() {
        return beanFactory -> beanFactory.registerScope(SCOPE_THREAD, new SimpleThreadScope());
    }

    @Resource
    @Bean
    public DataSource dataSource(
            @Autowired Environment environment) {
        final JndiDataSourceLookup dsLookup = new JndiDataSourceLookup();
        dsLookup.setResourceRef(true);
        String databaseJndiName = environment.getProperty("database.jndi");
        return dsLookup.getDataSource(databaseJndiName);
    }

    @Bean
    @Autowired
    public DataSourceTransactionManager transactionManager(DataSource dataSource) {
        return new DataSourceTransactionManager(dataSource);
    }

    @Bean
    @Scope(SCOPE_THREAD)
    public TelegramApiClient telegramApiClient() {
        String apiToken = propertyService.getValue("api.token");
        return new TelegramApiClientImpl(apiToken);
    }

    @Bean
    @Autowired
    public MessageReceiver messageReceiver(UpdateHandler updateHandler) {
        return new MessageReceiver(telegramApiClient(), updateHandler, propertyService);
    }

    @Bean
    @Autowired
    public UpdateHandler updateHandler(EventsProcessor eventsProcessor, NonCommandExecutor nonCommandExecutor) {
        return new UpdateHandlerImpl(messageParser(), callbackQueryParser(), commandsHolder(), commandExecutor(),
                eventsProcessor, nonCommandExecutor);
    }

    @Bean
    public CommandExecutor commandExecutor() {
        ExceptionHandler exceptionHandler = exceptionHandler();
        CommandExecutor commandExecutor = new CommandExecutorImpl(exceptionHandler);
        String channelName = propertyService.getValue("bot.channel");
        if (channelName != null) {
            CheckUserInChannel checkUserInChannel = new CheckUserInChannel(channelName, this::telegramApiClient);
            return new CommandExecutorWithPrecondition(commandExecutor, exceptionHandler, checkUserInChannel);
        }
        return commandExecutor;
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
    public CallbackQueryParser callbackQueryParser() {
        return new CallbackQueryParserImpl(commandsHolder());
    }

    @Bean
    public MessageParser messageParser() {
        AtomicResponse<User> userResponse = telegramApiClient().getMe().await();
        if (userResponse.getOk()) {
            User botUser = userResponse.getResult();
            return new MessageParserImpl(botUser.getUsername(), commandsHolder());
        } else {
            throw new IllegalStateException(userResponse.getDescription().orElse("Unable to fetch user"));
        }
    }

    @Bean
    public CommandsHolder commandsHolder() {
        return new CommandsHolderImpl(Arrays.asList(
                startCommand(),
                categoryCommand(),
                repeatCommand(),
                answerCommand(),
                hintCommand(),
                nextCommand(),
                stopCommand(),
                statCommand(),
                tourCommand(),
                tournamentCommand(),
                timerCommand(),
                clearCurrentTaskAndSendNextCommand(),
                helpCommand(),
                defaultCommand(),
                ratingCommand(),
                migrateCommand(),
                donateCommand()
        ));
    }

    @Bean
    public StartCommand startCommand() {
        return new StartCommand(this::telegramApiClient, chatService);
    }

    @Bean
    public CategoryCommand categoryCommand() {
        return new CategoryCommand(this::telegramApiClient, categoryService, chatService,
                chatGameService, taskService, tourList());
    }

    @Bean
    public RepeatCommand repeatCommand() {
        return new RepeatCommand(this::telegramApiClient, chatService, chatGameService,
                taskSender(), warningSender());
    }

    @Bean
    public AnswerCommand answerCommand() {
        return new AnswerCommand(this::telegramApiClient, chatService, chatGameService,
                answerSender());
    }

    @Bean
    public HintCommand hintCommand() {
        return new HintCommand(this::telegramApiClient, chatService, chatGameService, answerSender());
    }

    @Bean
    public NextCommand nextCommand() {
        return new NextCommand(this::telegramApiClient, chatService, chatGameService,
                nextTaskSender());
    }

    @Bean
    public StopCommand stopCommand() {
        return new StopCommand(this::telegramApiClient, chatService, chatGameService);
    }

    @Bean
    public StatCommand statCommand() {
        return new StatCommand(this::telegramApiClient, answerLogService, botInfo);
    }

    @Bean
    public TourCommand tourCommand() {
        return new TourCommand(this::telegramApiClient, chatService, chatGameService,
                tourList());
    }

    @Bean
    public TournamentCommand tournamentCommand() {
        return new TournamentCommand(this::telegramApiClient, chatService, tourList());
    }

    @Bean
    public TimerCommand timerCommand() {
        return new TimerCommand(this::telegramApiClient, chatGameService);
    }

    @Bean
    public ClearCurrentTaskAndSendNextCommand clearCurrentTaskAndSendNextCommand() {
        ClearCurrentTaskAndSendNextCommand clearCurrentTaskAndSendNextCommand = new ClearCurrentTaskAndSendNextCommand(
                this::telegramApiClient, chatGameService);
        clearCurrentTaskAndSendNextCommand.setCommandsHolderSupplier(this::commandsHolder);
        return clearCurrentTaskAndSendNextCommand;
    }

    @Bean(initMethod = "init")
    public HelpCommand helpCommand() {
        HelpCommand helpCommand = new HelpCommand(this::telegramApiClient);
        helpCommand.setCommandsHolderSupplier(this::commandsHolder);
        return helpCommand;
    }

    @Bean
    public DefaultCommand defaultCommand() {
        return new DefaultCommand(this::telegramApiClient, chatService, chatGameService,
                taskSender(), telegramUserService, ratingHelper(), botInfo, clock());
    }

    @Bean
    public RatingCommand ratingCommand() {
        return new RatingCommand(this::telegramApiClient, taskRatingService);
    }

    @Bean
    public DonateCommand donateCommand() {
        return new DonateCommand(this::telegramApiClient, paymentService, propertyService);
    }

    @Bean
    public MigrateCommand migrateCommand() {
        return new MigrateCommand(this::telegramApiClient, chatService, chatGameService,
                cipher);
    }

    @Bean
    public TourList tourList() {
        return new TourList(tourService);
    }

    @Bean
    public RatingHelper ratingHelper() {
        return new RatingHelper();
    }

    @Bean
    public TaskSender taskSender() {
        return new TaskSender(this::telegramApiClient, pictureService);
    }

    @Bean
    public AnswerSender answerSender() {
        return new AnswerSender(taskSender(), ratingHelper());
    }

    @Bean
    public NextTaskSender nextTaskSender() {
        return new NextTaskSender(this::telegramApiClient, taskSender(), answerSender(),
                ratingHelper(), tourList(), botInfo);
    }

    @Bean
    public WarningSender warningSender() {
        return new WarningSender(this::telegramApiClient);
    }

    @Bean
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public ChatGame chatGame(Chat chat) {
        return new ChatGame(chat, propertyService, categoryService, tourService, taskService,
                answerLogService, telegramUserService, botInfo, clock());
    }

    @Bean
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public AutoChatGame autoChatGame(Chat chat, int timeout) {
        return new AutoChatGame(chat, timeout, propertyService, categoryService, tourService, taskService,
                answerLogService, botInfo, telegramUserService, taskScheduler(), answerSender(), warningSender(),
                scheduledOperationService, clock());
    }

    @Bean
    public ResourceBundleMessageSource messageSource() {
        ResourceBundleMessageSource messageSource = new ResourceBundleMessageSource();
        messageSource.setBasename("i18n.messages");
        return messageSource;
    }

    @Bean
    public ThreadPoolTaskScheduler taskScheduler() {
        ThreadPoolTaskScheduler threadPoolTaskScheduler = new ThreadPoolTaskScheduler();
        threadPoolTaskScheduler.setPoolSize(10);
        return threadPoolTaskScheduler;
    }

    @Bean
    public ChatGameFactory chatGameFactory() {
        return new ChatGameFactory() {
            @Override
            public ChatGame createChatGame(Chat chat) {
                return chatGame(chat);
            }

            @Override
            public AutoChatGame createAutoChatGame(Chat chat, int timeout) {
                return autoChatGame(chat, timeout);
            }
        };
    }

    @Bean
    public Clock clock() {
        return Clock.systemUTC();
    }

}
