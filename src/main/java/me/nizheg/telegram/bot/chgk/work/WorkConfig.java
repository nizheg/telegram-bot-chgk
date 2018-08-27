package me.nizheg.telegram.bot.chgk.work;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.context.annotation.Bean;

import javax.sql.DataSource;

import lombok.RequiredArgsConstructor;
import me.nizheg.telegram.bot.api.service.TelegramApiClient;
import me.nizheg.telegram.bot.chgk.service.ChatService;

@RequiredArgsConstructor
public class WorkConfig {

    private final DataSource dataSource;
    private final ChatService chatService;
    private final TelegramApiClient telegramApiClient;

    @Bean(destroyMethod = "shutdown")
    public WorkManager workManager() {
        WorkManager workManager = new WorkManager(workService());
        workManager.registerWorker(sendMessageWorker());
        workManager.registerWorker(forwardMessageWorker());
        return workManager;
    }

    @Bean
    public BroadcastMessageDao broadcastMessageDao() {
        return new JdbcBroadcastMessageDao(dataSource);
    }

    @Bean
    public WorkService workService() {
        return new WorkServiceImpl(broadcastMessageDao(), objectMapper());
    }

    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }

    @Bean
    public SendMessageWorker sendMessageWorker() {
        return new SendMessageWorker(chatService, telegramApiClient);
    }

    @Bean
    public ForwardMessageWorker forwardMessageWorker() {
        return new ForwardMessageWorker(chatService, telegramApiClient);
    }
}
