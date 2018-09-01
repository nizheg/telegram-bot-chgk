package me.nizheg.telegram.bot.chgk.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class PagingParameters {

    private final int limit;
    private int offset;

    public PagingParameters pageNumber(int pageNumber) {
        return offset(Math.max(pageNumber - 1, 0) * limit);
    }

    public PagingParameters offset(int offset) {
        PagingParameters parameters = new PagingParameters(limit);
        parameters.offset = offset;
        return parameters;
    }

}
