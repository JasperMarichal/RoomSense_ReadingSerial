package be.kdg.integration3;

import be.kdg.integration3.domain.raw.*;

import java.sql.*;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class DBWriter implements DataWriter {
    public static final String dbCredentials = System.getenv("rs_db_cred");

    private final SerialRead serial;
    private final Connection connection;
    private Integer roomId;

    public DBWriter(SerialRead serialRead, Integer roomId) throws RuntimeException {
        this.roomId = roomId;
        this.serial = serialRead;
        if(dbCredentials.isBlank()) {
            throw new RuntimeException("Database credentials are not defined: " +
                    "make sure the rs_db_cred environment variable is set.");
        }
        try {
            connection = DriverManager.getConnection(dbCredentials);
            connection.setAutoCommit(false);
            System.out.println("Database connection established.");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void saveAllData() {
        List<RawDataRecord> data = serial.getRecordList();

        List<RawDataRecord> temperature = data.stream().filter(record -> record instanceof TemperatureData).toList();
        List<RawDataRecord> humidity = data.stream().filter(record -> record instanceof HumidityData).toList();
        List<RawDataRecord> sound = data.stream().filter(record -> record instanceof SoundData).toList();
        List<RawDataRecord> co2 = data.stream().filter(record -> record instanceof CO2Data).toList();

        saveEntries("temperature_entry", roomId, temperature);
        saveEntries("humidity_entry", roomId, humidity);
        saveEntries("raw_sound_entry", roomId, sound);
        saveEntries("co2_entry", roomId, co2);
        //saveEntries("noise_entry", roomId, noise);

    }

    public void saveEntries(String entry_table, Integer room_id, List<RawDataRecord> values) {
        String sql = "INSERT INTO "+entry_table+" (room_id,value,timestamp) VALUES (?,?,?);";
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            for(RawDataRecord dataRecord : values) {
                if (room_id == null) {
                    preparedStatement.setNull(1, Types.INTEGER);
                } else {
                    preparedStatement.setInt(1, room_id);
                }

                preparedStatement.setInt(2, dataRecord.getValue());
                preparedStatement.setTimestamp(3, new Timestamp(dataRecord.getTimestamp()));

                preparedStatement.addBatch();
            }
            int[] affectedRows = preparedStatement.executeBatch();

            connection.commit();
        }catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
