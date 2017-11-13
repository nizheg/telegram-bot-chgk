package me.nizheg.telegram.bot.chgk.dto;

import java.io.Serializable;

/**
 * @author Nikolay Zhegalin
 */
public class UsageStat implements Serializable {

    private static final long serialVersionUID = -2001918173488331216L;
    private long usedCount;
    private long count;

    public long getUsedCount() {
        return usedCount;
    }

    public void setUsedCount(long usedCount) {
        this.usedCount = usedCount;
    }

    public long getCount() {
        return count;
    }

    public void setCount(long count) {
        this.count = count;
    }
}
