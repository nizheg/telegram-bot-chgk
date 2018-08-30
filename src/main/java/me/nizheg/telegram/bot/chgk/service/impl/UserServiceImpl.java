package me.nizheg.telegram.bot.chgk.service.impl;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.GroupManager;
import org.springframework.security.provisioning.UserDetailsManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;

import lombok.RequiredArgsConstructor;
import me.nizheg.telegram.bot.chgk.dto.User;
import me.nizheg.telegram.bot.chgk.exception.DuplicationException;
import me.nizheg.telegram.bot.chgk.service.UserService;

/**
 * @author Nikolay Zhegalin
 */
@RequiredArgsConstructor
@Service
public class UserServiceImpl implements UserService {

    private static final String GROUP_DEFAULT = "new_users";
    private final UserDetailsManager userDetailsManager;
    private final GroupManager groupManager;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    @Override
    public User createUser(User user) {
        org.springframework.security.core.userdetails.User securityUser
                = new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                passwordEncoder.encode(user.getPassword()),
                Collections.emptyList());
        try {
            userDetailsManager.createUser(securityUser);
        } catch(DuplicateKeyException ex) {
            throw new DuplicationException(user.getUsername());
        }
        groupManager.addUserToGroup(user.getUsername(), GROUP_DEFAULT);
        return user;
    }
}
