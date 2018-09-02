package me.nizheg.telegram.bot.chgk.web;

import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.async.DeferredResult;

import java.security.Principal;
import java.util.List;
import java.util.Optional;

import javax.validation.Valid;

import lombok.RequiredArgsConstructor;
import me.nizheg.telegram.bot.chgk.dto.SendingMessage;
import me.nizheg.telegram.bot.chgk.dto.SendingMessageStatus;
import me.nizheg.telegram.bot.chgk.dto.TelegramUser;
import me.nizheg.telegram.bot.chgk.service.MessageService;
import me.nizheg.telegram.bot.chgk.service.TelegramUserService;
import me.nizheg.telegram.bot.chgk.work.data.ForwardMessageData;

/**
 * @author Nikolay Zhegalin
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("api/message")
public class MessageController {

    private final MessageService messageService;
    private final TelegramUserService telegramUserService;

    @PostMapping
    public SendingMessageStatus send(@RequestBody @Valid final SendingMessage message, Principal principal) {
        TelegramUser currentUser = Optional.ofNullable(principal)
                .filter(p -> StringUtils.isNotBlank(p.getName()))
                .map(p -> telegramUserService.getByUsername(p.getName()))
                .orElseThrow(() -> new IllegalArgumentException("Пользователь не найден"));
        message.setSender(currentUser);
        return messageService.send(message);
    }

    @GetMapping
    public List<SendingMessageStatus> getMessages(@RequestParam(defaultValue = "1") int pageNumber) {
        return messageService.getStatuses(pageNumber);
    }

    @PatchMapping(value = "{id}/status")
    public void setStatus(@PathVariable long id, @RequestBody @Valid SendingMessageStatus status) {
        messageService.setStatus(id, status);
    }

    @GetMapping(value = "{id}/status")
    public SendingMessageStatus getStatus(@PathVariable long id) {
        return messageService.getStatus(id);
    }

    @GetMapping("forwardData")
    public DeferredResult<ForwardMessageData> getForwardData() {
        return messageService.waitMessageForForwarding();
    }

}
