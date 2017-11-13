package me.nizheg.telegram.bot.chgk.util;

import me.nizheg.telegram.bot.chgk.dto.TelegramUser;
import me.nizheg.telegram.bot.chgk.service.TelegramUserService;
import me.nizheg.telegram.model.User;
import me.nizheg.telegram.service.TelegramApiClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

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
