package be.kdg.integration3.domain;

import java.sql.Timestamp;

public class SoundData implements RawDataRecord{
    private final Timestamp timestamp;
    private final int analogAmplitude;

    public SoundData(Timestamp timestamp, int analogAmplitude) {
        this.timestamp = timestamp;
        this.analogAmplitude = analogAmplitude;
    }

    @Override
    public Timestamp getTimestamp() {
        return timestamp;
    }

    @Override
    public int getValue() {
        return analogAmplitude;
    }
}
