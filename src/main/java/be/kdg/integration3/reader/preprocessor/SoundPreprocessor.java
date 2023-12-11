package be.kdg.integration3.reader.preprocessor;

import be.kdg.integration3.domain.processed.NoiseData;
import be.kdg.integration3.domain.processed.SoundSpike;
import be.kdg.integration3.domain.raw.RawDataRecord;
import be.kdg.integration3.domain.raw.SoundData;
import be.kdg.integration3.writer.RawDataWriter;
import be.kdg.integration3.writer.SoundSpikeWriter;

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
 *
 * The sound-spike detection is loosely based on <a href="https://stackoverflow.com/questions/56692066/need-an-algorithm-to-detect-large-spikes-in-oscillating-data">this thread</a>.
 * We first subtract each sample from the sample before it to get the absolute difference,
 * then we take a sum over a small window of the last n values. We can then compare this window
 * sum value to a threshold that is m times the current noise value. The deactivation threshold
 * is smaller than the activation threshold, this way the spike will be more reliably detected
 * since we expect the start of the spike to be much louder and then decay gradually in amplitude.
 * As a final note the deactivation threshold should be calculated with the same noise level
 * as the activation threshold, because otherwise the large spike will greatly influence the
 * noise level and therefore increase the threshold beyond wanted levels, cutting off the end of
 * the spike.
 * </pre>
 */
public class SoundPreprocessor implements DataPreprocessor {
    static final int BUFFER_SIZE = 2500;

    static final int WINDOW_SIZE_NOISE = 1250;
    static final int WINDOW_SIZE_SPIKEDETECTION = 30;
    static final int SPIKEDETECTION_MAX_SPIKELENGTH = 150;
    static final double SPIKEDETECTION_MULT_ACTIVATE = 5;
    static final double SPIKEDETECTION_MIN_ACTIVATE = 100;
    static final double SPIKEDETECTION_MULT_DEACTIVATE = 1.4;
    static final double SPIKEDETECTION_INCLUDED_WINDOW_FACTOR = 0.3;

    private final SoundSpikeWriter soundSpikeWriter;

    List<SoundData> dataBuffer;

    //Moving Average
    int totalSum;

    //Noise
    int totalDiff;
    List<Integer> difference;
    List<RawDataRecord> noiseValues;

    //Spike Detection
    boolean spikeDetected = false;
    double deactivationThreshold;
    SoundData spikeStart;
    int spikeLength = 0;

    public SoundPreprocessor(RawDataWriter complexWriter) {
        if(complexWriter instanceof SoundSpikeWriter) {
            this.soundSpikeWriter = (SoundSpikeWriter) complexWriter;
        }else {
            this.soundSpikeWriter = null;
        }

        this.dataBuffer = new ArrayList<>();
        totalSum = 0;
        totalDiff = 0;
        this.noiseValues = new ArrayList<>();
        this.difference = new ArrayList<>();
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

        int diff = Math.abs(entry.getValue() - avg);
        totalDiff += diff;
        difference.add(diff);
        int noise = totalDiff * 100 / dataBuffer.size();
        noiseValues.add(new SoundData(entry.getTimestamp(), noise));

        if(soundSpikeWriter != null && dataBuffer.size() >= BUFFER_SIZE/2) {
            int windowAvg = 0;
            for(int i = 1; i <= WINDOW_SIZE_SPIKEDETECTION; i++) {
                windowAvg += Math.abs(dataBuffer.get(dataBuffer.size() - 1 - i).getValue() - dataBuffer.get(dataBuffer.size() - i).getValue()) * 100;
            }
            windowAvg /= WINDOW_SIZE_SPIKEDETECTION;

            double activationThreshold = Math.max(SPIKEDETECTION_MIN_ACTIVATE, noise * SPIKEDETECTION_MULT_ACTIVATE);

            if(spikeDetected) {
                dataToKeep.add(entry);
                spikeLength++;
            }
            if(!spikeDetected && windowAvg >= activationThreshold) {
                spikeDetected = true;
                deactivationThreshold = noise * SPIKEDETECTION_MULT_DEACTIVATE;
                int savedWindow = (int) (WINDOW_SIZE_SPIKEDETECTION*SPIKEDETECTION_INCLUDED_WINDOW_FACTOR);
                spikeStart = dataBuffer.get(dataBuffer.size() - savedWindow);
                for(int i = savedWindow; i > 0; i--) {
                    dataToKeep.add(dataBuffer.get(dataBuffer.size() - i));
                    spikeLength++;
                }
                System.out.printf("SPIKE START windowAvg: %d act: %f deact: %f\n", windowAvg, activationThreshold, noise * SPIKEDETECTION_MULT_DEACTIVATE);
            } else if(spikeDetected && (windowAvg <= deactivationThreshold || spikeLength >= SPIKEDETECTION_MAX_SPIKELENGTH)) {
                System.out.printf("SPIKE END windowAvg: %d act: %f deact: %f len: %d\n", windowAvg, activationThreshold, deactivationThreshold, spikeLength);
                spikeDetected = false;
                spikeLength = 0;
                soundSpikeWriter.addComplexData_SoundSpike(new SoundSpike(spikeStart, entry));
            }
        }
    }

    private void onDataRemoved(SoundData entry, List<RawDataRecord> dataToKeep) {
        totalSum -= entry.getValue();

        if(noiseValues.size() > BUFFER_SIZE) {
            dataToKeep.add(new NoiseData(WINDOW_SIZE_NOISE, noiseValues.iterator()));
            for(int i = 0; i < WINDOW_SIZE_NOISE;i++) {
                totalDiff -= difference.remove(0);
                noiseValues.remove(0);
            }
        }

    }
}
