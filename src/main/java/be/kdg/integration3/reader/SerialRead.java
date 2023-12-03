package be.kdg.integration3.reader;

import be.kdg.integration3.reader.preprocessor.DataPreprocessor;
import be.kdg.integration3.writer.RawDataWriter;
import com.fazecast.jSerialComm.SerialPort;

import java.util.Arrays;

public class SerialRead extends DataReader {
    private SerialPort port;

    public SerialRead(DataPreprocessor preprocessor, RawDataWriter writer) {
        super(preprocessor, writer);
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

    @Override
    public int readData() {
        try {
            if(port.bytesAvailable() > 0) {
                byte[] readBuffer = new byte[port.bytesAvailable()];
                int numRead = port.readBytes(readBuffer, readBuffer.length);
                char[] readChars = new char[numRead];
                for(int i = 0; i < readChars.length; i++) {
                    readChars[i] = (char)readBuffer[i];
                }
                return parseData(readChars);
            }
        } catch (Exception e) {
            System.out.println(Arrays.toString(e.getStackTrace()));
        }
        return 0;
    }

}
