package me.nizheg.telegram.bot.chgk.web;

import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.Optional;

import javax.validation.Valid;

import lombok.RequiredArgsConstructor;
import me.nizheg.telegram.bot.chgk.dto.SendingMessage;
import me.nizheg.telegram.bot.chgk.dto.SendingMessageStatus;
import me.nizheg.telegram.bot.chgk.dto.TelegramUser;
import me.nizheg.telegram.bot.chgk.service.MessageService;
import me.nizheg.telegram.bot.chgk.service.TelegramUserService;

/**
 * @author Nikolay Zhegalin
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("api/message")
public class MessageController {

    private final MessageService messageService;
    private final TelegramUserService telegramUserService;

    @RequestMapping(method = RequestMethod.POST)
    public SendingMessageStatus send(@RequestBody @Valid final SendingMessage message, Principal principal) {
        TelegramUser currentUser = Optional.ofNullable(principal)
                .filter(p -> StringUtils.isNotBlank(p.getName()))
                .map(p -> telegramUserService.getByUsername(p.getName()))
                .orElseThrow(() -> new IllegalArgumentException("Пользователь не найден"));
        message.setSender(currentUser);
        return messageService.send(message);
    }

    @PatchMapping(value = "{id}/status")
    public void setStatus(@PathVariable long id, @RequestBody @Valid SendingMessageStatus status) {
        messageService.setStatus(id, status);
    }

    @RequestMapping(value = "{id}/status", method = RequestMethod.GET)
    public SendingMessageStatus getStatus(@PathVariable long id) {
        return messageService.getStatus(id);
    }


}
