package me.nizheg.telegram.bot.chgk.util;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author Nikolay Zhegalin
 */
public class DateUtils {

    public static Map<TimeUnit, Long> computeDiff(OffsetDateTime from, OffsetDateTime to) {
        long diffInSeconds = Duration.between(from, to).getSeconds();
        List<TimeUnit> units = Arrays.asList(TimeUnit.DAYS, TimeUnit.HOURS, TimeUnit.MINUTES, TimeUnit.SECONDS);
        Map<TimeUnit, Long> result = new LinkedHashMap<>();
        long secondsRest = diffInSeconds;
        for (TimeUnit unit : units) {
            long diff = unit.convert(secondsRest, TimeUnit.SECONDS);
            long diffInSecondsForUnit = unit.toSeconds(diff);
            secondsRest -= diffInSecondsForUnit;
            result.put(unit, diff);
        }
        return result;
    }
}
