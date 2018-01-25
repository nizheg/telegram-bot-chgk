package me.nizheg.telegram.bot.chgk.util;

import javax.annotation.PostConstruct;

import me.nizheg.telegram.bot.api.model.User;
import me.nizheg.telegram.bot.api.service.TelegramApiClient;
import me.nizheg.telegram.bot.chgk.dto.TelegramUser;
import me.nizheg.telegram.bot.chgk.service.TelegramUserService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author Nikolay Zhegalin
 */
@Component
public class BotInfo {
    @Autowired
    private TelegramApiClient telegramApiClient;
    @Autowired
    private TelegramUserService telegramUserService;
    private User botUser;

    @PostConstruct
    public void init() {
        botUser = telegramApiClient.getMe().getResult();
        telegramUserService.createOrUpdate(new TelegramUser(botUser));
    }

    public User getBotUser() {
        return botUser;
    }
}
