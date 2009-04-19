package fajitaboy.audio;

import static fajitaboy.constants.AddressConstants.*;

import fajitaboy.memory.AddressBus;

/**
 * Represents Game Boys second sound channel. Generates a square wave with
 * envelope functions.
 *
 * @author Adam Hulin, Johan Gustafsson
 *
 */
public class SoundChannel2 {

    /**
     * The sample rate the sound should be sampled.
     */
    private float sampleRate;

    /**
     * The old frequency that the last tone had.
     */
    private int oldFreq;

    /**
     * The waves current amplitude.
     */
    private int amp;

    /**
     * The address bus.
     */
    private AddressBus ab;

    /**
     * The frequency of the tone.
     */
    private int freq;

    /**
     * The current position in the wave length.
     */
    private int pos;

    /**
     * The length of the wave in samples.
     */
    private int waveLength;

    /**
     * The number of samples a wave length should be in high position.
     */
    private int dutyLength;

    /**
     * Used in envelope, indicates how much the amplitude should change.
     */
    private int envelopeStep;

    /**
     * The current position in the envelope.
     */
    private int envelopePos;

    /**
     * The number of step an envelope function should take.
     */
    private int envelopeStepLength;

    /**
     * Flag that indicates of toneLength is on or off.
     */
    private boolean lengthEnabled;

    /**
     * The length of the tone in samples.
     */
    private int toneLength;

    /**
     * Constructor for SoundChannel 2.
     *
     * @param ab
     *            The address bus.
     * @param sampleRate
     *            The sample rate.
     */
    public SoundChannel2(final AddressBus ab, final float sampleRate) {
        this.ab = ab;
        this.sampleRate = sampleRate;
        pos = 0;
        oldFreq = 0;
        amp = 32;
        lengthEnabled = false;
    }

    /**
     * Generates a tone sequence.
     *
     * @param destBuff
     *            The array that the sound should be added to.
     * @param left
     *            Flag for left output.
     * @param right
     *            Flag for right output.
     * @param samples
     *            The number of samples that should be generated.
     * @return The destBuff with sound added to it.
     */
    public byte[] generateTone(final byte[] destBuff, final boolean left,
            final boolean right, final int samples) {
        calcFreq();
        if (((ab.read(NR22_REGISTER) & 0xF0) >> 4) == 0) {
            return destBuff;
        }

        if (/*(toneLength > 0 && lengthEnabled) || !lengthEnabled*/true) {
            if (lengthEnabled) {
                toneLength -= samples;
            }
            int finalAmp;
            int k = 0;
            for (int i = 0; i < samples; i++) {

                // Envelope
                if (envelopeStepLength != 0) {
                    if ((envelopePos % (envelopeStepLength * samples)) == 0) {
                        if ((amp > 0) && (amp < 32)) {
                            amp += envelopeStep;
                        }
                    }
                    envelopePos++;
                }

                if (pos < dutyLength) {
                    finalAmp = -amp;
                }

                else {
                    finalAmp = amp;
                }
                if (left) {
                    destBuff[k] += (byte) finalAmp;
                }
                k++;
                if (right) {
                    destBuff[k] += (byte) finalAmp;
                }
                k++;
                pos = (pos + 1) % waveLength;
            }
        }
        return destBuff;
    }

    /**
     * Calculates the envelope parameters.
     */
    private void calcEnvelope() {
        int nr22 = ab.read(NR22_REGISTER);
        amp = ((nr22 & 0xF0) >> 4) * 2;
        envelopeStepLength = nr22 & 0x7;
        int direction = nr22 & 0x8;
        if (direction == 0) {
            envelopeStep = -2;
        } else {
            envelopeStep = 2;
        }
        envelopePos = 0;
    }

    /**
     * Checks if the frequency has changed, if so it calculates all related
     * parameters.
     */
    private void calcFreq() {
        int low1 = ab.read(SOUND2_LOW);
        int high1 = ab.read(SOUND2_HIGH) * 0x100;
        int tmp = (2047 - (high1 + low1) & 0x7ff);
        if (tmp != 0) {
            freq = 131072 / tmp;
        } else {
            freq = 131072;
        }
        if (freq == oldFreq) {
            return;
        } else {
            calcToneLength();
            calcEnvelope();
            dutyLength = calcWavePattern();
            oldFreq = freq;
        }
    }

    /**
     * Calculates the wave pattern.
     */
    private int calcWavePattern() {
        waveLength = (int) ((sampleRate) / (float) freq);
        if (waveLength == 0) {
            waveLength = 1;
        }
        int nr21 = ((ab.read(NR21_REGISTER) & 0xC0) >> 6);
        switch (nr21) {
        case 0:
            return (int) ((float) waveLength * 0.125);
        case 1:
            return (int) ((float) waveLength * 0.25);
        case 2:
            return (int) ((float) waveLength * 0.50);
        case 3:
            return (int) ((float) waveLength * 0.75);
        default:
            return 0;
        }
    }

    /**
     * Checks if calculate if tone length is enabled and if it is calculates the
     * tone length.
     */
    private void calcToneLength() {
        lengthEnabled = ((ab.read(NR24_REGISTER) & 0x40) > 0);
        if (lengthEnabled) {
            toneLength = (int) (((64 - ((double)
                    (ab.read(NR21_REGISTER) & 0x3F))) / 256) * sampleRate);
        }
    }

}