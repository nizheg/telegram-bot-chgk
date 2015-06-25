package me.nizheg.chgk.dto;

import me.nizheg.telegram.model.User;

import java.io.Serializable;

/**
 * //todo add comments
 *
 * @author Nikolay Zhegalin
 */
public class TelegramUser implements Serializable {

    private static final long serialVersionUID = 243030144259091695L;
    private Long id;
    private String username;
    private String firstname;
    private String lastname;

    public TelegramUser() {
    }

    public TelegramUser(User user) {
        setId(user.getId());
        setUsername(user.getUsername());
        setFirstname(user.getFirstName());
        setLastname(user.getLastName());
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getFirstname() {
        return firstname;
    }

    public void setFirstname(String firstname) {
        this.firstname = firstname;
    }

    public String getLastname() {
        return lastname;
    }

    public void setLastname(String lastname) {
        this.lastname = lastname;
    }
}
