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

    Audio au1;
    Audio au2;

    GamePanel gp;

    int duration = 100;

    int volume = 225;

    public AudioMain() throws LineUnavailableException {
        JFrame jfr = new JFrame();
        gp = new GamePanel(2);
        jfr.setContentPane(gp);
        jfr.setVisible(true);

        System.out.println("Running " + INSTRUCTIONS + " instructions");

        au1 = new Audio();
        au2 = new Audio();
        ab = new AddressBus("/tetris.gb");

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
        
        // if (i%70000==0){ //don't generate sound in the beginning
        int low1 = ab.read(SOUND1_LOW);
        int high1 = ab.read(SOUND1_HIGH)*0x100;
        int freq1 = (high1+low1)&0x7ff;
        //int freq1 = (131072 / (2048 - (((high1 & 0x7) << 8) | low1)));
        
        int low2 = ab.read(SOUND2_LOW);
        int high2 = ab.read(SOUND2_HIGH);
       // int freq2 = (131072 / (2048 - (((high2 & 0x7) << 8) | low2)));
        
       au1.generateTone(freq1, duration, volume);
      // au2.generateTone(freq2, duration, volume);
      // au2.noise();
        // }
    }
}
