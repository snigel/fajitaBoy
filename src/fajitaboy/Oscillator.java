package fajitaboy;

import static fajitaboy.constants.HardwareConstants.*;
import static fajitaboy.constants.AddressConstants.*;
import fajitaboy.lcd.LCD;
import fajitaboy.memory.AddressBus;
import fajitaboy.memory.MemoryInterface;

/**
 * Oscillator emulates the clock frequency of the Game Boy and tells different
 * devices to do different things at different clock cycles.
 * @author Tobias S
 */
public class Oscillator implements Runnable{

    /**
     * Max number of frames to skip in a row.
     * TODO move this constant to a proper constant class.
     */
    private final int MAX_FRAMESKIP = 10;
    
    private boolean running;
    
    /**
     * if true the screen is not updated on vblank.
     */
    private boolean frameSkip;
    
    /**
     * Next cycle to stop and wait if going too fast
     */
    private long nextHaltCycle;
    
    /**
     * Cycles since Oscillator initialization.
     */
    private long cycles;

    /**
     * Next cycle at which the LCD will proceed to next line.
     */
    private long nextLineInc;

    /**
     * Next cycle at which the LCD will change its mode.
     */
    private long nextModeChange;

    /**
     * Previous cycle at which the Timer was incremented.
     */
    private long prevTimerInc;

    /**
     * Next cycle at which the Divider will be incremented.
     */
    private long nextDividerInc;

    /**
     * Pointer to CPU instance.
     */
    private Cpu cpu;

    /**
     * Pointer to MemoryInterface instance.
     */
    private MemoryInterface ram;

    /**
     * Pointer to LCD instance.
     */
    private LCD lcd;

    private DrawsGameboyScreen dgs; 
    
    /**
     * Creates a new Oscillator with default values.
     * @param cpu
     *            Pointer to CPU instance.
     * @param ram
     *            Pointer to MemoryInterface instance.
     */
    public Oscillator(Cpu cpu, AddressBus ram) {
        this.cpu = cpu;
        this.ram = ram;
        lcd = new LCD(ram);
        reset();
    }

    /**
     * Creates a new Oscillator with default values.
     * @param cpu
     *            Pointer to CPU instance.
     * @param ram
     *            Pointer to MemoryInterface instance.
     */
    public Oscillator(Cpu cpu, AddressBus ram, DrawsGameboyScreen dgs) {
        this(cpu,ram);
        this.dgs = dgs; 
    }
    
    /**
     * Resets the Oscillator to default values.
     */
    public void reset() {
        cycles = 0;
        nextLineInc = GB_CYCLES_PER_LINE;
        nextModeChange = GB_CYCLES_PER_LINE;
        prevTimerInc = 0;
        nextDividerInc = GB_DIV_CLOCK;
        frameSkip = false;
        nextHaltCycle = GB_CYCLES_PER_FRAME;
        running = false;
    }

    /**
     * Step the Oscillator once. The Oscillator causes the CPU to step once, and
     * sends messages to other components at certain cycles.
     * @return Returns step time in cycles.
     */
    public int step() {
        // Step CPU
        int cycleInc = cpu.step();
        cycles += cycleInc;
        
        updateDiv();
        updateTimer();
        updateLCD();

        return cycleInc;
    }

    /**
     * Increment Divider register.
     */
    private void updateDiv() {
    	if ( cycles >= nextDividerInc ) {
    		ram.forceWrite( ADDRESS_DIV, (ram.read(ADDRESS_DIV) + 1) & 0xFF );
    		nextDividerInc += GB_DIV_CLOCK;
    	}
    }
    
