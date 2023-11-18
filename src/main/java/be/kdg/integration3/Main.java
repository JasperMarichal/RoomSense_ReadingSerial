package be.kdg.integration3;

import java.sql.Timestamp;
import java.time.Instant;

public class Main {
    public static void main(String[] args) {
        SerialRead read = new SerialRead();
        JsonWriter writer = new JsonWriter(read);

        while (true) {
            System.out.println("Start time " + Timestamp.from(Instant.now()));

            while (!(read.getRecordList().size() > 30)) {
                read.readSerial();
            }
//            System.out.println(read.getRecordList());
            writer.saveAllData();
            read.clearRecordList();

            System.out.println("End time " + Timestamp.from(Instant.now()));
        }
    }
}