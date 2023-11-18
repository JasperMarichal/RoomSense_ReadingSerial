package be.kdg.integration3.domain.raw;

public class HumidityData implements RawDataRecord{

    private final long timestamp;
    private final int humidity;

    public HumidityData(long timestamp, int humidity) {
        this.timestamp = timestamp;
        this.humidity = humidity;
    }

    @Override
    public long getTimestamp() {
        return timestamp;
    }

    @Override
    public int getValue() {
        return humidity;
    }
}
