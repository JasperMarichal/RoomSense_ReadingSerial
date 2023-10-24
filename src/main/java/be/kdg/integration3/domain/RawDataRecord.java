package be.kdg.integration3.domain;

import java.sql.Timestamp;

public interface RawDataRecord {
    public Timestamp getTimestamp();
    public int getValue();
}
