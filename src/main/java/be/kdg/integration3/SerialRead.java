package be.kdg.integration3;

import be.kdg.integration3.domain.*;
import com.fazecast.jSerialComm.SerialPort;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SerialRead {
    private SerialPort port;
    private boolean readingDataValue;
    private int currentValue;
    private char currentDataType;

    private List<RawDataRecord> recordList;

    public SerialRead() {
        this.recordList = new ArrayList<>();
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
            while (port.bytesAvailable() > 0) {
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
                Timestamp recordTimestamp = Timestamp.from(Instant.now());
                switch (currentDataType) {
                    case 'T':
                        recordList.add(new TemperatureData(recordTimestamp, currentValue));
                        newDataCount++;
                        break;
                    case 'H':
                        recordList.add(new HumidityData(recordTimestamp, currentValue));
                        newDataCount++;
                        break;
                    case 'C':
                        recordList.add(new CO2Data(recordTimestamp, currentValue));
                        newDataCount++;
                        break;
                    case 'S':
//                        recordList.add(new SoundData(recordTimestamp, currentValue));
//                        newDataCount++;
                        break;
                }
//                logger.debug("Added new record: {} {}", currentDataType, currentValue);
                currentDataType = ' ';
            }
        }
        return newDataCount;
    }

    public List<RawDataRecord> getRecordList() {
        return recordList;
    }

    public void clearRecordList() {
        recordList = new ArrayList<>();
    }
}
