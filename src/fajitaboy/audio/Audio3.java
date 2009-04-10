package fajitaboy.audio;

import static fajitaboy.constants.AddressConstants.*;

import fajitaboy.memory.AddressBus;

public class Audio3 {

    private float sampleRate;
    private int freq;
    private int oldFreq;
    private int waveLength;
    private byte[] wavePattern;
    private AddressBus ab;
    private int pos;
    private boolean lengthEnabled;
    private int toneLength;

    public Audio3(AddressBus ab, float sampleRate) {
        this.sampleRate = sampleRate;
        this.ab = ab;
        oldFreq = 0;
        pos = 0;
        lengthEnabled = false;
    }

    public byte[] generateTone(byte[] destBuff, boolean left, boolean right, int samples) {
        if((ab.read(NR30_REGISTER) & 0x80) == 0 ) {
            return destBuff;
        }
        calcFreq();
        if ((toneLength > 0 && lengthEnabled) || !lengthEnabled) {
            if(lengthEnabled) {
                toneLength -= samples;
            }
            int shift = calcShift();
            int k = 0;
            byte d;
            for (int i = 0; i < samples; i++) {
                d = (byte) (wavePattern[((32 * pos) / waveLength) % 32] >> shift);
                if (left) {
                    destBuff[k] += d;
                }
                k++;
                if (right) {
                    destBuff[k] += d;
                }
                k++;

                pos = (pos + 1) % waveLength;
            }
        }
        return destBuff;
    }

    private void calcFreq() {
        int low1 = ab.read(SOUND3_LOW);
        int high1 = ab.read(SOUND3_HIGH)*0x100;
        int tmp = (2047-(high1+low1)&0x7ff);
        if (tmp != 0) {
            freq = 65536 / tmp;
        }
        else {
            freq = 65536;
        }
        if (freq == oldFreq) {
            return;
        }
        else {
            calcToneLength();
            calcWavePattern();
            oldFreq = freq;
            return;
        }
    }

    private int calcShift() {
        int nr32 = (ab.read(NR32_REGISTER)  & 0x60) >> 5;
        switch(nr32) {
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

    private void calcWavePattern() {
        waveLength = (int)((sampleRate) / (float)freq);
        if (waveLength == 0) {
            waveLength = 1;
        }
        int startAddress = 0xFF30;
        int endAddress = 0xFF3F;
        wavePattern = new byte[32];
        int k = 0;
        for(int i = 0; i <= (endAddress - startAddress); i++) {
           wavePattern[k] = (byte) (((ab.read((startAddress +i)) & 0xF0) >> 4) * 2);
           k++;
           wavePattern[k] = (byte) ((ab.read((startAddress +i)) & 0xF) * 2);
           k++;
        }
    }
    
    /**
     * 
     */
    private void calcToneLength() {
        lengthEnabled = ((ab.read(NR34_REGISTER) & 0x40) > 0);
        if(lengthEnabled) {
            toneLength = (int) (((256 -((double)(ab.read(NR31_REGISTER) & 0xFF))) / 256) * sampleRate);
        }
    }

}
