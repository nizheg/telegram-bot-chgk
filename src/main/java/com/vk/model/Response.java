package com.vk.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * //todo add comments
 *
 * @author Nikolay Zhegalin
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Response {
    private Integer response;
    private Error error;

    public Integer getResponse() {
        return response;
    }

    public void setResponse(Integer response) {
        this.response = response;
    }

    public Error getError() {
        return error;
    }

    public void setError(Error error) {
        this.error = error;
    }

}
