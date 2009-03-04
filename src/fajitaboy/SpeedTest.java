package fajitaboy;

public class SpeedTest {
    private static final int INSTRUCTIONS = 5000000;
    public static void main(String[] args) {
	System.out.println("Running " + INSTRUCTIONS + " instructions");

	AddressBus a = new AddressBus("fajitaboy/tetris.gb");
	Cpu c = new Cpu(a);

	int cycl = 0;
	for (int i = 0; i < INSTRUCTIONS; i++)
	    cycl += c.step();
	System.out.println("Cycles run: " + cycl);

	a = new AddressBus("fajitaboy/tetris.gb");
	c = new Cpu(a);
	//c.reset();
	long t = System.currentTimeMillis();
	for (int i = 0; i < INSTRUCTIONS; i++)
	    c.step();
	long totaltime = System.currentTimeMillis() - t;
	System.out.println("Time run (ms): " + totaltime);
	System.out.println("Cycles per instruction~: " + ((float)cycl / INSTRUCTIONS));
	System.out.println("cycles per second (hz): " + (cycl / totaltime * 1000));
    }
}
