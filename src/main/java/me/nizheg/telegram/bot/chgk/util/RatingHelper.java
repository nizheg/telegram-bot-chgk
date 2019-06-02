package me.nizheg.telegram.bot.chgk.util;

import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

import me.nizheg.telegram.bot.api.model.InlineKeyboardButton;
import me.nizheg.telegram.util.Emoji;

import static me.nizheg.telegram.bot.api.model.InlineKeyboardButton.callbackDataButton;

@Component
public class RatingHelper {

    public List<InlineKeyboardButton> createRatingButtons(long taskId) {
        InlineKeyboardButton ratingUpButton = callbackDataButton(Emoji.THUMBS_UP_SIGN, "rating " + "+1 " + taskId);
        InlineKeyboardButton ratingDownButton = callbackDataButton(Emoji.THUMBS_DOWN_SIGN, "rating -1 " + taskId);
        return Arrays.asList(ratingUpButton, ratingDownButton);
    }

}
