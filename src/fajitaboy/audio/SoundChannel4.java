package fajitaboy.audio;

import static fajitaboy.constants.AddressConstants.*;

import java.util.Random;
import fajitaboy.memory.AddressBus;

/**
 * Represents Game Boys third sound channel. Generates a wave built up after an
 * given pattern.
 *
 * @author Adam Hulin, Johan Gustafsson
 *
 */
public class SoundChannel4 {

    /**
     * The sample rate the sound should be sampled.
     */
    private float sampleRate;

    /**
     * The old frequency that the last tone had.
     */
    private int oldFreq;

    /**
     * The frequency of the tone.
     */
    private int freq;

    /**
     * The final frequency that will be generated.
     */
    private int finalFreq;

    /**
     * The address bus.
     */
    private AddressBus ab;

    /**
     * The random generator that is used to generate the amplitude.
     */
    private Random random;

    /**
     * The waves current amplitude.
     */
    private int amp;

    /**
     * Used in envelope, indicates how much the amplitude should change.
     */
    private int envelopeStep;

    /**
     * The number of step an envelope function should take.
     */
    private int envelopeStepLength;

    /**
     * The current position in the envelope.
     */
    private int envelopePos;

    /**
     * Flag that indicates of toneLength is on or off.
     */
    private boolean lengthEnabled;

    /**
     * The length of the tone in samples.
     */
    private int toneLength;

    /**
     * Constructor for SoundChannel 4.
     *
     * @param ab
     *            The address bus.
     * @param sampleRate
     *            The sample rate.
     */
    public SoundChannel4(final AddressBus ab, final float sampleRate) {
        this.ab = ab;
        this.sampleRate = sampleRate;
        random = new Random();
        amp = 32;
        oldFreq = 0;
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

        calcFreq();
        if (((ab.read(NR42_REGISTER) & 0xF0) >> 4) == 0) {
            return destBuff;
        }

        if ((toneLength > 0 && lengthEnabled) || !lengthEnabled) {
            if (lengthEnabled) {
                toneLength -= samples;
            }

            int finalAmp = amp;
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

                if (left) {
                    destBuff[k] += (byte) finalAmp;
                }
                k++;
                if (right) {
                    destBuff[k] += (byte) finalAmp;
                }
                k++;

                if (i % finalFreq == 0) {
                    boolean j = random.nextBoolean();
                    if (j)
                        finalAmp = amp;
                    else {
                        finalAmp = -amp;
                    }
                }
            }
        }

        if (toneLength < 0 && lengthEnabled && 
                (ab.read(NR41_REGISTER) & 0x100) == 0) {
            calcToneLength();
        }

        return destBuff;
    }

    /**
     * Checks if the frequency has changed, if so it calculates all related
     * parameters.
     */
    private void calcFreq() {
        int nr43 = ab.read(NR43_REGISTER);
        int s = (nr43 & 0xF0) >> 4;
        double r = nr43 & 0x7;
        if (r == 0) {
            r = 0.5;
        }
        freq = (int) ((524288 / r) / (Math.pow(2, (s + 1))));
        if (freq == oldFreq) {
            return;
        } else {
            finalFreq = (int) (sampleRate / freq);
            if (finalFreq == 0) {
                finalFreq = 1;
            }
            calcEnvelope();
            calcToneLength();
            oldFreq = freq;
        }
    }

    /**
     * Calculates the envelope parameters.
     */
    private void calcEnvelope() {
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
    }

    /**
     * Checks if calculate if tone length is enabled and if it is calculates the
     * tone length.
     */
    private void calcToneLength() {
        lengthEnabled = ((ab.read(NR44_REGISTER) & 0x40) > 0);
        int nr41 = ab.read(NR41_REGISTER);
        if (lengthEnabled) {
            toneLength = (int) (((64 - ((double)
                    (nr41 & 0x3F))) / 256) * sampleRate);
            //Reset length counter
            ab.forceWrite(NR41_REGISTER, nr41 + 0x100);
        }
    }
}
