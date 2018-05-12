package me.nizheg.telegram.bot.chgk.repository;

import java.util.List;

import me.nizheg.telegram.bot.chgk.dto.ChatMapping;

/**

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
