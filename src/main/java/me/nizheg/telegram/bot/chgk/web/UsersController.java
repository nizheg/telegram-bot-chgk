package me.nizheg.telegram.bot.chgk.web;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import me.nizheg.telegram.bot.chgk.dto.User;
import me.nizheg.telegram.bot.chgk.service.UserService;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/users")
public class UsersController {

    private final UserService userService;

    @PostMapping
    public User createUser(@RequestBody User user) {
       return userService.createUser(user);
    }
}
