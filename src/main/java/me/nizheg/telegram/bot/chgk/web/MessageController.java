package me.nizheg.telegram.bot.chgk.web;

import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.Optional;

import me.nizheg.telegram.bot.chgk.dto.BroadcastStatus;
import me.nizheg.telegram.bot.chgk.dto.SendingMessage;
import me.nizheg.telegram.bot.chgk.dto.TelegramUser;
import me.nizheg.telegram.bot.chgk.service.MessageService;
import me.nizheg.telegram.bot.chgk.service.TelegramUserService;

/**
 * @author Nikolay Zhegalin
 */
@RestController
@RequestMapping("api/message")
public class MessageController {

    private final MessageService messageService;
    private final TelegramUserService telegramUserService;

    public MessageController(
            MessageService messageService,
            TelegramUserService telegramUserService) {
        this.messageService = messageService;
        this.telegramUserService = telegramUserService;
    }

    @RequestMapping(method = RequestMethod.POST)
    public void send(@RequestBody final SendingMessage message, Principal principal) {
        TelegramUser currentUser = Optional.ofNullable(principal)
                .filter(p -> StringUtils.isNotBlank(p.getName()))
                .map(p -> telegramUserService.getByUsername(p.getName()))
                .orElseThrow(() -> new IllegalArgumentException("Пользователь не найден"));
        message.setSender(currentUser);
        messageService.send(message);
    }

    @PutMapping(value = "/{id}/status")
    public void setStatus(@PathVariable long id, @RequestBody BroadcastStatus.Status status) {
        messageService.setStatus(id, status);
    }
//
//    @RequestMapping(value = "/status", method = RequestMethod.GET)
//    public BroadcastStatus getStatus() {
//        return messageService.getStatus();
//    }


}
