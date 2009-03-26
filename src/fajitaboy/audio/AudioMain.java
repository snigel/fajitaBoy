package fajitaboy.audio;

import static fajitaboy.constants.AddressConstants.*;
import static fajitaboy.constants.BitmaskConstants.*;

import javax.sound.sampled.LineUnavailableException;
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
    Audio au2;
    Audio2 au3;
    Audio3 au4;

    GamePanel gp;

    int duration = 100;

    int volume = 225;

    public AudioMain() throws LineUnavailableException {
        JFrame jfr = new JFrame();
        gp = new GamePanel(2);
        jfr.setContentPane(gp);
        jfr.setVisible(true);

        System.out.println("Running " + INSTRUCTIONS + " instructions");

//        au2 = new Audio();
        ab = new AddressBus("/home/johan/bombjack.gb");
        au1 = new Audio(ab, SOUND1_LOW, SOUND1_HIGH);
        au2 = new Audio(ab, SOUND2_LOW, SOUND2_HIGH);
        au3 = new Audio2(ab, SOUND3_LOW, SOUND3_HIGH);
        au4 = new Audio3(ab);

        Cpu cpu = new Cpu(ab);
        Oscillator oc = new Oscillator(cpu, ab, this);

        // c.reset();
        for (int i = 0; i < INSTRUCTIONS; i++) {
            // System.out.println("looping");
            oc.step();
            /*
             * public static final int AUDIO_FREQ_MASK = 0x38; public static
             * final int AUDIO_UPPER_BYTE = 5;
             */

            // System.out.println(cpu.getSP());
            // if(ab.read(0xFF24)>1)
            // System.out.println(ab.read(0xFF24));
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
        // TODO Auto-generated method stub

        //gp.drawGameboyScreen(data);
       // int low1 = ab.read(SOUND1_LOW);
       // int high1 = ab.read(SOUND1_HIGH)*0x100;
        // int freq1 = 131072/(2047-(high1+low1)&0x7ff);
        au1.generateTone();
          au2.generateTone();
        au3.generateTone();
        au4.generateTone();

/*
        int low2 = ab.read(SOUND2_LOW);
        int high2 = ab.read(SOUND2_HIGH)*0x100;
        int freq2 = 131072/(2047-(high1+low1)&0x7ff);
        au2.generateTone(freq2, duration, volume);
*/
    }
}
