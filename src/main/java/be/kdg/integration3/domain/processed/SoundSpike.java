package be.kdg.integration3.domain.processed;

import be.kdg.integration3.domain.raw.SoundData;

public class SoundSpike {
    private final SoundData entryStart;
    private final SoundData entryEnd;

    public SoundSpike(SoundData startEntry, SoundData endEntry) {
        this.entryStart = startEntry;
        this.entryEnd = endEntry;
    }

    public SoundData getEntryStart() {
        return entryStart;
    }

    public SoundData getEntryEnd() {
        return entryEnd;
    }
}
