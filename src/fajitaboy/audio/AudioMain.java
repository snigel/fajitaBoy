package fajitaboy.audio;

import static fajitaboy.constants.AddressConstants.*;
import static fajitaboy.constants.BitmaskConstants.*;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import javax.swing.JFrame;

import fajitaboy.Cpu;
import fajitaboy.DrawsGameboyScreen;
import fajitaboy.GamePanel;
import fajitaboy.Oscillator;
import fajitaboy.memory.AddressBus;

/**
 * A debug class for testing the audio.
 * @author snigel
 */
public class AudioMain implements DrawsGameboyScreen {

    AddressBus ab;
    Audio au1;
    Audio2 au2;
    Audio3 au3;
    Audio4 au4;

    GamePanel gp;

    float samplerate = 44100;
    int samples = 1100;
    byte[] destBuff = new byte[samples];
    AudioFormat af;
    SourceDataLine sdl;

    public AudioMain() throws LineUnavailableException {
        af = new AudioFormat(samplerate, 8, 1, true, false);
        DataLine.Info info = new DataLine.Info(SourceDataLine.class, af);
        sdl = (SourceDataLine) AudioSystem.getLine(info);
        sdl.open(af);
        sdl.start();

        JFrame jfr = new JFrame();
        gp = new GamePanel(2);
        jfr.setContentPane(gp);
        jfr.setVisible(true);


        ab = new AddressBus("/bombjack.gb");
        au1 = new Audio(ab, SOUND1_LOW, SOUND1_HIGH, samples);
        au2 = new Audio2(ab, SOUND2_LOW, SOUND2_HIGH, samples);
        au3 = new Audio3(ab, SOUND3_LOW, SOUND3_HIGH, samples);
        au4 = new Audio4(ab, samples);

        Cpu cpu = new Cpu(ab);
        Oscillator oc = new Oscillator(cpu, ab, this);

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

    public void drawGameboyScreen(int[][] data) {
        destBuff = new byte[samples];
        //gp.drawGameboyScreen(data);
         au1.generateTone(destBuff);
         au2.generateTone(destBuff);
        au3.generateTone(destBuff);
      //   au4.generateTone(destBuff);
         sdl.write(destBuff, 0, samples);
    }
}
