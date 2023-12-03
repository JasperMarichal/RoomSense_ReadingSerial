package be.kdg.integration3;

import be.kdg.integration3.reader.DataReader;
import be.kdg.integration3.reader.TelnetRead;
import be.kdg.integration3.reader.preprocessor.DataPreprocessor;
import be.kdg.integration3.reader.preprocessor.SoundPreprocessor;
import be.kdg.integration3.writer.DBWriter;
import be.kdg.integration3.writer.JsonWriter;
import be.kdg.integration3.writer.RawDataWriter;

public class Main {
    public static final int SAVE_BATCH_SIZE = 50;

    public static void main(String[] args) {
        String ip = "";
        int port = 0;
        int roomId = 0;

        try {
            for (int i = 0; i < args.length; i++) {
                System.out.println("Argument " + i + ": " + args[i]);
                String arg = args[i];
                if (arg.startsWith("ip=")) {
                    ip = arg.substring(arg.lastIndexOf("=") + 1);
                } else if (arg.startsWith("port=")) {
                    port = Integer.parseInt(arg.substring(arg.lastIndexOf("=") + 1));
                } else if (arg.startsWith("room=")) {
                    roomId = Integer.parseInt(arg.substring(arg.lastIndexOf("=") + 1));
                }
            }
        } catch (NumberFormatException e){
            System.out.println("Port and Room must be entered as a number!");
        }

        if (port == 0 || roomId == 0 || ip.isEmpty()){
            System.err.println("You have not entered at least one of the runtime variables");
            System.out.println("Ensure you have defined 'ip=ipAddress port=portNumber room=roomId'");
            return;
        }

        RawDataWriter writer;
        try {
            writer = new DBWriter(roomId, SAVE_BATCH_SIZE);
        } catch (RuntimeException e) {
            System.err.println(e.getMessage());
            System.err.println("Falling back to JsonWriter...");
            writer = new JsonWriter(SAVE_BATCH_SIZE);
        }

        DataPreprocessor preprocessor = new SoundPreprocessor(writer);
        DataReader reader = new TelnetRead(preprocessor, writer, ip, port);

        while (true) {
            reader.readData();
            writer.saveAllData();
        }
    }
}
