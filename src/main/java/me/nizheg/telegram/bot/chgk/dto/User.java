package me.nizheg.telegram.bot.chgk.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;

/**
 * @author Nikolay Zhegalin
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class User {
    private String username;
    private String password;
}
