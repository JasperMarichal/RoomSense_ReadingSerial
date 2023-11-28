package be.kdg.integration3.reader;

import be.kdg.integration3.reader.preprocessor.DataPreprocessor;
import be.kdg.integration3.writer.RawDataWriter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.Arrays;

public class TelnetRead extends DataReader {
    private static final String deviceIp = System.getenv("rs_ip");
    private static final String devicePort = System.getenv("rs_port");
    private final Socket arduinoSocket;
    private final InputStream arduinoInput;
    private final BufferedReader arduinoBufferedRead;

    public TelnetRead(DataPreprocessor preprocessor, RawDataWriter writer) {
        super(preprocessor, writer);

        try {
            arduinoSocket = new Socket(deviceIp, Integer.parseInt(devicePort));
            arduinoInput = arduinoSocket.getInputStream();
            InputStreamReader arduinoInputCharacter = new InputStreamReader(arduinoInput);
            arduinoBufferedRead = new BufferedReader(arduinoInputCharacter);
        } catch (IOException io) {

            throw new RuntimeException(io);
        }

    }

    @Override
    public int readData() {
        try {
            if (arduinoBufferedRead.ready()){
                String line = arduinoBufferedRead.readLine();
                char[] input = line.toCharArray();
                return parseData(input);
            }
        } catch (IOException e){
            System.out.println(Arrays.toString(e.getStackTrace()));
        }
        return 0;
    }
}
