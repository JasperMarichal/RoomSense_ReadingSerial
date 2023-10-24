package be.kdg.integration3.domain;

import java.sql.Timestamp;

public class HumidityData implements RawDataRecord{

    private final Timestamp timestamp;
    private final int humidity;

    public HumidityData(Timestamp timestamp, int humidity) {
        this.timestamp = timestamp;
        this.humidity = humidity;
    }

    @Override
    public Timestamp getTimestamp() {
        return timestamp;
    }

    @Override
    public int getValue() {
        return humidity;
    }
}
