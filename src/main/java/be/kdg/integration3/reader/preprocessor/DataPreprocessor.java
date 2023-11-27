package be.kdg.integration3.reader.preprocessor;

import be.kdg.integration3.domain.raw.RawDataRecord;

import java.util.List;

public interface DataPreprocessor {

    /**
     * Preprocesses the corresponding raw data to filter out unnecessary entries.
     * @param raw The next raw data entry
     * @return Raw data entries that should be kept
     */
    List<RawDataRecord> processRawData(RawDataRecord raw);
}
