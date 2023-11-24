package be.kdg.integration3;

import be.kdg.integration3.domain.processed.NoiseData;
import be.kdg.integration3.domain.raw.RawDataRecord;
import be.kdg.integration3.domain.raw.SoundData;

import java.util.ArrayList;
import java.util.List;

/**
 * <pre>
 * This helper class does some preprocessing on the raw sound data to make it more useful,
 * and to reduce the amount of data that has to be transmitted to the database (and stored).
 *
 * The preprocessor firstly has an internal moving average of the sound level, this way
 * our sensor does not have to be calibrated too precisely since everything will be compared to
 * this average value.
 *
 * The noise is calculated as the absolute difference from the moving average. (=mean absolute deviation)
 * Because we do not wish to store as many noise values as there are raw sound entries,
 * this value will be averaged over a small window (reducing the dataset's size by that windowSize),
 * this is performed automatically by the SimpleProcessedData (super)class using an iterator.
 * </pre>
 */
public class SoundPreprocessor implements DataPreprocessor {
    static final int BUFFER_SIZE = 2500;

    static final int WINDOW_SIZE_NOISE = 500;

    List<SoundData> dataBuffer;

    //Moving Average
    int totalSum;

    //Noise
    int totalDiff;
    List<RawDataRecord> noiseValues;

    public SoundPreprocessor() {
        this.dataBuffer = new ArrayList<>();

        totalSum = 0;

        totalDiff = 0;
        this.noiseValues = new ArrayList<>();
    }

    @Override
    public List<RawDataRecord> processRawData(RawDataRecord raw) {
        List<RawDataRecord> dataToKeep = new ArrayList<>();
        if(!(raw instanceof SoundData)) {
            // This preprocessor does not know about any other type of raw data, so it keeps it by default
            // (this makes it possible for later to chain/have combined preprocessors if needed)
            dataToKeep.add(raw);
            return dataToKeep;
        }

        dataBuffer.add((SoundData) raw);
        onDataAdded(dataBuffer.get(dataBuffer.size()-1), dataToKeep);
        if(dataBuffer.size() > BUFFER_SIZE) {
            onDataRemoved(dataBuffer.remove(0), dataToKeep);
        }

        return dataToKeep;
    }

    private void onDataAdded(SoundData entry, List<RawDataRecord> dataToKeep) {
        totalSum += entry.getValue();
        int avg = totalSum / dataBuffer.size();

        totalDiff += Math.abs(entry.getValue() - avg);
        int noise = totalDiff / dataBuffer.size();
        noiseValues.add(new SoundData(entry.getTimestamp(), noise));


    }

    private void onDataRemoved(SoundData entry, List<RawDataRecord> dataToKeep) {
        totalSum -= entry.getValue();

        if(noiseValues.size() > BUFFER_SIZE) {
            dataToKeep.add(new NoiseData(WINDOW_SIZE_NOISE, noiseValues.iterator()));
            for(int i = 0; i < WINDOW_SIZE_NOISE;i++) {
                totalDiff -= noiseValues.remove(noiseValues.size() - 1).getValue();
            }
        }


    }
}
