package fajitaboy.gb.audio;

import static fajitaboy.constants.AddressConstants.*;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import fajitaboy.FileIOStreamHelper;
import fajitaboy.gb.StateMachine;
import fajitaboy.gb.memory.AddressBus;

/**
 * Represents Game Boys first sound channel. Generates a square wave with
 * envelope and sweep functions.
 *
 * @author Adam Hulin, Johan Gustafsson
 *
 */
/**
 * @author Adam Hulin, Johan Gustafsson
 *
 */
public class SoundChannel1 implements StateMachine {

    /**
     * The sample rate the sound should be sampled.
     */
    private float sampleRate;

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
     * The number of sweeps the sweep function should take.
     */
    private int sweepLength;

    /**
     * The number of steps the a sweep should take.
     */
    private int sweepSteps;

    /**
     * Indicates the direction the frequency should change.
     */
    private int sweepDirection;

    /**
     * Indicates the number of sweep steps taken.
     */
    private int sweepNr;

    /**
     * The position in a sweep.
     */
    private int sweepPos;

    /**
     * Flag that indicates of toneLength is on or off.
     */
    private boolean lengthEnabled;

    /**
     * The length of the tone in samples.
     */
    private int toneLength;

    /**
     * Holds the value of the percent of the volume output.
     */
    double volume;

