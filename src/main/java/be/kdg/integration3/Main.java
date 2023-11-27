package be.kdg.integration3;

import be.kdg.integration3.reader.SerialRead;
import be.kdg.integration3.reader.preprocessor.DataPreprocessor;
import be.kdg.integration3.reader.preprocessor.SoundPreprocessor;
import be.kdg.integration3.writer.DBWriter;
import be.kdg.integration3.writer.JsonWriter;
import be.kdg.integration3.writer.RawDataWriter;

public class Main {
    public static final int SAVE_BATCH_SIZE = 50;

    public static void main(String[] args) {
        RawDataWriter writer;
        try {
            writer = new DBWriter(null, SAVE_BATCH_SIZE);
        }catch (RuntimeException e) {
            System.err.println(e.getMessage());
            System.err.println("Falling back to JsonWriter...");
            writer = new JsonWriter(SAVE_BATCH_SIZE);
        }

        DataPreprocessor preprocessor = new SoundPreprocessor(writer);
        SerialRead read = new SerialRead(preprocessor, writer);

        while (true) {
            read.readSerial();
            writer.saveAllData();
        }
    }
}
