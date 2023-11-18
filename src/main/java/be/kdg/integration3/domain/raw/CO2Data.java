package be.kdg.integration3.domain.raw;

public class CO2Data implements RawDataRecord{

    private final long timestamp;
    private final int analogConcentration;

    public CO2Data(long timestamp, int analogConcentration) {
        this.timestamp = timestamp;
        this.analogConcentration = analogConcentration;
    }

    public static int convertAnalogToPPM(int analog) {
        //TODO: Conversion
        return analog;
    }

    @Override
    public long getTimestamp() {
        return timestamp;
    }

    @Override
    public int getValue() {
        return convertAnalogToPPM(analogConcentration);
    }
}
