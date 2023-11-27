package be.kdg.integration3.writer;

import be.kdg.integration3.domain.raw.*;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.FileWriter;
import java.io.IOException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;


public class JsonWriter implements RawDataWriter {
    private final GsonBuilder gsonBuilder;

    private final int batchSize;

    private final List<RawDataRecord> data;

    public JsonWriter(int batchSize) {
        this.batchSize = batchSize;
        data = new ArrayList<>();
        gsonBuilder = new GsonBuilder();
    }

    @Override
    public void saveAllData() {
        if(data.size() > batchSize) {
            List<TemperatureData> temperature = data.stream().filter(record -> record instanceof TemperatureData).map(rawDataRecord -> (TemperatureData) rawDataRecord).toList();
            List<HumidityData> humidity = data.stream().filter(record -> record instanceof HumidityData).map(rawDataRecord -> (HumidityData) rawDataRecord).toList();
            List<SoundData> sound = data.stream().filter(record -> record instanceof SoundData).map(rawDataRecord -> (SoundData) rawDataRecord).toList();
            List<CO2Data> CO2 = data.stream().filter(record -> record instanceof CO2Data).map(rawDataRecord -> (CO2Data) rawDataRecord).toList();

            saveTemperature(temperature);
            saveHumidity(humidity);
//            saveSound(sound);
            saveCO2(CO2);
            data.clear();
        }
    }

    @Override
    public void addRawDataEntry(RawDataRecord rawDataRecord) {
        data.add(rawDataRecord);
    }

    @Override
    public void addRawDataEntries(List<RawDataRecord> rawDataRecordList) {
        data.addAll(rawDataRecordList);
    }

    @Override
    public List<RawDataRecord> getRecordList() {
        return data;
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
        String name = "CO2" + timestamp.getTime();

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
