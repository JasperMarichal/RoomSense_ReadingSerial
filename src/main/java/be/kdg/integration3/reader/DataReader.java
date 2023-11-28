package be.kdg.integration3.reader;

import be.kdg.integration3.domain.raw.*;
import be.kdg.integration3.reader.preprocessor.DataPreprocessor;
import be.kdg.integration3.writer.RawDataWriter;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;

public abstract class DataReader {
    protected final RawDataWriter writer;
    protected final DataPreprocessor preprocessor;
    protected char currentDataType;
    private boolean readingDataValue;
    private int currentValue;

    public DataReader(DataPreprocessor preprocessor, RawDataWriter writer) {
        this.currentDataType = ' ';
        this.writer = writer;
        this.preprocessor = preprocessor;
    }

    public abstract int readData();

    protected int parseData(char[] newSerialData) {
        int newDataCount = 0;
        for(char c : newSerialData) {
            if(c == 'T' || c == 'H' || c == 'C' || c == 'S') {
                currentDataType = c;
                readingDataValue = true;
                currentValue = 0;
            }
            if(readingDataValue && c >= '0' && c <= '9') {
                currentValue *= 10;
                currentValue += (c - '0');
            }
            if(readingDataValue && c == '\n') {
                readingDataValue = false;
                long recordTimestamp = Timestamp.from(Instant.now()).getTime();
                switch (currentDataType) {
                    case 'T' -> newDataCount += enterData(new TemperatureData(recordTimestamp, currentValue));
                    case 'H' -> newDataCount += enterData(new HumidityData(recordTimestamp, currentValue));
                    case 'C' -> newDataCount += enterData(new CO2Data(recordTimestamp, currentValue));
                    case 'S' -> newDataCount += enterData(new SoundData(recordTimestamp, currentValue));
                }
                currentDataType = ' ';
            }
        }
        return newDataCount;
    }

    private int enterData(RawDataRecord newEntry) {
        List<RawDataRecord> keptData = preprocessor.processRawData(newEntry);
        writer.addRawDataEntries(keptData);
        return keptData.size();
    }
}
