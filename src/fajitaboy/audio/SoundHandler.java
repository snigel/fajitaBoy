package fajitaboy.audio;

import static fajitaboy.constants.AddressConstants.*;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import fajitaboy.memory.AddressBus;

/**
 * A debug class for testing the audio.
 * @author snigel
 */
public class SoundHandler {

    private AddressBus ab;
    private Audio au1;
    private Audio2 au2;
    private Audio3 au3;
    private Audio4 au4;
    private boolean ch1Left;
    private boolean ch1Right;
    private boolean ch2Left;
    private boolean ch2Right;
    private boolean ch3Left;
    private boolean ch3Right;
    private boolean ch4Left;
    private boolean ch4Right;
    private int samples;
    private int finalSamples;
    private byte[] destBuff;
    private AudioFormat af;
    private SourceDataLine sdl;

    public SoundHandler(AddressBus ab, float sampleRate, int samples ) throws LineUnavailableException {
        af = new AudioFormat(sampleRate, 8, 2, true, false);
        DataLine.Info info = new DataLine.Info(SourceDataLine.class, af);
        sdl = (SourceDataLine) AudioSystem.getLine(info);
        sdl.open(af);
        sdl.start();

        this.ab = ab;
        this.samples = samples;
        au1 = new Audio(ab, sampleRate);
        au2 = new Audio2(ab, sampleRate);
        au3 = new Audio3(ab, sampleRate);
        au4 = new Audio4(ab, sampleRate);

    }

    public void generateTone() throws LineUnavailableException {
        if(sdl.available()*2 < samples*2) {
            destBuff = new byte[sdl.available()*2];
            finalSamples = sdl.available();
        }
        else {
            destBuff = new byte[samples*2];
            finalSamples = samples;
        }
        stereoSelect();
        au1.generateTone(destBuff, ch1Left, ch1Right, finalSamples);
        au2.generateTone(destBuff, ch2Left, ch2Right, finalSamples);
        au3.generateTone(destBuff, ch3Left, ch3Right, finalSamples);
        au4.generateTone(destBuff, ch4Left, ch4Right, finalSamples);

        sdl.write(destBuff, 0, destBuff.length);
    }

    private void stereoSelect() {
        int nr51 = ab.read(NR51_REGISTER);
        ch1Left = ((nr51 & 0x1) > 0);
        ch2Left = ((nr51 & 0x2) > 0);
        ch3Left = ((nr51 & 0x4) > 0);
        ch4Left = ((nr51 & 0x8) > 0);
        ch1Right = ((nr51 & 0x10) > 0);
        ch2Right = ((nr51 & 0x20)  > 0);
        ch3Right = ((nr51 & 0x40) > 0);
        ch4Right = ((nr51 & 0x80) > 0);
    }
    
    public void close() {
    	sdl.close();
    }
}

