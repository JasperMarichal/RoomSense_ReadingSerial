package be.kdg.integration3;

import java.sql.Timestamp;
import java.time.Instant;

public class Main {
    public static void main(String[] args) {
        SerialRead read = new SerialRead();
        JSonWriter writer = new JSonWriter(read);

        System.out.println("Start time " + Timestamp.from(Instant.now()));

        while (!(read.getRecordList().size() > 20)){
            read.readSerial();
        }
        writer.saveAllData();
        read.clearRecordList();

        System.out.println("End time " + Timestamp.from(Instant.now()));
    }
}