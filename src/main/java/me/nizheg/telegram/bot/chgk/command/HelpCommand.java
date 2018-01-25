package me.nizheg.telegram.bot.chgk.command;

import me.nizheg.telegram.bot.api.service.TelegramApiClient;

/**
 * //todo add comments
 *
 * @author Nikolay Zhegalin
 */
public class HelpCommand extends me.nizheg.telegram.bot.command.HelpCommand {
    public HelpCommand(TelegramApiClient telegramApiClient) {
        super(telegramApiClient);
    }

    @Override
    public String getDescription() {
        String description = super.getDescription();
        return description + //
                "\nhttps://telegram.me/storebot?start=chgk_bot - оценить и оставить отзыв\n" + //
                "Подписывайтесь на официальную страницу бота https://vk.com/chgk_bot";
    }
}
