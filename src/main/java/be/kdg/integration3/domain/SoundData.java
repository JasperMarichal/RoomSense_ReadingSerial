package be.kdg.integration3.domain;

import java.sql.Timestamp;

public class SoundData implements RawDataRecord{
    private final long timestamp;
    private final int analogAmplitude;

    public SoundData(long timestamp, int analogAmplitude) {
        this.timestamp = timestamp;
        this.analogAmplitude = analogAmplitude;
    }

    @Override
    public long getTimestamp() {
        return timestamp;
    }

    @Override
    public int getValue() {
        return analogAmplitude;
    }
}
