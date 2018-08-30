package me.nizheg.telegram.bot.chgk.service;

import org.springframework.transaction.annotation.Transactional;

import me.nizheg.telegram.bot.chgk.dto.User;

/**
 * @author Nikolay Zhegalin
 */
public interface UserService {

    @Transactional
    User createUser(User user);
}
