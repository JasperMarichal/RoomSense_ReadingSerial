package be.kdg.integration3.reader;

import be.kdg.integration3.domain.raw.*;
import be.kdg.integration3.reader.preprocessor.DataPreprocessor;
import be.kdg.integration3.writer.RawDataWriter;

import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;

public class TelnetRead{
    Socket arduinoSocket;
    InputStream arduinoInput;
    BufferedReader arduinoBufferedRead;
    private final String deviceIp = System.getenv("rs_ip");
    private final String devicePort = System.getenv("rs_port");

    private boolean readingDataValue;
    private int currentValue;
    private char currentDataType;
    private final RawDataWriter writer;
    private final DataPreprocessor preprocessor;

    public TelnetRead(DataPreprocessor preprocessor, RawDataWriter writer) {
        this.preprocessor = preprocessor;
        this.writer = writer;
        initTelnet();
        this.currentDataType = ' ';
    }

    private void initTelnet() {
        try {
            arduinoSocket = new Socket(deviceIp, Integer.parseInt(devicePort));
            arduinoInput = arduinoSocket.getInputStream();
            InputStreamReader arduinoInputCharacter = new InputStreamReader(arduinoInput);
            arduinoBufferedRead = new BufferedReader(arduinoInputCharacter);
        } catch (IOException io){
            System.out.println(Arrays.toString(io.getStackTrace()));
        }
    }

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


    private int parseData(char[] newSerialData) {
        int newDataCount = 0;
        for(char c : newSerialData) {
            if(c == 'T' || c == 'H' || c == 'C' || c == 'S') {
                currentDataType = c;
                readingDataValue = true;
                currentValue = 0;
            }
            if(readingDataValue && c >= '0' && c <= '9'){
                currentValue *= 10;
                currentValue += (c - '0');
            }
            if(readingDataValue && c == '\n') {
                readingDataValue = false;
                long recordTimestamp = Timestamp.from(Instant.now()).getTime();
                switch (currentDataType) {
                    case 'T' -> newDataCount += enterData(new TemperatureData(recordTimestamp, currentValue));
                    case 'H' -> newDataCount += enterData(new HumidityData(recordTimestamp, currentValue));
                    case 'C' -> newDataCount += enterData(new CO2Data(recordTimestamp, currentValue));
                    case 'S' -> newDataCount += enterData(new SoundData(recordTimestamp, currentValue));
                }
                currentDataType = ' ';
            }
        }
        return newDataCount;
    }

    private int enterData(RawDataRecord newEntry) {
        List<RawDataRecord> keptData = preprocessor.processRawData(newEntry);
        writer.addRawDataEntries(keptData);
        return keptData.size();
    }
}
