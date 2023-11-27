package be.kdg.integration3.reader;

import be.kdg.integration3.reader.preprocessor.DataPreprocessor;
import be.kdg.integration3.domain.raw.*;
import be.kdg.integration3.writer.RawDataWriter;
import com.fazecast.jSerialComm.SerialPort;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;

public class SerialRead {
    private SerialPort port;
    private boolean readingDataValue;
    private int currentValue;
    private char currentDataType;

    private final RawDataWriter writer;
    private final DataPreprocessor preprocessor;

    public SerialRead(DataPreprocessor preprocessor, RawDataWriter writer) {
        this.preprocessor = preprocessor;
        this.writer = writer;
        initSerial();
        this.currentDataType = ' ';
    }

    private void initSerial() {
        SerialPort[]  commPorts = SerialPort.getCommPorts();
        System.out.println("List COM ports");
        for(int i = 0; i < commPorts.length; i++) {
            System.out.println("comPorts[" + i + "] = " + commPorts[i].getDescriptivePortName());
        }
        SerialPort port = commPorts[0];     // array index to select COM port
        port.setBaudRate(115200);
        port.openPort();
        this.port = port;
    }

    public int readSerial() {
        try {
            if(port.bytesAvailable() > 0) {
                byte[] readBuffer = new byte[port.bytesAvailable()];
                int numRead = port.readBytes(readBuffer, readBuffer.length);
                char[] readChars = new char[numRead];
                for(int i = 0; i < readChars.length; i++) {
                    readChars[i] = (char)readBuffer[i];
                }
                return parseSerial(readChars);
            }
        } catch (Exception e) {
            System.out.println(Arrays.toString(e.getStackTrace()));
        }
        return 0;
    }

    private int parseSerial(char[] newSerialData) {
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
