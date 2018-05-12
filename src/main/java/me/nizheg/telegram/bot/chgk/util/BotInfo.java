package me.nizheg.telegram.bot.chgk.util;

import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

import me.nizheg.telegram.bot.api.model.User;
import me.nizheg.telegram.bot.api.service.TelegramApiClient;
import me.nizheg.telegram.bot.chgk.dto.TelegramUser;
import me.nizheg.telegram.bot.chgk.service.TelegramUserService;

/**
 * @author Nikolay Zhegalin
 */
@Component
public class BotInfo {

    private final TelegramApiClient telegramApiClient;
    private final TelegramUserService telegramUserService;
    private User botUser;

    public BotInfo(
            TelegramApiClient telegramApiClient,
            TelegramUserService telegramUserService) {
        this.telegramApiClient = telegramApiClient;
        this.telegramUserService = telegramUserService;
    }

    @PostConstruct
    public void init() {
        botUser = telegramApiClient.getMe().getResult();
        telegramUserService.createOrUpdate(new TelegramUser(botUser));
    }

    public User getBotUser() {
        return botUser;
    }
}
