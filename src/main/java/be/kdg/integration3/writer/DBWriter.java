package be.kdg.integration3.writer;

import be.kdg.integration3.domain.processed.NoiseData;
import be.kdg.integration3.domain.processed.SoundSpike;
import be.kdg.integration3.domain.raw.*;

import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DBWriter implements RawDataWriter, SoundSpikeWriter {
    public static final String dbCredentials = System.getenv("rs_db_cred");

    private final Connection connection;
    private final Integer roomId;
    private final int batchSize;

    private final List<RawDataRecord> recordList;
    private final List<SoundSpike> complex_soundSpikes;

    /**
     * A writer that writes to a (postgresql) database,
     * requires an environmental variable <b>rs_db_cred</b>
     * to be set to the jdbc url of the database with
     * database credentials included.
     * @param roomId the database id of the room the associated device is in
     * @param batchSize how many raw data records to collect before saving to the database
     * @throws RuntimeException If the database connection could not be achieved
     */
    public DBWriter(Integer roomId, int batchSize) throws RuntimeException {
        this.roomId = roomId;
        this.batchSize = batchSize;
        this.recordList = new ArrayList<>();
        this.complex_soundSpikes = new ArrayList<>();
        if(dbCredentials == null || dbCredentials.isBlank()) {
            throw new RuntimeException("Database credentials are not defined: " +
                    "make sure the rs_db_cred environment variable is set.");
        }
        try {
            System.out.println("Attempting to connect to database...");
            connection = DriverManager.getConnection(dbCredentials);
            connection.setAutoCommit(false);
            System.out.println("Database connection established.");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void saveAllData() {
        if(recordList.size() > batchSize) {
            long saveStart = System.currentTimeMillis();
            int affectedRows = 0;

            List<RawDataRecord> temperature = recordList.stream().filter(record -> record instanceof TemperatureData).toList();
            List<RawDataRecord> humidity = recordList.stream().filter(record -> record instanceof HumidityData).toList();
            List<RawDataRecord> sound = recordList.stream().filter(record -> record instanceof SoundData).toList();
            List<RawDataRecord> co2 = recordList.stream().filter(record -> record instanceof CO2Data).toList();
            List<RawDataRecord> noise = recordList.stream().filter(record -> record instanceof NoiseData).toList();

            affectedRows += saveEntries("temperature_entry", connection, roomId, temperature);
            affectedRows += saveEntries("humidity_entry", connection, roomId, humidity);
            affectedRows += saveEntries("raw_sound_entry", connection, roomId, sound);
            affectedRows += saveEntries("co2_entry", connection, roomId, co2);
            affectedRows += saveEntries("noise_entry", connection, roomId, noise);

            affectedRows += saveComplexData();

            recordList.clear();
            System.out.printf("Save completed in %d ms affected rows: %d\n", System.currentTimeMillis() - saveStart, affectedRows);
        }
    }

    @Override
    public void addRawDataEntry(RawDataRecord rawDataRecord) {
        recordList.add(rawDataRecord);
    }

    @Override
    public void addRawDataEntries(List<RawDataRecord> rawDataRecordList) {
        recordList.addAll(rawDataRecordList);
    }

    @Override
    public List<RawDataRecord> getRecordList() {
        return recordList;
    }

    private static int saveEntries(String entry_table, Connection db, Integer room_id, List<RawDataRecord> values) {
        String sql = "INSERT INTO "+entry_table+" (room_id,value,timestamp) VALUES (?,?,?);";
        try (PreparedStatement preparedStatement = db.prepareStatement(sql)) {
            for(RawDataRecord dataRecord : values) {
                if (room_id == null) {
                    preparedStatement.setNull(1, Types.INTEGER);
                } else {
                    preparedStatement.setInt(1, room_id);
                }

                preparedStatement.setInt(2, dataRecord.getValue());
                preparedStatement.setTimestamp(3, microsToTimestamp(dataRecord.getTimestamp()));

                preparedStatement.addBatch();
            }
            int[] affectedRows = preparedStatement.executeBatch();

            db.commit();
            return Arrays.stream(affectedRows).sum();
        }catch (SQLException e) {
            //throw new RuntimeException(e);
            return 0;
        }
    }

    @Override
    public void addComplexData_SoundSpike(SoundSpike spike) {
        complex_soundSpikes.add(spike);
    }

    private int saveComplexData() {
        int affectedRows = 0;

        affectedRows += saveSoundSpikes(complex_soundSpikes, connection, roomId);

        complex_soundSpikes.clear();
        return affectedRows;
    }

    private static int saveSoundSpikes(List<SoundSpike> spikes, Connection db, Integer room_id) {
        String sql = "INSERT INTO sound_spike (room_id, start_entry, end_entry) VALUES (?,?,?);";
        try (PreparedStatement preparedStatement = db.prepareStatement(sql)) {
            for(SoundSpike spike : spikes) {
                if (room_id == null) {
                    preparedStatement.setNull(1, Types.INTEGER);
                } else {
                    preparedStatement.setInt(1, room_id);
                }

                preparedStatement.setTimestamp(2, microsToTimestamp(spike.getEntryStart().getTimestamp()));
                preparedStatement.setTimestamp(3, microsToTimestamp(spike.getEntryEnd().getTimestamp()));

                preparedStatement.addBatch();
            }
            int[] affectedRows = preparedStatement.executeBatch();

            db.commit();
            return Arrays.stream(affectedRows).sum();
        }catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private static Timestamp microsToTimestamp(long micros) {
        long secondStartMillis = (micros/1000000L) * 1000L;
        Timestamp timestamp = new Timestamp(secondStartMillis);
        timestamp.setNanos((int) ((micros - (secondStartMillis*1000L))*1000L));
        return timestamp;
    }
}
