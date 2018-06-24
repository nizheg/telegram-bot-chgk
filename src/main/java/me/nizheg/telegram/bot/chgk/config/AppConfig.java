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

import java.util.Arrays;
import java.util.List;

import javax.annotation.Resource;
import javax.sql.DataSource;

import me.nizheg.payments.service.PaymentService;
import me.nizheg.telegram.bot.api.model.User;
import me.nizheg.telegram.bot.api.service.TelegramApiClient;
import me.nizheg.telegram.bot.api.service.impl.NonBlockingTelegramApiClientImpl;
import me.nizheg.telegram.bot.api.service.impl.TelegramApiClientImpl;
import me.nizheg.telegram.bot.chgk.command.AnswerCommand;
import me.nizheg.telegram.bot.chgk.command.CategoryCommand;
import me.nizheg.telegram.bot.chgk.command.ClearCurrentTaskAndSendNextCommand;
import me.nizheg.telegram.bot.chgk.command.DefaultCommand;
import me.nizheg.telegram.bot.chgk.command.DonateCommand;
import me.nizheg.telegram.bot.chgk.command.HintCommand;
import me.nizheg.telegram.bot.chgk.command.NextCommand;
import me.nizheg.telegram.bot.chgk.command.RatingCommand;
import me.nizheg.telegram.bot.chgk.command.RepeatCommand;
import me.nizheg.telegram.bot.chgk.command.StartCommand;
import me.nizheg.telegram.bot.chgk.command.StatCommand;
import me.nizheg.telegram.bot.chgk.command.StopCommand;
import me.nizheg.telegram.bot.chgk.command.TimerCommand;
import me.nizheg.telegram.bot.chgk.command.TourCommand;
import me.nizheg.telegram.bot.chgk.command.TournamentCommand;
import me.nizheg.telegram.bot.chgk.domain.AnswerOperation;
import me.nizheg.telegram.bot.chgk.domain.AutoChatGame;
import me.nizheg.telegram.bot.chgk.domain.ChatGame;
import me.nizheg.telegram.bot.chgk.domain.ChatGameFactory;
import me.nizheg.telegram.bot.chgk.domain.WarningOperation;
import me.nizheg.telegram.bot.chgk.dto.Chat;
import me.nizheg.telegram.bot.chgk.repository.TaskDao;
import me.nizheg.telegram.bot.chgk.service.AnswerLogService;
import me.nizheg.telegram.bot.chgk.service.CategoryService;
import me.nizheg.telegram.bot.chgk.service.ChatGameService;
import me.nizheg.telegram.bot.chgk.service.ChatService;
import me.nizheg.telegram.bot.chgk.service.MessageService;
import me.nizheg.telegram.bot.chgk.service.ScheduledOperationService;
import me.nizheg.telegram.bot.chgk.service.TaskRatingService;
import me.nizheg.telegram.bot.chgk.service.TaskService;
import me.nizheg.telegram.bot.chgk.service.TelegramUserService;
import me.nizheg.telegram.bot.chgk.service.TourService;
import me.nizheg.telegram.bot.chgk.telegram.TelegramApiClientWrapper;
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
import me.nizheg.telegram.bot.service.MessageParser;
import me.nizheg.telegram.bot.service.PropertyService;
import me.nizheg.telegram.bot.service.UpdateHandler;
import me.nizheg.telegram.bot.service.impl.CallbackQueryParserImpl;
import me.nizheg.telegram.bot.service.impl.CommandExecutorImpl;
import me.nizheg.telegram.bot.service.impl.CommandsHolderImpl;
import me.nizheg.telegram.bot.service.impl.MessageParserImpl;
import me.nizheg.telegram.bot.service.impl.MessageReceiver;
import me.nizheg.telegram.bot.service.impl.UpdateHandlerImpl;

/**
 * @author Nikolay Zhegalin
 */
@Configuration
@ComponentScan({"me.nizheg.telegram.bot.chgk.repository", "me.nizheg.telegram.bot.chgk.service",
        "me.nizheg.telegram.bot.chgk.event", "me.nizheg.telegram.bot.chgk.util", "me.nizheg.telegram.bot.chgk.domain",
        "me.nizheg.telegram.bot.chgk.command", "info.chgk", "me.nizheg.payments"})
@PropertySource("classpath:/config.properties")
public class AppConfig {

    public static final String SCOPE_THREAD = "thread";
    @Autowired
    private PropertyService propertyService;
    @Autowired
    private RatingHelper ratingHelper;
    @Autowired
    private CategoryService categoryService;
    @Autowired
    private TaskService taskService;
    @Autowired
    private TourList tourList;
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
    private AnswerOperation nextTaskOperation;
    @Autowired
    private WarningOperation warningOperation;
    @Autowired
    private ScheduledOperationService scheduledOperationService;
    @Autowired
    private TaskSender taskSender;
    @Autowired
    private AnswerSender answerSender;
    @Autowired
    private BotInfo botInfo;
    @Autowired
    private NextTaskSender nextTaskSender;
    @Autowired
    private WarningSender warningSender;
    @Autowired
    private ChatGameService chatGameService;
    @Autowired
    private MessageService messageService;

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
    public TelegramApiClient telegramApiClient() {
        String apiToken = propertyService.getValue("api.token");
        TelegramApiClientImpl telegramApiClient = new TelegramApiClientImpl(apiToken);
        return new TelegramApiClientWrapper(telegramApiClient, chatService);
    }

