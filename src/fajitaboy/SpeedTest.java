package fajitaboy;

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
    private static final int INSTRUCTIONS = 50000000;

    /**
     * Runs INSTRUCTIONS, times it and prints the time it took,
     * and how many cycles were run per second.
     */
    private void test() {
        System.out.println("Running " + INSTRUCTIONS + " instructions");

        AddressBus a = new AddressBus("/tetris_patched.gb");
        Cpu c = new Cpu(a);

        int cycl = 0;
        for (int i = 0; i < INSTRUCTIONS; i++) {
            cycl += c.step();
        }
        System.out.println("Cycles run: " + cycl);

        a = new AddressBus("/tetris_patched.gb");
        c = new Cpu(a);
        Oscillator o = new Oscillator(c, a);
        // c.reset();
        long t = System.currentTimeMillis();
        for (int i = 0; i < INSTRUCTIONS; i++) {
            o.step();
        }

        long totaltime = System.currentTimeMillis() - t;
        System.out.println("Time run (ms): " + totaltime);
        System.out.println("Cycles per instruction~: "
                + ((float) cycl / INSTRUCTIONS));
        System.out.println("cycles per second (hz): "
                + (cycl / totaltime * 1000));
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
