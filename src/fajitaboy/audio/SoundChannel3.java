package fajitaboy.audio;

import static fajitaboy.constants.AddressConstants.*;

import fajitaboy.memory.AddressBus;

/**
 * Represents Game Boys third sound channel. Generates a wave built up after an
 * given pattern.
 *
 * @author Adam Hulin, Johan Gustafsson
 *
 */
public class SoundChannel3 {

    /**
     * The sample rate the sound should be sampled.
     */
    private float sampleRate;

    /**
     * The frequency of the tone.
     */
    private int freq;

    /**
     * The old frequency that the last tone had.
     */
    private int oldFreq;

    /**
     * The length of the wave in samples.
     */
    private int waveLength;

    /**
     * The array that holds the pattern for a wavelength.
     */
    private byte[] wavePattern;

    /**
     * The address bus
     */
    private AddressBus ab;

    /**
     * The current position in the wave length.
     */
    private int pos;

    /**
     * Flag that indicates of toneLength is on or off.
     */
    private boolean lengthEnabled;

    /**
     * The length of the tone in samples.
     */
    private int toneLength;

    /**
     * Constructor for SoundChannel 3.
     *
     * @param ab
     *            The address bus.
     * @param sampleRate
     *            The sample rate.
     */
    public SoundChannel3(final AddressBus ab, final float sampleRate) {
        this.sampleRate = sampleRate;
        this.ab = ab;
        oldFreq = 0;
        pos = 0;
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
    public final byte[] generateTone(final byte[] destBuff, final boolean left,
            final boolean right, final int samples) {
        if ((ab.read(NR30_REGISTER) & 0x80) == 0) {
            return destBuff;
        }

        calcFreq();
        if ((toneLength > 0 && lengthEnabled) || !lengthEnabled) {
            if (lengthEnabled) {
                toneLength -= samples;
            }
            int shift = calcShift();
            int k = 0;
            byte amp;
            for (int i = 0; i < samples; i++) {
                // Read the wave pattern array in a modulus fashion.
                amp = (byte)
                    (wavePattern[((32 * pos) / waveLength) % 32] >> shift);
                if (left) {
                    destBuff[k] += amp;
                }
                k++;
                if (right) {
                    destBuff[k] += amp;
                }
                k++;

                pos = (pos + 1) % waveLength;
            }

        }

        if (toneLength < 0 && lengthEnabled && 
                (ab.read(NR31_REGISTER) & 0x100) == 0) {
            calcToneLength();
        }

        return destBuff;
    }

    /**
     * Checks if the frequency has changed, if so it calculates all related
     * parameters.
     */
    private void calcFreq() {
        int low1 = ab.read(SOUND3_LOW);
        int high1 = ab.read(SOUND3_HIGH) * 0x100;
        int tmp = (2047 - (high1 + low1) & 0x7ff);
        if (tmp != 0) {
            freq = 65536 / tmp;
        } else {
            freq = 65536;
        }
        if (freq == oldFreq) {
            return;
        } else {
            calcToneLength();
            calcWavePattern();
            oldFreq = freq;
            return;
        }
    }

    /**
     * Calculates the number the amplitude should be shifted with.
     *
     * @return The shift number.
     */
    private int calcShift() {
        int nr32 = (ab.read(NR32_REGISTER) & 0x60) >> 5;
        switch (nr32) {
        case 0:
            return 9;
        case 1:
            return 0;
        case 2:
            return 1;
        case 3:
            return 2;
        default:
            return 9;
        }
    }

    /**
     * Calculates the wave pattern that a wavelength should have.
     */
    private void calcWavePattern() {
        waveLength = (int) ((sampleRate) / (float) freq);
        if (waveLength == 0) {
            waveLength = 1;
        }
        wavePattern = new byte[32];
        int k = 0;
        for (int i = 0; i <= (SOUND3_WAVEPATTERN_END - SOUND3_WAVEPATTERN_START); i++) {
            wavePattern[k] = (byte) (((ab.read((SOUND3_WAVEPATTERN_START + i)) & 0xF0) >> 4) * 2);
            k++;
            wavePattern[k] = (byte) ((ab.read((SOUND3_WAVEPATTERN_START + i)) & 0xF) * 2);
            k++;
        }
    }

    /**
     * Checks if calculate if tone length is enabled and if it is calculates the
     * tone length.
     */
    private void calcToneLength() {
        lengthEnabled = ((ab.read(NR34_REGISTER) & 0x40) > 0);
        int nr31 = ab.read(NR31_REGISTER);
        if (lengthEnabled) {
            toneLength = (int) (((256 - ((double) 
                    (nr31 & 0xFF))) / 256) * sampleRate);
            //Reset the length counter.
            ab.forceWrite(NR31_REGISTER, nr31 + 0x100);
        }
    }
}
