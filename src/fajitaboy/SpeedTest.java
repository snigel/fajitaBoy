package fajitaboy;

import fajitaboy.memory.AddressBus;

/**
 * A simple benchmark for instructions.
 * @author arvid
 */
public final class SpeedTest {

    /**
     * Creates a SpeedTest.
     */
    private SpeedTest() {
    }

    /**
     * Number of instructions to try.
     */
    private static final int INSTRUCTIONS = 500000;

    /**
     * Runs INSTRUCTIONS, times it and prints the time it took,
     * and how many cycles were run per second.
     */
    private void test() {
        
      AddressBus  a = new AddressBus("/1942.gb");
      Cpu c = new Cpu(a);
        Oscillator o = new Oscillator(c, a);
        for (int i = 0; i < INSTRUCTIONS; i++) {
            o.step();
        }
    }

    /**
     * Runs SpeedTest.
     * @param args
     * No parameters
     */
    public static void main(final String[] args) {
        (new SpeedTest()).test();
    }
}