    /**
     * Increment Timer.
     */
    private void updateTimer() {
        int tac = ram.read(ADDRESS_TAC);
        int timerFreq = 0;
        switch (tac & 0x03) {
        case 0:
            timerFreq = GB_TIMER_CLOCK_0;
            break;
        case 1:
            timerFreq = GB_TIMER_CLOCK_1;
            break;
        case 2:
            timerFreq = GB_TIMER_CLOCK_2;
            break;
        case 3:
            timerFreq = GB_TIMER_CLOCK_3;
            break;
        }
        
        while (cycles >= prevTimerInc + timerFreq) {
            if ((tac & 0x04) != 0) {
                int tima = ram.read(ADDRESS_TIMA);
                if (tima == 0xFF) {
                    ram.write(ADDRESS_TIMA, ram.read(ADDRESS_TMA));
                    ram.write(ADDRESS_IF, ram.read(ADDRESS_IF) | 0x04);
                } else {
                    ram.write(ADDRESS_TIMA, tima + 1);
                }
            }
            prevTimerInc += timerFreq;
        }

    }

    /**
     * Check for LCD register status change.
     */
    private void updateLCD() {
        int ly;
        boolean lycHit = false;
        
        if (cycles > nextLineInc) {
        	lycHit = lcd.nextLine();
        	
            nextLineInc += GB_CYCLES_PER_LINE;

            // Check line # for VBlank and reset to 0
            ly = ram.read(ADDRESS_LY);
            if (ly == GB_VBLANK_LINE) {
                lcd.vblank();
                
                //call draw
                if (!frameSkip && dgs != null) {
                    dgs.drawGameboyScreen(lcd.getScreen());
                }  
            }
        }

        if (cycles > nextModeChange) {
            ly = ram.read(ADDRESS_LY);
            if (ly < 144) {
                lcd.changeMode();

                // Read current mode and determine wait time from that
                int mode = ram.read(ADDRESS_STAT) & 0x03;
                if (mode == 0) {
                    nextModeChange += GB_HBLANK_PERIOD;
                } else if (mode == 2) {
                    nextModeChange += GB_LCD_OAMSEARCH_PERIOD;
                } else if (mode == 3) {
                    nextModeChange += GB_LCD_TRANSFER_PERIOD;
                }
            } else {
                // This line nothing happens, see what happens next line...
                nextModeChange += GB_CYCLES_PER_LINE;
            }
        }

        // Handle LYC hit
        if (lycHit) {
            // Trigger LCDSTAT interrupt if LYC=LY Coincidence Interrupt is
            // enabled
            int stat = ram.read(ADDRESS_STAT);
            if ((stat & 0x40) != 0) {
                ram.write(ADDRESS_IF, ram.read(ADDRESS_IF) | 0x02);
            }
        }
    }

    public LCD getLCD() {
        return lcd;
    }

    /**
     * Starts the execution. Create a new thread and call start() to
     * make this method run in a separate thread.
     */
    public final void run() {
        running = true;
        long sleepTime;
        long nextUpdate = System.nanoTime();
        int frameSkipCount = 0;
        while (running) {
            // Oscillator.step();
            step();

            if (cycles > nextHaltCycle) {
                nextUpdate += GB_NANOS_PER_FRAME;
                sleepTime = nextUpdate - System.nanoTime();
                if (sleepTime >= 0) {
                    frameSkip = false;
                    lcd.frameSkip = false;
                    frameSkipCount = 0;
                    try {
                        Thread.sleep(sleepTime / 1000000);
                    } catch (InterruptedException e) {}
                 // Thread.sleep is not 100% accurate so we have to
                 // give some extra marginal before frame skip.
                } else if (sleepTime < -GB_NANOS_PER_FRAME) {
                    if (frameSkipCount >= MAX_FRAMESKIP) {
                        frameSkip = false;
                        lcd.frameSkip = false;
                        frameSkipCount = 0;
                        nextUpdate = System.nanoTime();
                    } else {
                        frameSkip = true;
                        lcd.frameSkip = true;
                        frameSkipCount++;
                    }
                }
                nextHaltCycle += GB_CYCLES_PER_FRAME;
            }
        }
        frameSkip = false;
    }

    /**
     * Method to call to stop the run() loop.
     */
    public final void stop() {
        running = false;
    }

    /**
     * To know if run() is running.
     * @return true if the Oscillator is running, otherwise false.
     */
    public final boolean isRunning() {
        return running;
    }

}
