package be.kdg.integration3.domain;

import java.sql.Timestamp;

public class TemperatureData implements RawDataRecord {
    private final Timestamp timestamp;
    private final int temperature;


    public TemperatureData(Timestamp timestamp, int temperature) {
        this.timestamp = timestamp;
        this.temperature = temperature;
    }

    @Override
    public Timestamp getTimestamp() {
        return timestamp;
    }

    @Override
    public int getValue() {
        return temperature;
    }
}
