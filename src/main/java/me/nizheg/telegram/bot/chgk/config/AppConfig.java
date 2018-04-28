package me.nizheg.telegram.bot.chgk.config;

import com.vk.VkApi;
import com.vk.impl.VkApiImpl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.lookup.JndiDataSourceLookup;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import javax.annotation.Resource;
import javax.sql.DataSource;

import me.nizheg.telegram.bot.api.service.TelegramApiClient;
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
import me.nizheg.telegram.bot.chgk.telegram.TelegramApiClientWrapper;
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
        "me.nizheg.telegram.bot.chgk.event", "info.chgk", "me.nizheg.payments"})
@EnableScheduling()
//@PropertySource()
public class AppConfig {

    private final PropertyService propertyService;

    public AppConfig(PropertyService propertyService) {this.propertyService = propertyService;}

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
        String botName = propertyService.getValue("bot.name");
        return new MessageParserImpl(botName, commandsHolder());
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
        return new StartCommand(telegramApiClient());
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
        return new HelpCommand(telegramApiClient());
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

    @Bean(destroyMethod = "shutdown")
    public ScheduledExecutorService taskScheduler() {
        return Executors.newScheduledThreadPool(10);
    }

}
