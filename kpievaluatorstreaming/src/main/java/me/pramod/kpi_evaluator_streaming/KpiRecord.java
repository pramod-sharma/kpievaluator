package me.pramod.kpi_evaluator_streaming;

import java.io.Serializable;

/**
 * Created by pramod on 20/10/17.
 */
public class KpiRecord implements Serializable{
    private static final String UNDERSCORE_SEPERATOR = "_";

    private final String userId;
    private final Integer hourOfData;
    private final Integer dayOfYear;
    private final Integer kpiCount;
    private final String id;

    public KpiRecord(String userId, Integer hourOfData, Integer dayOfYear, Integer kpiCount) {
        this.userId = userId;
        this.hourOfData = hourOfData;
        this.dayOfYear = dayOfYear;
        this.kpiCount = kpiCount;
        this.id = new StringBuilder().append(userId).append(UNDERSCORE_SEPERATOR).
                append(dayOfYear).append(UNDERSCORE_SEPERATOR).append(hourOfData).toString();
    }

    public String getUserId() {
        return userId;
    }

    public Integer getHourOfData() {
        return hourOfData;
    }

    public Integer getDayOfYear() {
        return dayOfYear;
    }

    public Integer getKpiCount() {
        return kpiCount;
    }

    public String getId() {
        return id;
    }
}
