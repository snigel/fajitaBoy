package fajitaboy;

import static fajitaboy.constants.AddressConstants.NR11_REGISTER;
import static fajitaboy.constants.AddressConstants.*;
import static fajitaboy.constants.AddressConstants.SOUND1_LOW;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;

public class Audio2 {

    AudioFormat af;

    byte[] buffer1;
    byte[] buffer2;
    byte[] bufferMix;
    SourceDataLine sdl;

    float samplerate = 44100;

    int samples = 735;
    int offset = 0;
    int end = samples;
    int oldFreq = 0;
    int oldFreq2 = 0;
    int soundLow;
    int soundHigh;
    int length = (int) (samplerate*1);
    AddressBus ab;

    public Audio2(AddressBus ab, int soundLow, int soundHigh) throws LineUnavailableException {
        this.soundHigh = soundHigh;
        this.soundLow = soundLow;
        this.ab = ab;
        af = new AudioFormat(samplerate, 8, 1, true, false);
        DataLine.Info info = new DataLine.Info(SourceDataLine.class, af);
        sdl = (SourceDataLine) AudioSystem.getLine(info);
     //   sdl = AudioSystem.getSourceDataLine(af);
        buffer1 = null;
        buffer2 = null;
        sdl.open(af);
        sdl.start();

    }

    public void generateTone() {
        /*
        boolean ch1 = calcFreq1();
        boolean ch2 = calcFreq2();
        if (ch1 || ch2) {
            mic
        }
        */
        calcFreq1();
        if (oldFreq != 0) {
            if(offset + samples > buffer1.length) {
                end = buffer1.length - offset;
            }
            sdl.write(buffer1, offset, end);
            offset += samples;
        }
        /*
        if(buffer1 != null) {
            int i = 0;
            int j;
            for(j = offset; offset < end; j++) {
                buffer2[i] = buffer1[j];
                i++;
            }
            offset = j;
            sdl.write(buffer2, 0, i);
            end += samples;
            if(end >= buffer1.length) {
                buffer1 = null;
                offset = 0;
                end = samples;
            }
        }
        else {
            readLength();
        }
    */
    }

    private boolean calcFreq1() {
        int low1 = ab.read(soundLow);
        int high1 = ab.read(soundHigh)*0x100;
        int freq1 = 65536/(2047-(high1+low1)&0x7ff);
        System.out.println("channel 3 freq: " + freq1);
        if (freq1 == oldFreq) {
            return false;
        }
        else {
            buffer1 = new byte[length];
            for(int i = 0; i < length; i++) {
                double angle1 = i / (samplerate/freq1) * 2.0 * Math.PI;
                buffer1[i] = (byte) (100 * Math.signum((Math.sin(angle1))));
            }
            oldFreq = freq1;
            offset = 0;
            end = samples;
            return true;
        }

    }

    private boolean calcFreq2() {
        int low1 = ab.read(SOUND2_LOW);
        int high1 = ab.read(SOUND2_HIGH)*0x100;
        int freq1 = 65536/(2048-(high1+low1)&0x7ff);
        if (freq1 == oldFreq) {
            return false;
        }
        else {
            buffer2 = new byte[length];
            for(int i = 0; i < length; i++) {
                double angle1 = i / (samplerate/freq1) * 2.0 * Math.PI;
                buffer2[i] = (byte) (100 * Math.signum((Math.sin(angle1))));
            }
            oldFreq2 = freq1;
            offset = 0;
            end = samples;
            return true;
        }
    }

    private void mix(boolean ch1, boolean ch2) {
        if (ch1 && ch2 ) {
            for (int i = 0; i < length; i++) {
                bufferMix[i] = (byte) (buffer1[i] + buffer2[i]);
            }
        }
        else if (ch1) {
            int k = 0;
            while (offset < buffer2.length) {
                bufferMix[k] = (byte)(buffer2[offset] + buffer1[k]);
                k++;
                offset++;
            }
        }
        else if (ch2) {
            int j = 0;
            while (offset < buffer1.length) {
                bufferMix[j] = (byte) (buffer1[offset] + buffer2[j]);
                offset++;
                j++;
            }
        }
    }
    private void readLength() {
        int length = ab.read(NR11_REGISTER);
        length = length & 0x3F;
        System.out.println("Gb length: " + length);
        //Längd i samples
        length = (int)((64-length)*(1/256)*samplerate);
        if(length < end) {
            end = length;
        }
        if(length != 0 ) {
            buffer1 = new byte[length];
            int low1 = ab.read(SOUND2_LOW);
            int high1 = ab.read(SOUND2_HIGH)*0x100;
            int freq1 = 131072/(2047-(high1+low1)&0x7ff);

            for(int i = 0; i < length; i++) {
                double angle1 = i / (samplerate/freq1) * 2.0 * Math.PI;
                buffer1[i] = (byte) (100 * Math.signum((Math.sin(angle1))));
            }
        }
        else {
            int nr14 = ab.read(NR14_REGISTER)& 0x40;
            System.out.println("Nr14: " + nr14);
            if (nr14 == 0) {
                System.out.println("Nr14: oändlig längd");
            }
        }

    }
}
