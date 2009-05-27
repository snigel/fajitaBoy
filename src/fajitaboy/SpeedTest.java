package fajitaboy;

import fajitaboy.ReadRom;

import fajitaboy.gb.Cpu;
import fajitaboy.gb.Oscillator;
import fajitaboy.gb.memory.AddressBus;

/**
 * A simple benchmark for instructions.
 * @author arvid
 */
@Deprecated
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
    @Deprecated
    private void test() {
      int[] rom = ReadRom.readRom("/1942.gb");
      AddressBus  a = new AddressBus(rom);
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
