package me.nizheg.telegram.bot.chgk.work;

import java.util.Map;

import lombok.Builder;
import lombok.Getter;
import me.nizheg.telegram.bot.chgk.work.data.SendingWorkData;

@Builder
@Getter
public class SendingWorkStatus {
    private final long id;
    private final SendingWorkData data;
    private final Map<WorkStatus, Integer> statuses;
}
