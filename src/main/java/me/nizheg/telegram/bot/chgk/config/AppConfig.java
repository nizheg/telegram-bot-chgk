package me.nizheg.telegram.bot.chgk.config;

import com.vk.VkApi;
import com.vk.impl.VkApiImpl;
import me.nizheg.telegram.bot.api.model.User;
import me.nizheg.telegram.bot.api.service.TelegramApiClient;
import me.nizheg.telegram.bot.api.service.impl.NonBlockingTelegramApiClientImpl;
import me.nizheg.telegram.bot.api.service.impl.TelegramApiClientImpl;
import me.nizheg.telegram.bot.chgk.command.*;
import me.nizheg.telegram.bot.chgk.telegram.TelegramApiClientWrapper;
import me.nizheg.telegram.bot.command.HelpCommand;
import me.nizheg.telegram.bot.event.ChatEventListener;
import me.nizheg.telegram.bot.service.*;
import me.nizheg.telegram.bot.service.impl.*;
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

import javax.annotation.Resource;
import javax.sql.DataSource;
import java.util.Arrays;
import java.util.List;

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
        return new TelegramApiClientWrapper(telegramApiClient);
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
        return new StartCommand(() -> asyncTelegramApiClient(telegramApiClient()));
    }

    @Bean
    public CategoryCommand categoryCommand() {
        return new CategoryCommand(telegramApiClient());
    }

    @Bean
    public RepeatCommand repeatCommand() {
        return new RepeatCommand(telegramApiClient());
    }

    @Bean
    public AnswerCommand answerCommand() {
        return new AnswerCommand(telegramApiClient());
    }

    @Bean
    public HintCommand hintCommand() {
        return new HintCommand(telegramApiClient());
    }

    @Bean
    public NextCommand nextCommand() {
        return new NextCommand(telegramApiClient());
    }

    @Bean
    public StopCommand stopCommand() {
        return new StopCommand(telegramApiClient());
    }

    @Bean
    public FeedbackCommand feedbackCommand() {
        return new FeedbackCommand(telegramApiClient());
    }

    @Bean
    public StatCommand statCommand() {
        return new StatCommand(telegramApiClient());
    }

    @Bean
    public TourCommand tourCommand() {
        return new TourCommand(telegramApiClient());
    }

    @Bean
    public TournamentCommand tournamentCommand() {
        return new TournamentCommand(telegramApiClient());
    }

    @Bean
    public TimerCommand timerCommand() {
        return new TimerCommand(telegramApiClient());
    }

    @Bean
    public ClearCurrentTaskAndSendNextCommand clearCurrentTaskAndSendNextCommand() {
        return new ClearCurrentTaskAndSendNextCommand(telegramApiClient());
    }

    @Bean
    public HelpCommand helpCommand() {
        HelpCommand helpCommand = new HelpCommand(telegramApiClient());
        helpCommand.setCommandsHolderSupplier(this::commandsHolder);
        return helpCommand;
    }

    @Bean
    public DefaultCommand defaultCommand() {
        return new DefaultCommand(telegramApiClient());
    }

    @Bean
    public RatingCommand ratingCommand() {
        return new RatingCommand(telegramApiClient());
    }

    @Bean
    public DonateCommand donateCommand() {
        return new DonateCommand(telegramApiClient());
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
