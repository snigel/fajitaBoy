package fajitaboy;

import static fajitaboy.constants.AddressConstants.*;
import static fajitaboy.constants.BitmaskConstants.*;

import javax.sound.sampled.LineUnavailableException;

/**
 * A debug class for testing the audio.
 * @author snigel
 *
 */
public class AudioMain {

    /**
     * Number of instructions to run the audiotest.
     */
    private static final int INSTRUCTIONS = 5000000;

    /**
     * Starts the audiotest class.
     * @param args
     * No arguments
     * @throws
     * LineUnavailable
     */
    public static void main(final String[] args)
        throws LineUnavailableException {
        System.out.println("Running " + INSTRUCTIONS + " instructions");
        Audio au = new Audio();
        AddressBus ab = new AddressBus("/1942.gb");
        Cpu cpu = new Cpu(ab);
        Oscillator oc = new Oscillator(cpu, ab);

        int duration = 16;
        int volume = 125;
        // c.reset();
        for (int i = 0; i < INSTRUCTIONS; i++) {
            // System.out.println("looping");
            oc.step();
            int low = ab.read(SOUND3_LOW);
            int high = ab.read(SOUND3_HIGH);
            int freq = (high & AUDIO_FREQ_MASK) << AUDIO_UPPER_BYTE | low;
            freq = 131072 / (2048 - freq);
            if (i > 100000){ //don't generate sound in the beginning
                au.generateTone(freq, duration, volume);
            }
            // System.out.println(cpu.getSP());
            // if(ab.read(0xFF24)>1)
            // System.out.println(ab.read(0xFF24));
        }
    }
}
