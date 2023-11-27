package be.kdg.integration3.writer;

import be.kdg.integration3.domain.raw.RawDataRecord;

import java.util.List;

public interface RawDataWriter {

    void saveAllData();

    void addRawDataEntry(RawDataRecord rawDataRecord);
    void addRawDataEntries(List<RawDataRecord> rawDataRecordList);

    List<RawDataRecord> getRecordList();
}
