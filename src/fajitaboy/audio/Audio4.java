package fajitaboy.audio;

import static fajitaboy.constants.AddressConstants.NR11_REGISTER;
import static fajitaboy.constants.AddressConstants.*;
import static fajitaboy.constants.AddressConstants.SOUND1_LOW;

import java.util.Random;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;

import fajitaboy.memory.AddressBus;

public class Audio4 {

    AudioFormat af;
    byte[] buffer1;

    float samplerate = 44100;

    int samples;
    int offset = 0;
    int oldFreq = 0;
    int freq;
    int length = (int) (samplerate);
    AddressBus ab;
    Random random;
    int amp = 32;

    public Audio4(AddressBus ab, int samples) throws LineUnavailableException {
        this.ab = ab;
        this.samples = samples;
        random = new Random();
        buffer1 = null;
    }

    public byte[] generateTone(byte[] destBuff) {
        calcFreq();
        int nr44 = ab.read(NR44_REGISTER)& 0x40;
        if ((oldFreq != 0) && (nr44 == 0))  {
        
            if(offset + samples > buffer1.length) {
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
        int nr43 = ab.read(NR43_REGISTER);
        System.out.println("nr43 " + nr43 );
        int s = (nr43 & 0xF0) >> 4 ;
        System.out.println("s: " + s);    
    //    System.out.println("s = " + s);
        double r = nr43 & 0x7;
        if(r == 0) {
            r = 0.5;
        }
        freq = (int)((524288 / r) / (Math.pow(2, (s+1))));
        //freq = (int) (52488 / (r)) >> (s + 1);
       System.out.println("freq : " + freq);
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
        buffer1 = new byte[length];
        int k = 0;
        for(int i = 0; i < length; i++) {
            double angle1 = i / (samplerate/freq) * 2.0 * Math.PI;
            buffer1[i] = (byte) (amp * Math.signum((Math.sin(angle1))));
            k++;
            if(k == 10) {
              amp = random.nextInt(64)-32;
         //   calcAmp();
                k = 0;
            }
            
        }
        offset = 0 ;
        
    }
    private void calcAmp() {
       // amp =(int) ((Math.random() * 32 * 2) - 32);
        /*
        boolean a = random.nextBoolean();
        if (a) {
            amp = -32;
        }
        else {
            amp = 32;
        }*/
    }
}
