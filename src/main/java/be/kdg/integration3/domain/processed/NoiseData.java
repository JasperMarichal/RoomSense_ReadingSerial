package be.kdg.integration3.domain.processed;

import be.kdg.integration3.domain.raw.RawDataRecord;

import java.util.Iterator;

public class NoiseData extends SimpleProcessedData {


    public NoiseData(int windowSize, Iterator<RawDataRecord> it) {
        super(windowSize, it);
    }

}
