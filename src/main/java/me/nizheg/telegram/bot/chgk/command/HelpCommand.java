package me.nizheg.telegram.bot.chgk.command;

import java.util.function.Supplier;

import javax.annotation.Nonnull;

import me.nizheg.telegram.bot.api.service.TelegramApiClient;

/**
 * @author Nikolay Zhegalin
 */
public class HelpCommand extends me.nizheg.telegram.bot.command.HelpCommand {

    public HelpCommand(@Nonnull TelegramApiClient telegramApiClient) {
        super(telegramApiClient);
    }

    public HelpCommand(@Nonnull Supplier<TelegramApiClient> telegramApiClientSupplier) {
        super(telegramApiClientSupplier);
    }

    @Override
    public String getDescription() {
        String description = super.getDescription();
        return description + //
                "\nhttps://telegram.me/storebot?start=chgk_bot - оценить и оставить отзыв\n" + //
                "Подписывайтесь на официальную страницу бота https://vk.com/chgk_bot";
    }
}
