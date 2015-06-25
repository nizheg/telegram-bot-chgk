package me.nizheg.chgk.repository;

import java.util.List;

import me.nizheg.chgk.dto.Chat;
import me.nizheg.chgk.exception.DuplicationException;

/**
 * //todo add comments
 *
 * @author Nikolay Zhegalin
 */
public interface ChatDao {

    Chat create(Chat chat) throws DuplicationException;

    Chat read(long id);

    Chat update(Chat chat);

    void delete(long id);

    boolean isExist(long id);

    List<Chat> getChatsWithScheduledOperation();
}
