package be.kdg.integration3.domain;

import java.sql.Timestamp;

public class CO2Data implements RawDataRecord{

    private final Timestamp timestamp;
    private final int analogConcentration;

    public CO2Data(Timestamp timestamp, int analogConcentration) {
        this.timestamp = timestamp;
        this.analogConcentration = analogConcentration;
    }

    public static int convertAnalogToPPM(int analog) {
        //TODO: Conversion
        return analog;
    }

    @Override
    public Timestamp getTimestamp() {
        return timestamp;
    }

    @Override
    public int getValue() {
        return convertAnalogToPPM(analogConcentration);
    }
}
