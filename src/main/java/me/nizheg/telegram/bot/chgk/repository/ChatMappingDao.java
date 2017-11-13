package me.nizheg.telegram.bot.chgk.repository;

import me.nizheg.telegram.bot.chgk.dto.ChatMapping;

import java.util.List;

/**
 * //todo add comments
 *
 * @author Nikolay Zhegalin
 */
public interface ChatMappingDao {
    ChatMapping create(ChatMapping chatMapping);

    List<ChatMapping> getByGroupId(Long groupId);

    List<ChatMapping> getBySuperGroupId(Long superGroupId);

    boolean isExistsForGroup(String groupId);

    boolean isExistsForSuperGroup(String superGroupId);
}