    @Bean
    @Scope(SCOPE_THREAD)
    @Autowired
    public NonBlockingTelegramApiClientImpl asyncTelegramApiClient(TelegramApiClient telegramApiClient) {
        return new NonBlockingTelegramApiClientImpl(telegramApiClient);
    }

    @Bean
    @Autowired
    public MessageReceiver messageReceiver(UpdateHandler updateHandler) {
        return new MessageReceiver(telegramApiClient(), updateHandler, propertyService);
    }

    @Bean
    @Autowired
    public UpdateHandler updateHandler(CommandExecutor commandExecutor) {
        return new UpdateHandlerImpl(messageParser(), callbackQueryParser(), commandsHolder(), commandExecutor);
    }

    @Bean
    public CommandExecutor commandsExecutor(
            @Autowired(required = false)
                    List<ChatEventListener> eventListeners,
            @Autowired(required = false)
                    List<NonCommandMessageProcessor> nonCommandMessageProcessors) {
        return new CommandExecutorImpl(telegramApiClient(), eventListeners, nonCommandMessageProcessors);
    }

    @Bean
    public CallbackQueryParser callbackQueryParser() {
        return new CallbackQueryParserImpl(commandsHolder());
    }

    @Bean
    public MessageParser messageParser() {
        User botUser = telegramApiClient().getMe().getResult();
        return new MessageParserImpl(botUser.getUsername(), commandsHolder());
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
                donateCommand()
        ));
    }

    @Bean
    public StartCommand startCommand() {
        return new StartCommand(() -> asyncTelegramApiClient(telegramApiClient()), chatService);
    }

    @Bean
    public CategoryCommand categoryCommand() {
        return new CategoryCommand(() -> asyncTelegramApiClient(telegramApiClient()), categoryService, chatService,
                chatGameService, taskService, tourList);
    }

    @Bean
    public RepeatCommand repeatCommand() {
        return new RepeatCommand(() -> asyncTelegramApiClient(telegramApiClient()), chatService, chatGameService,
                taskSender,
                warningSender);
    }

    @Bean
    public AnswerCommand answerCommand() {
        return new AnswerCommand(() -> asyncTelegramApiClient(telegramApiClient()), chatService, chatGameService,
                answerSender,
                ratingHelper);
    }

    @Bean
    public HintCommand hintCommand() {
        return new HintCommand(() -> asyncTelegramApiClient(telegramApiClient()), chatService, chatGameService,
                answerSender);
    }

    @Bean
    public NextCommand nextCommand() {
        return new NextCommand(() -> asyncTelegramApiClient(telegramApiClient()), chatService, chatGameService,
                nextTaskSender);
    }

    @Bean
    public StopCommand stopCommand() {
        return new StopCommand(() -> asyncTelegramApiClient(telegramApiClient()), chatService, chatGameService);
    }

    @Bean
    public StatCommand statCommand() {
        return new StatCommand(() -> asyncTelegramApiClient(telegramApiClient()), answerLogService, botInfo);
    }

    @Bean
    public TourCommand tourCommand() {
        return new TourCommand(() -> asyncTelegramApiClient(telegramApiClient()), chatService, chatGameService,
                tourList);
    }

    @Bean
    public TournamentCommand tournamentCommand() {
        return new TournamentCommand(() -> asyncTelegramApiClient(telegramApiClient()), chatService, tourList);
    }

    @Bean
    public TimerCommand timerCommand() {
        return new TimerCommand(() -> asyncTelegramApiClient(telegramApiClient()), chatGameService);
    }

    @Bean
    public ClearCurrentTaskAndSendNextCommand clearCurrentTaskAndSendNextCommand() {
        ClearCurrentTaskAndSendNextCommand clearCurrentTaskAndSendNextCommand = new ClearCurrentTaskAndSendNextCommand(
                () -> asyncTelegramApiClient(telegramApiClient()), chatGameService);
        clearCurrentTaskAndSendNextCommand.setCommandsHolderSupplier(this::commandsHolder);
        return clearCurrentTaskAndSendNextCommand;
    }

    @Bean(initMethod = "init")
    public HelpCommand helpCommand() {
        HelpCommand helpCommand = new HelpCommand(() -> asyncTelegramApiClient(telegramApiClient()));
        helpCommand.setCommandsHolderSupplier(this::commandsHolder);
        return helpCommand;
    }

    @Bean
    public DefaultCommand defaultCommand() {
        return new DefaultCommand(() -> asyncTelegramApiClient(telegramApiClient()), chatService, chatGameService,
                taskSender, telegramUserService, ratingHelper, botInfo);
    }

    @Bean
    public RatingCommand ratingCommand() {
        return new RatingCommand(() -> asyncTelegramApiClient(telegramApiClient()), taskRatingService);
    }

    @Bean
    public DonateCommand donateCommand() {
        return new DonateCommand(() -> asyncTelegramApiClient(telegramApiClient()), paymentService, propertyService);
    }

    @Bean
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public ChatGame chatGame(Chat chat) {
        return new ChatGame(chat, propertyService, categoryService, tourService, taskService,
                answerLogService, telegramUserService, botInfo);
    }

    @Bean
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public AutoChatGame autoChatGame(Chat chat, int timeout) {
        return new AutoChatGame(chat, timeout, propertyService, categoryService, tourService, taskService,
                answerLogService, botInfo, telegramUserService, taskScheduler(), nextTaskOperation, warningOperation,
                scheduledOperationService);
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

}
