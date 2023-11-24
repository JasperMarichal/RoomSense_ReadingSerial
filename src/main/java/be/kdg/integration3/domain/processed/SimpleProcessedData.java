package be.kdg.integration3.domain.processed;

import be.kdg.integration3.domain.raw.RawDataRecord;

import java.util.Iterator;

public class SimpleProcessedData implements RawDataRecord {
    protected final int windowSize;
    protected final int average;
    protected final long timestamp;

    public SimpleProcessedData(int windowSize, Iterator<RawDataRecord> it) {
        this.windowSize = windowSize;
        int totVal = 0;
        long totTimestamp = 0;
        for(int i = 0; i < windowSize;i++) {
            RawDataRecord record = it.next();
            totVal += record.getValue();
            totTimestamp += record.getTimestamp();
        }
        this.average = totVal / windowSize;
        this.timestamp = totTimestamp / windowSize;
    }

    public int getWindowSize() {
        return windowSize;
    }

    public int getValue() {
        return average;
    }

    public long getTimestamp() {
        return timestamp;
    }
}
