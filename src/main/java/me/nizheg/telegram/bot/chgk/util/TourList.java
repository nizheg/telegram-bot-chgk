package me.nizheg.telegram.bot.chgk.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import me.nizheg.telegram.bot.api.model.InlineKeyboardButton;
import me.nizheg.telegram.bot.api.model.InlineKeyboardMarkup;
import me.nizheg.telegram.bot.api.model.ParseMode;
import me.nizheg.telegram.bot.api.service.param.Message;
import me.nizheg.telegram.bot.api.util.TelegramHtmlUtil;
import me.nizheg.telegram.bot.chgk.dto.LightTour;
import me.nizheg.telegram.bot.chgk.dto.composite.LightTourWithStat;
import me.nizheg.telegram.bot.chgk.dto.composite.TourGroup;
import me.nizheg.telegram.bot.chgk.dto.composite.Tournament;
import me.nizheg.telegram.bot.chgk.service.TourService;
import me.nizheg.telegram.util.Emoji;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author Nikolay Zhegalin
 */
@Component
public class TourList {

    private static final String ICON_TOURNAMENT = Emoji.PAGE_WITH_CURL;
    private static final String ICON_TOURNAMENT_GROUP = Emoji.OPEN_FILE_FOLDER;
    private final static int PAGE_SIZE = 5;
    @Autowired
    private TourService tourService;

    public Message getTournamentsListOfChat(Long chatId, int numberOfPage) {
        StringBuilder messageBuilder = new StringBuilder("<b>Выберите турнир для прохождения:</b>\n");
        int publishedTournamentsCount = tourService.getPublishedTournamentsCount();
        List<LightTourWithStat> publishedTournaments = tourService.getPublishedTournamentsWithStatForChat(chatId, PAGE_SIZE, numberOfPage);
        for (LightTourWithStat publishedTournament : publishedTournaments) {
            LightTour lightTour = publishedTournament.getLightTour();
            messageBuilder.append("\n" + ICON_TOURNAMENT + "<b> " + getTitle(lightTour) + "</b>");
            messageBuilder.append("\n<i>Выполнено:</i> " + Math.abs(publishedTournament.getDonePercent()) + "% из " + publishedTournament.getTasksCount());
            messageBuilder.append("\n<i>Выбрать:</i> " + createTourLink(lightTour) + "\n");
        }
        InlineKeyboardMarkup inlineKeyboardMarkup = null;
        if (publishedTournamentsCount > PAGE_SIZE) {
            int numberOfPages = publishedTournamentsCount / PAGE_SIZE;
            if (publishedTournamentsCount % PAGE_SIZE > 0) {
                numberOfPages = numberOfPages + 1;
            }
            int previousPage = numberOfPage - 1;
            if (previousPage < 0) {
                previousPage = numberOfPages - 1;
            }
            int nextPage = numberOfPage + 1;
            if (nextPage >= numberOfPages) {
                nextPage = 0;
            }
            inlineKeyboardMarkup = new InlineKeyboardMarkup();
            List<InlineKeyboardButton> keyboard = new ArrayList<InlineKeyboardButton>();
            inlineKeyboardMarkup.setInlineKeyboard(Collections.singletonList(keyboard));
            InlineKeyboardButton previous = new InlineKeyboardButton();
            previous.setText(Emoji.LEFTWARDS_BLACK_ARROW);
            previous.setCallbackData("tournament " + previousPage);
            keyboard.add(previous);
            InlineKeyboardButton root = new InlineKeyboardButton();
            root.setText(Emoji.OPEN_FILE_FOLDER + " Корень");
            root.setCallbackData("tour");
            keyboard.add(root);
            InlineKeyboardButton next = new InlineKeyboardButton();
            next.setText(Emoji.BLACK_RIGHTWARDS_ARROW);
            next.setCallbackData("tournament " + nextPage);
            keyboard.add(next);
        }
        return new Message(messageBuilder.toString(), chatId, ParseMode.HTML, true, null, inlineKeyboardMarkup);
    }

    private String getTitle(LightTour lightTour) {
        return TelegramHtmlUtil.escape(lightTour.getTitle());
    }

    public String getToursListOfTourGroup(long tourId) {
        LightTour compositeTour = tourService.createCompositeTour(tourId);
        if (compositeTour instanceof TourGroup && !(compositeTour instanceof Tournament)) {
            TourGroup tourGroup = (TourGroup) compositeTour;
            StringBuilder messageBuilder = new StringBuilder();
            if (tourGroup.getId() != LightTour.ROOT_ID) {
                messageBuilder.append(Emoji.UPWARDS_BLACK_ARROW + " /tour Корень\n");
            }
            LightTour parentTour = tourGroup.getParentTour();
            if (parentTour != null && parentTour.getId() != LightTour.ROOT_ID) {
                messageBuilder.append(Emoji.LEFTWARDS_BLACK_ARROW + " " + createTourLink(parentTour) + " " + getTitle(parentTour) + "\n");
            }
            for (LightTour lightTour : tourGroup.getChildTours()) {
                if (lightTour.getType() == LightTour.Type.TOURNAMENT && lightTour.getStatus() != LightTour.Status.PUBLISHED) {
                    continue;
                }
                String emoji = "";
                switch (lightTour.getType()) {
                    case TOURNAMENT_GROUP:
                        emoji = ICON_TOURNAMENT_GROUP;
                        break;
                    case TOURNAMENT:
                        emoji = ICON_TOURNAMENT;
                        break;
                    default:
                        break;
                }
                messageBuilder.append("\n" + emoji + " " + createTourLink(lightTour) + " " + getTitle(lightTour));
            }
            return messageBuilder.toString();
        }
        return null;
    }

    private String createTourLink(LightTour tour) {
        return "/tour_" + tour.getId();
    }
}
