package me.nizheg.telegram.bot.chgk.config;

import com.vk.VkApi;
import com.vk.impl.VkApiImpl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.context.support.SimpleThreadScope;
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
import me.nizheg.telegram.bot.chgk.command.FeedbackCommand;
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
import me.nizheg.telegram.bot.chgk.service.AnswerLogService;
import me.nizheg.telegram.bot.chgk.service.CategoryService;
import me.nizheg.telegram.bot.chgk.service.ChatService;
import me.nizheg.telegram.bot.chgk.service.FeedbackService;
import me.nizheg.telegram.bot.chgk.service.TaskRatingService;
import me.nizheg.telegram.bot.chgk.service.TaskService;
import me.nizheg.telegram.bot.chgk.service.TelegramUserService;
import me.nizheg.telegram.bot.chgk.telegram.TelegramApiClientWrapper;
import me.nizheg.telegram.bot.chgk.util.AnswerSender;
import me.nizheg.telegram.bot.chgk.util.BotInfo;
import me.nizheg.telegram.bot.chgk.util.NextTaskSender;
import me.nizheg.telegram.bot.chgk.util.RatingHelper;
import me.nizheg.telegram.bot.chgk.util.TaskSender;
import me.nizheg.telegram.bot.chgk.util.TourList;
import me.nizheg.telegram.bot.chgk.util.WarningSender;
import me.nizheg.telegram.bot.command.HelpCommand;
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
        "info.chgk", "me.nizheg.payments"})
//@PropertySource()
public class AppConfig {

    public static final String SCOPE_THREAD = "thread";
    @Autowired
    private PropertyService propertyService;
    @Autowired
    private ChatService chatService;
    @Autowired
    private AnswerSender answerSender;
    @Autowired
    private RatingHelper ratingHelper;
    @Autowired
    private CategoryService categoryService;
    @Autowired
    private TaskService taskService;
    @Autowired
    private TourList tourList;
    @Autowired
    private TaskSender taskSender;
    @Autowired
    private TelegramUserService telegramUserService;
    @Autowired
    private BotInfo botInfo;
    @Autowired
    private PaymentService paymentService;
    @Autowired
    private FeedbackService feedbackService;
    @Autowired
    private NextTaskSender nextTaskSender;
    @Autowired
    private WarningSender warningSender;
    @Autowired
    private AnswerLogService answerLogService;
    @Autowired
    private TaskRatingService taskRatingService;

    @Bean
    public static BeanFactoryPostProcessor beanFactoryPostProcessor() {
        return beanFactory -> beanFactory.registerScope(SCOPE_THREAD, new SimpleThreadScope());
    }

    @Resource
    @Bean
    public DataSource dataSource() {
        final JndiDataSourceLookup dsLookup = new JndiDataSourceLookup();
        dsLookup.setResourceRef(true);
        return dsLookup.getDataSource("jdbc/chgk");
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
    public VkApi vkApi() {
        String vkToken = propertyService.getValue("vk.access.token");
        return new VkApiImpl(vkToken);
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
    @Autowired(required = false)
    public CommandExecutor commandsExecutor(List<ChatEventListener> eventListeners) {
        return new CommandExecutorImpl(telegramApiClient(), eventListeners);
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
                feedbackCommand(),
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
                taskService, tourList);
    }

    @Bean
    public RepeatCommand repeatCommand() {
        return new RepeatCommand(() -> asyncTelegramApiClient(telegramApiClient()), chatService, taskSender,
                warningSender);
    }

    @Bean
    public AnswerCommand answerCommand() {
        return new AnswerCommand(() -> asyncTelegramApiClient(telegramApiClient()), chatService, answerSender,
                ratingHelper);
    }

    @Bean
    public HintCommand hintCommand() {
        return new HintCommand(() -> asyncTelegramApiClient(telegramApiClient()), chatService, answerSender);
    }

    @Bean
    public NextCommand nextCommand() {
        return new NextCommand(() -> asyncTelegramApiClient(telegramApiClient()), chatService, nextTaskSender);
    }

    @Bean
    public StopCommand stopCommand() {
        return new StopCommand(() -> asyncTelegramApiClient(telegramApiClient()), chatService);
    }

    @Bean
    public FeedbackCommand feedbackCommand() {
        return new FeedbackCommand(() -> asyncTelegramApiClient(telegramApiClient()), feedbackService);
    }

    @Bean
    public StatCommand statCommand() {
        return new StatCommand(() -> asyncTelegramApiClient(telegramApiClient()), answerLogService, botInfo);
    }

    @Bean
    public TourCommand tourCommand() {
        return new TourCommand(() -> asyncTelegramApiClient(telegramApiClient()), chatService, tourList);
    }

    @Bean
    public TournamentCommand tournamentCommand() {
        return new TournamentCommand(() -> asyncTelegramApiClient(telegramApiClient()), chatService, tourList);
    }

    @Bean
    public TimerCommand timerCommand() {
        return new TimerCommand(() -> asyncTelegramApiClient(telegramApiClient()), chatService);
    }

    @Bean
    public ClearCurrentTaskAndSendNextCommand clearCurrentTaskAndSendNextCommand() {
        return new ClearCurrentTaskAndSendNextCommand(() -> asyncTelegramApiClient(telegramApiClient()), chatService);
    }

    @Bean(initMethod = "init")
    public HelpCommand helpCommand() {
        HelpCommand helpCommand = new HelpCommand(() -> asyncTelegramApiClient(telegramApiClient()));
        helpCommand.setCommandsHolderSupplier(this::commandsHolder);
        return helpCommand;
    }

    @Bean
    public DefaultCommand defaultCommand() {
        return new DefaultCommand(() -> asyncTelegramApiClient(telegramApiClient()), chatService, taskSender,
                telegramUserService, ratingHelper, botInfo);
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

}
