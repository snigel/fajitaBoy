package fajitaboy;

import static fajitaboy.constants.HardwareConstants.*;
import static fajitaboy.constants.AddressConstants.*;
import static fajitaboy.constants.MessageConstants.*;

/**
 * Oscillator emulates the clock frequency of the Game Boy and tells different
 * devices to do different things at different clock cycles.
 * @author Tobias S
 */
public class Oscillator {

    /**
     * Cycles since Oscillator initialization.
     */
    private int cycles;

    /**
     * Next cycle at which the LCD will proceed to next line.
     */
    private int nextLineInc;

    /**
     * Next cycle at which the LCD will change its mode.
     */
    private int nextModeChange;

    /**
     * Previous cycle at which the Timer was incremented.
     */
    private int prevTimerInc;

    /**
     * Next cycle at which the Divider will be incremented.
     */
    private int nextDividerInc;

    /**
     * set to true if LY == LYC
     */
    private boolean lycHit;

    /**
     * Pointer to CPU instance.
     */
    private Cpu cpu;

    /**
     * Pointer to AddressBus instance.
     */
    private AddressBus ram;

    /**
     * Pointer to LCD instance.
     */
    private LCD lcd;

    /**
     * Creates a new Oscillator with default values.
     * @param cpu
     *            Pointer to CPU instance.
     * @param ram
     *            Pointer to AddressBus instance.
     */
    public Oscillator(Cpu cpu, AddressBus ram) {
        this.cpu = cpu;
        this.ram = ram;
        lcd = new LCD(ram);
        reset();
    }

    /**
     * Resets the Oscillator to default values.
     */
    public void reset() {
        cycles = 0;
        nextLineInc = GB_CYCLES_PER_LINE;
        nextModeChange = GB_CYCLES_PER_LINE;
        prevTimerInc = 0;
        nextDividerInc = 0;
        lycHit = false;
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

        updateTimer();

        updateLCD();

        return cycleInc;
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
        if (cycles > nextLineInc) {
            int returnMsg = lcd.oscillatorMessage(MSG_LCD_NEXT_LINE);
            nextLineInc += GB_CYCLES_PER_LINE;
            if ((returnMsg & MSG_LCD_LYC_HIT) != 0) {
                lycHit = true;
            } else {
                lycHit = false;
            }

            // Check line # for VBlank and reset to 0
            ly = ram.read(ADDRESS_LY);
            if (ly == 144) {
                lcd.oscillatorMessage(MSG_LCD_VBLANK);
            } else if (ly >= 154) {
                ram.write(ADDRESS_LY, 0);
            }
        }

        if (cycles > nextModeChange) {
            ly = ram.read(ADDRESS_LY);
            if (ly < 144) {
                lcd.oscillatorMessage(MSG_LCD_CHANGE_MODE);

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
            if ((stat & 0x80) != 0) {
                ram.write(ADDRESS_IF, ram.read(ADDRESS_IF) | 0x02);
            }
        }
    }
}
