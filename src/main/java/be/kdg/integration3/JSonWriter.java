package be.kdg.integration3;

import be.kdg.integration3.domain.*;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.FileWriter;
import java.io.IOException;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;


public class JSonWriter {

    private final SerialRead serial;
    GsonBuilder gsonBuilder;

    public JSonWriter(SerialRead serialRead) {
        this.serial = serialRead;
        gsonBuilder = new GsonBuilder();
//        gsonBuilder.setPrettyPrinting();
    }

    public void saveAllData() {
        List<RawDataRecord> data = serial.getRecordList();

        List<TemperatureData> temperature = data.stream().filter(record -> record instanceof TemperatureData).map(rawDataRecord -> (TemperatureData) rawDataRecord).toList();
        List<HumidityData> humidity = data.stream().filter(record -> record instanceof HumidityData).map(rawDataRecord -> (HumidityData) rawDataRecord).toList();
        List<SoundData> sound = data.stream().filter(record -> record instanceof SoundData).map(rawDataRecord -> (SoundData) rawDataRecord).toList();
        List<CO2Data> CO2 = data.stream().filter(record -> record instanceof CO2Data).map(rawDataRecord -> (CO2Data) rawDataRecord).toList();

        saveTemperature(temperature);
        saveHumidity(humidity);
//        saveSound(sound);
//        saveCO2(CO2);
    }

    private void saveTemperature(List<TemperatureData> temperatureData){
        Timestamp timestamp = Timestamp.from(Instant.now());
        String name = "temperature" + timestamp.getTime();

        saveToJson(temperatureData, name);
    }

    private void saveHumidity(List<HumidityData> humidityData){
        Timestamp timestamp = Timestamp.from(Instant.now());
        String name = "humidity" + timestamp.getTime();

        saveToJson(humidityData, name);
    }

    private void saveSound(List<SoundData> soundData){
        Timestamp timestamp = Timestamp.from(Instant.now());
        String name = "sound" + timestamp.getTime();

        saveToJson(soundData, name);
    }

    private void saveCO2(List<CO2Data> CO2Data){
        Timestamp timestamp = Timestamp.from(Instant.now());
        String name = "co2" + timestamp.getTime();

        saveToJson(CO2Data, name);
    }


    private void saveToJson(List objectList, String name){
        Gson gson = gsonBuilder.create();
        String jsonString = gson.toJson(objectList);

        try (FileWriter jsonWriter = new FileWriter("..\\JSONSaves\\"+ name +".json")) {
            jsonWriter.write(jsonString);
            System.out.println("Data is saved to " + name + ".json...");
        } catch (IOException e) {
            System.out.println("Unable to save " + name + "to json");
            e.printStackTrace();
        }
    }
}
