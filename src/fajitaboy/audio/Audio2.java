package fajitaboy.audio;

import static fajitaboy.constants.AddressConstants.*;

import fajitaboy.memory.AddressBus;

public class Audio2 {
    private float sampleRate;
    private int oldFreq;
    private int amp;
    private AddressBus ab;
    private int freq;
    private int pos;
    private int waveLength;
    private int dutyLength;
    private int step;
    private int stepLength;
    private boolean lengthEnabled;
    private int toneLength;


    public Audio2(AddressBus ab,float sampleRate) {
        this.ab = ab;
        this.sampleRate = sampleRate;
        pos = 0;
        oldFreq = 0;
        amp = 32;
        lengthEnabled = false;
    }

    public byte[] generateTone(byte[] destBuff, boolean left, boolean right, int samples) {
        calcFreq();
        if(((ab.read(NR22_REGISTER) & 0xF0 )>> 4) == 0) {
            return destBuff;
        }

        if ((toneLength > 0 && lengthEnabled) || !lengthEnabled) {
            if(lengthEnabled) {
                toneLength -= samples;
            }
            int finalAmp;
            int k = 0;
            for (int i = 0; i < samples; i++) {

                //Envelope
                if (stepLength != 0) {
                    if ((i % (stepLength * samples)) == 0) {
                        if ((amp > 0) && (amp < 32)) {
                            amp += step;
                        }
                    }
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

    private void calcEnvelope() {
        int nr22 = ab.read(NR22_REGISTER);
        amp = ((nr22 & 0xF0 )>> 4) * 2;
        stepLength = nr22 & 0x7;
        int direction = nr22 & 0x8;
        if (direction == 0) {
            step = -2;
        } else {
            step = 2;
        }
    }

    private void calcFreq() {
        int low1 = ab.read(SOUND2_LOW);
        int high1 = ab.read(SOUND2_HIGH) * 0x100;
        int tmp = (2047 - (high1 + low1) & 0x7ff);
        if(tmp != 0 ) {
            freq = 131072 / tmp;
        }
        else {
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
     *
     */
    private int calcWavePattern() {
        waveLength = (int)((sampleRate) / (float)freq);
        if (waveLength == 0) {
            waveLength = 1;
        }
        int nr21 = ((ab.read(NR21_REGISTER) & 0xC0) >> 6);
        switch(nr21) {
        case 0:
            return (int)((float)waveLength * 0.125);
        case 1:
            return (int)((float)waveLength * 0.25);
        case 2:
            return (int)((float)waveLength * 0.50);
        case 3:
            return (int)((float)waveLength * 0.75);
        default:
            return 0;
        }
    }
    
    /**
     * 
     */
    private void calcToneLength() {
        lengthEnabled = ((ab.read(NR24_REGISTER) & 0x40) > 0);
        if(lengthEnabled) {
            toneLength = (int) (((64 -((double)(ab.read(NR21_REGISTER) & 0x3F))) / 256) * sampleRate);
        }
    }

}
