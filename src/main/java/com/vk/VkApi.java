package com.vk;

/**
 * //todo add comments
 *
 * @author Nikolay Zhegalin
 */
public interface VkApi {
    String createCommentOnBoard(long groupId, long topicId, String message, boolean isFromGroup);
}
