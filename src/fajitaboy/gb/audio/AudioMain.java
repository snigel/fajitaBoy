package fajitaboy.gb.audio;

import static fajitaboy.constants.AddressConstants.*;
import static fajitaboy.constants.AudioConstants.*;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import javax.swing.JFrame;

import fajitaboy.AudioReciever;
import fajitaboy.ReadRom;
import fajitaboy.VideoReciever;
import fajitaboy.gb.audio.SoundReciever;
import fajitaboy.applet.GamePanel;
import fajitaboy.gb.Cpu;
import fajitaboy.gb.Oscillator;
import fajitaboy.gb.memory.AddressBus;

/**
 * A debug class for testing the audio.
 * @author snigel
 */
public class AudioMain implements VideoReciever {

    private AddressBus ab;
    private SoundChannel1 au1;
    private SoundChannel2 au2;
    private SoundChannel3 au3;
    private SoundChannel4 au4;
    private boolean ch1Left;
    private boolean ch1Right;
    private boolean ch2Left;
    private boolean ch2Right;
    private boolean ch3Left;
    private boolean ch3Right;
    private boolean ch4Left;
    private boolean ch4Right;
    private GamePanel gp;
    private float sampleRate = 44100;
    private int samples = 735;
    private int finalSamples;
    private byte[] destBuff;
    private AudioFormat af;
    private SourceDataLine sdl;

    public AudioMain() throws LineUnavailableException {
        af = new AudioFormat(sampleRate, 8, 2, true, false);
        DataLine.Info info = new DataLine.Info(SourceDataLine.class, af);
        sdl = (SourceDataLine) AudioSystem.getLine(info);
        sdl.open(af);
        sdl.start();

        JFrame jfr = new JFrame();
        gp = new GamePanel(2);
        jfr.setContentPane(gp);
        jfr.setVisible(true);


        ab = new AddressBus(ReadRom.readRom("/bombjack.gb"));
        au1 = new SoundChannel1(ab, sampleRate);
        au2 = new SoundChannel2(ab, sampleRate);
        au3 = new SoundChannel3(ab, sampleRate);
        au4 = new SoundChannel4(ab, sampleRate);

        Cpu cpu = new Cpu(ab);
        Oscillator oc = new Oscillator(cpu, ab, this, (AudioReciever)(new SoundReciever(AUDIO_SAMPLERATE)));

        for (int i = 0; i < INSTRUCTIONS; i++) {
            oc.step();
        }
    }

    /**
     * Number of instructions to run the audiotest.
     */
    private static final int INSTRUCTIONS = 50000000;

    /**
     * Starts the audiotest class.
     * @param args
     *            No arguments
     * @throws LineUnavailable
     */
    public static void main(final String[] args)
            throws LineUnavailableException {
        new AudioMain();
    }

    public void transmitVideo(int[][] data) {
        if(sdl.available()*2 < samples*2) {
            destBuff = new byte[sdl.available()*2];
            finalSamples = sdl.available();
        }
        else {
            destBuff = new byte[samples*2];
            finalSamples = samples;
        }
        stereoSelect();
        gp.transmitVideo(data);
        au1.generateTone(destBuff, ch1Left, ch1Right, finalSamples);
        au2.generateTone(destBuff, ch2Left, ch2Right, finalSamples);
        au3.generateTone(destBuff, ch3Left, ch3Right, finalSamples);
        au4.generateTone(destBuff, ch4Left, ch4Right, finalSamples);

        sdl.write(destBuff, 0, destBuff.length);
    }

    private void stereoSelect() {
        int nr51 = ab.read(ADDRESS_NR51);
        ch1Left = ((nr51 & 0x1) > 0);
        ch2Left = ((nr51 & 0x2) > 0);
        ch3Left = ((nr51 & 0x4) > 0);
        ch4Left = ((nr51 & 0x8) > 0);
        ch1Right = ((nr51 & 0x10) > 0);
        ch2Right = ((nr51 & 0x20)  > 0);
        ch3Right = ((nr51 & 0x40) > 0);
        ch4Right = ((nr51 & 0x80) > 0);
    }

    /** {@inheritDoc} */
	public void enableVideo(boolean enable) {
		// TODO Auto-generated method stub
		
	}
}

