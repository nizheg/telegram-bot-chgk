package me.nizheg.telegram.bot.chgk.web;

import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.Optional;

import me.nizheg.telegram.bot.chgk.dto.BroadcastStatus;
import me.nizheg.telegram.bot.chgk.dto.SendingMessage;
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
    public BroadcastStatus send(@RequestBody final SendingMessage message, Principal principal) {
        return Optional.ofNullable(principal)
                .filter(p -> StringUtils.isNotBlank(p.getName()))
                .map(p -> telegramUserService.getByUsername(p.getName()))
                .map(me -> messageService.send(message, me))
                .orElse(new BroadcastStatus(BroadcastStatus.Status.REJECTED, "Пользователь не найден"));
    }

    @RequestMapping(value = "/status", method = RequestMethod.POST)
    public BroadcastStatus setStatus(@RequestBody BroadcastStatus status) {
        return messageService.setStatus(status);
    }

    @RequestMapping(value = "/status", method = RequestMethod.GET)
    public BroadcastStatus getStatus() {
        return messageService.getStatus();
    }


}
