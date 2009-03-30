package fajitaboy.audio;

import static fajitaboy.constants.AddressConstants.NR11_REGISTER;
import static fajitaboy.constants.AddressConstants.*;
import static fajitaboy.constants.AddressConstants.SOUND1_LOW;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;

import fajitaboy.memory.AddressBus;

public class Audio2 {

    AudioFormat af;

    byte[] buffer1;
    SourceDataLine sdl;

    float samplerate = 44100;
    int samples;
    int offset = 0;
    int oldFreq = 0;
    int amp = 32;
    int soundLow;
    int soundHigh;
    int length = (int) (samplerate*3);
    AddressBus ab;
    int freq;

    private int oldNr12 = 0;

    public Audio2(AddressBus ab, int soundLow, int soundHigh, int samples)
            throws LineUnavailableException {
        this.soundHigh = soundHigh;
        this.soundLow = soundLow;
        this.ab = ab;
        this.samples = samples;
        buffer1 = null;
    }

    public byte[] generateTone(byte[] destBuff) {
        calcFreq();

        if (oldFreq != 0) {
            if (offset + samples > buffer1.length) {
                fillBuffer();
                System.out.println("Filling buffer");
                // end = buffer1.length - offset;
            }
            // System.arraycopy(buffer1, offset, destBuff, 0, end);
            int j = 0;
            for (int i = offset; i < (samples + offset); i++) {
                // System.out.println("loop");
                destBuff[j] += buffer1[i];
                j++;
            }
            // System.out.println("j =  " + j);
            // sdl.write(destBuff, 0, end);
            // sdl.write(buffer1, offset, end);
            offset += samples;
        }
        return destBuff;
    }

    private boolean calcFreq() {
        int low1 = ab.read(soundLow);
        int high1 = ab.read(soundHigh) * 0x100;
        freq = 131072 / (2047 - (high1 + low1) & 0x7ff);
        // System.out.println("freq " + freq);
        if (freq == oldFreq) {
            return false;
        } else {
            fillBuffer();
            oldFreq = freq;
            offset = 0;
            return true;
        }

    }

    private void fillBuffer() {
        buffer1 = new byte[length];

        int step;
        int nr22 = ab.read(NR22_REGISTER);
        amp = ((nr22 & 0xF0 )>> 4) * 2;
        int stepLength = nr22 & 0x7;
        int direction = nr22 & 0x8;
        if (direction == 0) {
            step = -2;
        } else {
            step = 2;
        }
        for (int i = 0; i < length; i++) {
            if (stepLength != 0) {
                if ((i % (stepLength * samples)) == 0) {
    //                System.out.println("amp " + amp);
                    if ((amp > 0) && (amp < 32)) {
                        amp += step;
                    }
                }
            }
            double angle1 = i / (samplerate / freq) * 2.0 * Math.PI;
            buffer1[i] = (byte) (amp * Math.signum((Math.sin(angle1))));
        }
        offset = 0;
    }
}
