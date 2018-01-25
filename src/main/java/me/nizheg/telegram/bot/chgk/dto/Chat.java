package me.nizheg.telegram.bot.chgk.dto;

import java.io.Serializable;

import me.nizheg.telegram.bot.api.model.User;
import me.nizheg.telegram.util.TelegramApiUtil;

/**
 * //todo add comments
 *
 * @author Nikolay Zhegalin
 */
public class Chat implements Serializable {

    private static final long serialVersionUID = 1182165756024143195L;
    private final long id;
    private final boolean isPrivate;
    private String title;
    private String userName;
    private String firstName;
    private String lastName;

    public Chat(me.nizheg.telegram.bot.api.model.Chat chat) {
        this.id = chat.getId();
        this.isPrivate = TelegramApiUtil.isPrivateChat(chat);
        this.title = chat.getTitle();
        this.userName = chat.getUsername();
        this.firstName = chat.getFirstName();
        this.lastName = chat.getLastName();
    }

    public Chat(long id, boolean isPrivate) {
        this.id = id;
        this.isPrivate = isPrivate;
    }

    public Chat(User user) {
        this(user.getId(), true);
        userName = user.getUsername();
        firstName = user.getFirstName();
        lastName = user.getLastName();
    }

    public long getId() {
        return id;
    }

    public boolean isPrivate() {
        return isPrivate;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }
}