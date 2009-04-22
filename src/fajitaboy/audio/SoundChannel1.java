package fajitaboy.audio;

import static fajitaboy.constants.AddressConstants.*;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import fajitaboy.FileIOStreamHelper;
import fajitaboy.StateMachine;
import fajitaboy.memory.AddressBus;

/**
 * Represents Game Boys first sound channel. Generates a square wave with
 * envelope and sweep functions.
 *
 * @author Adam Hulin, Johan Gustafsson
 *
 */
public class SoundChannel1 implements StateMachine {

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
        // If the sound channel is mute, return.
        if (((ab.read(NR12_REGISTER) & 0xF0) >> 4) == 0) {
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
                            && (sweepNr < sweepSteps)) {
                        System.out.println("Sweep");
                        if (sweepDirection == 0) {
                            freq = freq + (int) (freq / (Math.pow(2, sweepNr)));
                        } else {
                            freq = freq - (int) (freq / (Math.pow(2, sweepNr)));
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
        // This is needed for get the length to work correctly,
        // It isn't pretty :) It checks if the forced written bit has
        // been reset. That indicates that we have a new length to work with.
        if (toneLength < 0 && lengthEnabled && 
                (ab.read(NR11_REGISTER) & 0x100) == 0) {
            calcToneLength();
        }
        
        if ((ab.read(NR12_REGISTER) & 0x100) == 0){
            calcEnvelope();
        }

        if ((ab.read(NR10_REGISTER) & 0x100) == 0){
            calcSweep();
        }


        return destBuff;
    }

    /**
     * Calculates the envelope parameters.
     */
    private void calcEnvelope() {
        // Read the envelope register.
        int nr12 = ab.read(NR12_REGISTER);
        amp = ((nr12 & 0xF0) >> 4) * 2;
        envelopeStepLength = nr12 & 0x7;
        int direction = nr12 & 0x8;
        if (direction == 0) {
            envelopeStep = -2;
        } else {
            envelopeStep = 2;
        }
        envelopePos = 0;
        ab.forceWrite(NR12_REGISTER, nr12 + 0x100);
    }

    /**
     * Checks if the frequency has changed, if so it calculates all related
     * parameters.
     */
    private void calcFreq() {
        int low1 = ab.read(SOUND1_LOW);
        int high1 = ab.read(SOUND1_HIGH) * 0x100;
        // Calculate the frequency
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
            calcSweep();
            calcEnvelope();
            dutyLength = calcWavePattern();
            oldFreq = freq;
        }
    }

    /**
     * Checks if calculate if tone length is enabled and if it is calculates the
     * tone length.
     */
    private void calcToneLength() {
        lengthEnabled = ((ab.read(NR14_REGISTER) & 0x40) > 0);
        int nr11 = ab.read(NR11_REGISTER);
        if (lengthEnabled) {
            toneLength = (int) (((64 - ((double)
                    (nr11 & 0x3F))) / 256) * sampleRate);
            //Add a ninth bit to indicate that the length has been read.
            //If the bit is reset, we know that a write has taken place.
            ab.forceWrite(NR11_REGISTER, nr11 + 0x100);
        }
    }

    /**
     * Calculates the sweep parameters.
     */
    private void calcSweep() {
        sweepNr = 0;
        sweepPos = 0;
        int nr10 = ab.read(NR10_REGISTER);
        ab.forceWrite(NR10_REGISTER, nr10 + 0x100);
        sweepSteps = nr10 & 0x7;
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
    private int calcWavePattern() {
        waveLength = (int) ((sampleRate) / (float) freq);
        if (waveLength == 0) {
            waveLength = 1;
        }
        int nr11 = ((ab.read(NR11_REGISTER) & 0xC0) >> 6);
        switch (nr11) {
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
		oldFreq = (int) FileIOStreamHelper.readData(is, 4);
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
		FileIOStreamHelper.writeData(os, (long) oldFreq, 4);
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
