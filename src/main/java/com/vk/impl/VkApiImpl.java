package com.vk.impl;

import com.vk.VkApi;
import com.vk.model.Response;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.web.client.RestTemplate;

import java.util.Map;
import java.util.TreeMap;

/**

 *
 * @author Nikolay Zhegalin
 */
public class VkApiImpl implements VkApi {

    private final Log logger = LogFactory.getLog(getClass());
    private final String accessToken;
    private static final String API_URL = "https://api.vk.com/method/";

    public VkApiImpl(String accessToken) {
        Validate.notBlank(accessToken, "Acess token should be defined");
        this.accessToken = accessToken;
    }

    @Override
    public String createCommentOnBoard(long groupId, long topicId, String message, boolean isFromGroup) {
        Map<String, Object> parameters = new TreeMap<>();
        parameters.put("access_token", accessToken);
        parameters.put("group_id", groupId);
        parameters.put("topic_id", topicId);
        parameters.put("message", message);
        if (isFromGroup) {
            parameters.put("from_group", 1);
        } else {
            parameters.put("from_group", 0);
        }
        RestTemplate restTemplate = new RestTemplate();
        Response response;
        try {
            response = restTemplate.postForObject(API_URL + "/board.createComment" + createQueryString(parameters), null, Response.class, parameters);
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
            return null;
        }
        if (response.getError() != null) {
            logger.error(response.getError().getErrorCode() + " " + response.getError().getErrorMessage());
        }
        if (response.getResponse() != null) {
            return "https://vk.com/topic-" + groupId + "_" + topicId + "?post=" + response.getResponse();
        } else {
            return null;
        }
    }

    private String createQueryString(Map<String, Object> parameters) {
        StringBuilder urlBuilder = new StringBuilder();
        if (!parameters.isEmpty()) {
            urlBuilder.append("?");
        }
        for (String s : parameters.keySet()) {
            urlBuilder.append(s + "={" + s + "}&");
        }
        return StringUtils.removeEnd(urlBuilder.toString(), "&");
    }

}
