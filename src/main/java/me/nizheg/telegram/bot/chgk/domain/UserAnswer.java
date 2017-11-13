package me.nizheg.telegram.bot.chgk.domain;

import me.nizheg.telegram.model.User;

/**
 * @author Nikolay Zhegalin
 */
public class UserAnswer {
    private final String text;
    private final User user;

    public UserAnswer(String text, User user) {
        this.text = text;
        this.user = user;
    }

    public String getText() {
        return text;
    }

    public User getUser() {
        return user;
    }

}
