package fajitaboy;

import static fajitaboy.constants.AddressConstants.*;
import static fajitaboy.constants.BitmaskConstants.*;

import javax.sound.sampled.LineUnavailableException;
import javax.swing.JFrame;

/**
 * A debug class for testing the audio.
 * @author snigel
 */
public class AudioMain implements DrawsGameboyScreen {

    AddressBus ab;

    Audio au;

    GamePanel gp;

    int duration = 100;

    int volume = 225;

    public AudioMain() throws LineUnavailableException {
        JFrame jfr = new JFrame();
        gp = new GamePanel(2);
        jfr.setContentPane(gp);
        jfr.setVisible(true);

        System.out.println("Running " + INSTRUCTIONS + " instructions");

        au = new Audio();
        ab = new AddressBus("/home/johan/bombjack.gb");

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

        gp.drawGameboyScreen(data);
        
        // if (i%70000==0){ //don't generate sound in the beginning
        int low = ab.read(SOUND1_LOW);
        int high = ab.read(SOUND1_HIGH);
        int freq = (131072 / (2048 - (((high & 0x7) << 8) | low)));
        System.out.println(freq+" "+low+" "+high);
        //int i;
        //System.out.println(i++);
        au.generateTone(freq, duration, volume);
        // }
    }
}
