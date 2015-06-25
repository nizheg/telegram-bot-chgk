package me.nizheg.chgk.repository.param;

import java.util.List;

/**
 * @author Nikolay Zhegalin
 */
public class StatSearchParams {
    private List<Long> excludeUserIds;
    private Integer limit;

    public List<Long> getExcludeUserIds() {
        return excludeUserIds;
    }

    public void setExcludeUserIds(List<Long> excludeUserIds) {
        this.excludeUserIds = excludeUserIds;
    }

    public Integer getLimit() {
        return limit;
    }

    public void setLimit(Integer limit) {
        this.limit = limit;
    }
}
