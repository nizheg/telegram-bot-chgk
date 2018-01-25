package me.nizheg.telegram.bot.chgk.util;

import java.util.Arrays;
import java.util.List;

import me.nizheg.telegram.bot.api.model.InlineKeyboardButton;
import me.nizheg.telegram.util.Emoji;

import org.springframework.stereotype.Component;

@Component
public class RatingHelper {

    public List<InlineKeyboardButton> createRatingButtons(long taskId) {
        InlineKeyboardButton ratingUpButton = new InlineKeyboardButton();
        ratingUpButton.setText(Emoji.THUMBS_UP_SIGN);
        ratingUpButton.setCallbackData("rating +1 " + taskId);
        InlineKeyboardButton ratingDownButton = new InlineKeyboardButton();
        ratingDownButton.setText(Emoji.THUMBS_DOWN_SIGN);
        ratingDownButton.setCallbackData("rating -1 " + taskId);
        return Arrays.asList(ratingUpButton, ratingDownButton);
    }

}
