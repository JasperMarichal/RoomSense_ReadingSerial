package be.kdg.integration3.domain;

import java.sql.Timestamp;


public class TemperatureData implements RawDataRecord {
    private final long timestamp;
    private final int temperature;


    public TemperatureData(long timestamp, int temperature) {
        this.timestamp = timestamp;
        this.temperature = temperature;
    }

    @Override
    public long getTimestamp() {
        return timestamp;
    }

    @Override
    public int getValue() {
        return temperature;
    }
}
