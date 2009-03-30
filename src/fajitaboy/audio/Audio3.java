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

public class Audio3 {

    byte[] buffer1;

    float samplerate = 44100;

    int samples;
    int offset = 0;
    int amp = 32;
    int freq;
    int oldFreq = 0;
    int soundLow;
    int soundHigh;
    int length = (int) (samplerate*3);
    int waveLength;
    int test = 0;
    byte[] wavePattern;
    AddressBus ab;

    public Audio3(AddressBus ab, int soundLow,
            int soundHigh, int samples) throws LineUnavailableException {
        this.samples = samples;
        this.soundHigh = soundHigh;
        this.soundLow = soundLow;
        this.ab = ab;
        buffer1 = null;
    }

    public byte[] generateTone(byte[] destBuff) {
        calcFreq();
        if (oldFreq != 0) {
            if(offset + samples > buffer1.length) {
                System.out.println("fillingbuffer" + freq);

                test++;
                fillBuffer();
            }
            int j = 0;
            for(int i = offset; i < (samples + offset); i++) {
             //   System.out.println("loop");
                destBuff[j] += buffer1[i];
                j++;
            }
            offset += samples;
        }
        return destBuff;
    }

    private boolean calcFreq() {
        int low1 = ab.read(soundLow);
        int high1 = ab.read(soundHigh)*0x100;
        freq = 65536/(2047-(high1+low1)&0x7ff);
        if (freq == oldFreq) {
            return false;
        }
        else {
            fillBuffer();
            oldFreq = freq;
            return true;
        }
    }

    private void fillBuffer() {
     //   if(freq > 1000)
//            System.out.println("freq = " + freq);
//        int nr34 = ab.read(NR34_REGISTER) % 0x40;
//        if(nr34 == 0)
//            System.out.println("length set");
        buffer1 = new byte[length];
        waveLength = (int)((samplerate) / (float)freq);
        if (waveLength == 0) {
            waveLength = 1;
        }
      // amp = calcAmplitude();
        calcWavePattern();
        int pos = 0;
        for(int i = 0; i < length; i++) {
            buffer1[i] = (byte) (wavePattern[((32 * pos) / waveLength) % 32]);
            pos = (pos +1) % waveLength;
        }
        offset = 0;
    }

    private int calcShift() {
        int nr30 = ab.read(NR30_REGISTER)  & 0x80;
   //     if(nr30 == 0) {
   //         return 4; //Skifta ut alla ettor.
   //     }
        int nr32 = (ab.read(NR32_REGISTER)  & 0x60) >> 5;
        switch(nr32) {
        case 0:
            return 4;
        case 1:
            return 0;
        case 2:
            return 1;
        case 3:
            return 2;
        default:
            return 0;
        }
    }

    private void calcWavePattern() {
        int shift = calcShift();
        int startAddress = 0xFF30;
        int endAddress = 0xFF3F;
        wavePattern = new byte[32];
        for(int i = 0; i <= (endAddress - startAddress); i++) {
           wavePattern[i] = (byte) ((((ab.read((startAddress +i)) & 0xF0) >> 4) >> shift) * 2);
           wavePattern[i + 1] = (byte) (((ab.read((startAddress +i)) & 0xF) >> shift) * 2);
         //  int k = (ab.read((startAddress +i)) & 0xF0) >> 4;
         //  System.out.println("Wavepattern " + k);
        }
    }
}
