package be.kdg.integration3;

import java.sql.Timestamp;
import java.time.Instant;

public class Main {
    public static void main(String[] args) {
        SerialRead read = new SerialRead();
        DataWriter writer;
        try {
            writer = new DBWriter(read, null);
        }catch (RuntimeException e) {
            System.err.println(e.getMessage());
            System.err.println("Falling back to JsonWriter...");
            writer = new JsonWriter(read);
        }


        while (true) {
            System.out.println("Start time " + Timestamp.from(Instant.now()));

            while (!(read.getRecordList().size() > 30)) {
                read.readSerial();
            }
            writer.saveAllData();
            read.clearRecordList();

            System.out.println("End time " + Timestamp.from(Instant.now()));
        }
    }
}