    /**
     * Constructor for SoundChannel 1.
     *
     * @param ab
     *            The address bus.
     * @param sampleRate
     *            The sample rate.
     */
    public SoundChannel1(final AddressBus ab, final float sampleRate) {
        this.ab = ab;
        this.sampleRate = sampleRate;
        pos = 0;
        amp = 32;
        lengthEnabled = false;
        volume = 1;
        toneLength = -1;
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

        updateParameters();
        // If the sound channel is mute, return.
        if (((ab.read(ADDRESS_NR12) & 0xF0) >> 4) == 0) {
            return destBuff;
        }

        if ((toneLength > 0 && lengthEnabled) || !lengthEnabled) {
            if (lengthEnabled) {
                toneLength -= samples;
            }
            // The final amplitude, that should be put in the array.
            int finalAmp;
            // Used for stereo input.
            int k = 0;
            // Fill the array with samples.
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

                // Sweep
                if (sweepLength != 0) {
                    if (((sweepPos % (sweepLength * samples)) == 0)
                          /*  && (sweepNr < sweepSteps)*/) {
                        int nr13 = ab.read(ADDRESS_NR13);
                        if (sweepDirection == 0) {
                            freq = freq + (int) (freq / (Math.pow(2, sweepNr)));
                            calcWavePattern();
                            calcWaveLength();
                            ab.forceWrite(ADDRESS_NR13, nr13 + 0x100);
                        } else {
                            freq = freq - (int) (freq / (Math.pow(2, sweepNr)));
                            calcWavePattern();
                            calcWaveLength();
                            ab.forceWrite(ADDRESS_NR13, nr13 + 0x100);
                        }
                    }
                    sweepPos++;
                }
                // Check the wavepattern
                if (pos < dutyLength) {
                    finalAmp = -amp;
                } else {
                    finalAmp = amp;
                }

                if (left) {
                    destBuff[k] += (byte) ((double)finalAmp * volume);
                }
                k++;
                if (right) {
                    destBuff[k] += (byte) ((double)finalAmp * volume);
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
        // Read the envelope register.
        int nr12 = ab.read(ADDRESS_NR12);
        amp = ((nr12 & 0xF0) >> 4) * 2;
        envelopeStepLength = nr12 & 0x7;
        int direction = nr12 & 0x8;
        if (direction == 0) {
            envelopeStep = -2;
        } else {
            envelopeStep = 2;
        }
        envelopePos = 0;
        ab.forceWrite(ADDRESS_NR12, nr12 + 0x100);
    }

    /**
     * Checks if the frequency has changed, if so it calculates all related
     * parameters.
     */
    private void calcFreq() {
        int low1 = ab.read(ADDRESS_SOUND1_LOW);
        int high1 = ab.read(ADDRESS_SOUND1_HIGH) * 0x100;
        // Calculate the frequency
        int tmp = (2047 - (high1 + low1) & 0x7ff);
        if (tmp != 0) {
            freq = 131072 / tmp;
        } else {
            freq = 131072;
        }
        calcWaveLength();
        calcToneLength();
        calcWavePattern();
        ab.forceWrite(ADDRESS_SOUND1_LOW, low1 + 0x100);        
    }

    /**
     * Checks if calculate if tone length is enabled and if it is calculates the
     * tone length.
     */
    private void calcToneLength() {
        lengthEnabled = ((ab.read(ADDRESS_NR14) & 0x40) > 0);
        int nr11 = ab.read(ADDRESS_NR11);
        if (lengthEnabled) {
            toneLength = (int) (((64 - ((double)
                    (nr11 & 0x3F))) / 256) * sampleRate);
            //Add a ninth bit to indicate that the length has been read.
            //If the bit is reset, we know that a write has taken place.
            ab.forceWrite(ADDRESS_NR11, nr11 + 0x100);
        }
    }

    /**
     * Calculates the sweep parameters.
     */
    private void calcSweep() {
        sweepNr = 0;
        sweepPos = 0;
        int nr10 = ab.read(ADDRESS_NR10);
        ab.forceWrite(ADDRESS_NR10, nr10 + 0x100);
        sweepNr = nr10 & 0x7;
        sweepDirection = nr10 & 0x8;
        int sweepTime = ((nr10 & 0x70) >> 4);

        switch (sweepTime) {
        case 0:
            sweepLength = 0;
            break;
        case 1:
            sweepLength = (int) ((sampleRate / 1000) * 7.8);
            break;
        case 2:
            sweepLength = (int) ((sampleRate / 1000) * 15.6);
            break;
        case 3:
            sweepLength = (int) ((sampleRate / 1000) * 23.4);
            break;
        case 4:
            sweepLength = (int) ((sampleRate / 1000) * 31.3);
            break;
        case 5:
            sweepLength = (int) ((sampleRate / 1000) * 39.1);
            break;
        case 6:
            sweepLength = (int) ((sampleRate / 1000) * 46.9);
            break;
        case 7:
            sweepLength = (int) ((sampleRate / 1000) * 54.7);
            break;
        default:
            sweepLength = 0;
            break;
        }

    }

    /**
     * Calculates the wave pattern.
     */
    private void calcWavePattern() {
        int nr11 = ((ab.read(ADDRESS_NR11) & 0xC0) >> 6);
//        ab.forceWrite(ADDRESS_NR11, nr11 + 0x100);
        switch (nr11) {
        case 0:
            dutyLength = (int) ((float) waveLength * 0.125); break;
        case 1:
            dutyLength = (int) ((float) waveLength * 0.25); break;
        case 2:
            dutyLength = (int) ((float) waveLength * 0.50); break;
        case 3:
            dutyLength = (int) ((float) waveLength * 0.75); break;
        default:
            dutyLength = 0; break;
        }
    }

    /**
     * Sets the volume.
     * @param volume Should be a value between 0-1.
     */
    public final void setVolume(final double volume) {
        if(volume >= 0 && volume <= 1) {
            this.volume = volume;
        }
    }
    
    /**
     * Checks the registers and updates the parameters.
     */
    private void updateParameters() {

        if ((ab.read(ADDRESS_NR13) & 0x100) == 0){
            calcFreq();
        }

        if ((ab.read(ADDRESS_NR11) & 0x100) == 0){
            if (toneLength < 0 && lengthEnabled) {
                calcToneLength();
            }
//            calcWavePattern();
        }

        
        if ((ab.read(ADDRESS_NR12) & 0x100) == 0){
            calcEnvelope();
        }

        if ((ab.read(ADDRESS_NR10) & 0x100) == 0){
            calcSweep();
        }
    }
    
    private void calcWaveLength() {
        waveLength = (int) ((sampleRate) / (float) freq);
        if (waveLength == 0) {
            waveLength = 1;
        }
    }

    /**
     * {@inheritDoc}
     */
    public void readState(FileInputStream is) throws IOException {
        amp = (int) FileIOStreamHelper.readData(is, 4);
        dutyLength = (int) FileIOStreamHelper.readData(is, 4);
        envelopePos = (int) FileIOStreamHelper.readData(is, 4);
        envelopeStep = (int) FileIOStreamHelper.readData(is, 4);
        envelopeStepLength = (int) FileIOStreamHelper.readData(is, 4);
        freq = (int) FileIOStreamHelper.readData(is, 4);
        lengthEnabled = FileIOStreamHelper.readBoolean(is);
        pos = (int) FileIOStreamHelper.readData(is, 4);
        sampleRate = (int) FileIOStreamHelper.readData(is, 4);
        sweepDirection = (int) FileIOStreamHelper.readData(is, 4);
        sweepLength = (int) FileIOStreamHelper.readData(is, 4);
        sweepNr = (int) FileIOStreamHelper.readData(is, 4);
        sweepPos = (int) FileIOStreamHelper.readData(is, 4);
        sweepSteps = (int) FileIOStreamHelper.readData(is, 4);
        toneLength = (int) FileIOStreamHelper.readData(is, 4);
        waveLength = (int) FileIOStreamHelper.readData(is, 4);
    }

    /**
     * {@inheritDoc}
     */
    public void saveState(FileOutputStream os) throws IOException {
        FileIOStreamHelper.writeData(os, (long) amp, 4);
        FileIOStreamHelper.writeData(os, (long) dutyLength, 4);
        FileIOStreamHelper.writeData(os, (long) envelopePos, 4);
        FileIOStreamHelper.writeData(os, (long) envelopeStep, 4);
        FileIOStreamHelper.writeData(os, (long) envelopeStepLength, 4);
        FileIOStreamHelper.writeData(os, (long) freq, 4);
        FileIOStreamHelper.writeBoolean(os, lengthEnabled);
        FileIOStreamHelper.writeData(os, (long) pos, 4);
        FileIOStreamHelper.writeData(os, (long) sampleRate, 4);
        FileIOStreamHelper.writeData(os, (long) sweepDirection, 4);
        FileIOStreamHelper.writeData(os, (long) sweepLength, 4);
        FileIOStreamHelper.writeData(os, (long) sweepNr, 4);
        FileIOStreamHelper.writeData(os, (long) sweepPos, 4);
        FileIOStreamHelper.writeData(os, (long) sweepSteps, 4);
        FileIOStreamHelper.writeData(os, (long) toneLength, 4);
        FileIOStreamHelper.writeData(os, (long) waveLength, 4);
    }
}
