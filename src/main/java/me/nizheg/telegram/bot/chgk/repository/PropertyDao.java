package me.nizheg.telegram.bot.chgk.repository;

import java.util.List;

import me.nizheg.telegram.bot.chgk.dto.Property;

/**

 *
 * @author Nikolay Zhegalin
 */
public interface PropertyDao {

    Property create(Property property);

    Property read(String key);

    Property readByKeyAndChatId(String key, Long chatId);

    List<Long> readChatIdsByKeyAndValue(String key, String value);

    Property update(Property property);

    void delete(Property property);

    boolean isExist(String key);

    boolean isExist(String key, Long chatId);

    void copyProperties(Long fromChatId, Long toChatId);

    void deleteByChatId(Long chatId);
}
