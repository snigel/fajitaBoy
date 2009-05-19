package fajitaboy.gb;

import static fajitaboy.constants.AddressConstants.ADDRESS_DIV;
import static fajitaboy.constants.AddressConstants.ADDRESS_IF;
import static fajitaboy.constants.AddressConstants.ADDRESS_TAC;
import static fajitaboy.constants.AddressConstants.ADDRESS_TIMA;
import static fajitaboy.constants.AddressConstants.ADDRESS_TMA;
import static fajitaboy.constants.HardwareConstants.GB_DIV_CLOCK;
import static fajitaboy.constants.HardwareConstants.GB_TIMER_CLOCK_0;
import static fajitaboy.constants.HardwareConstants.GB_TIMER_CLOCK_1;
import static fajitaboy.constants.HardwareConstants.GB_TIMER_CLOCK_2;
import static fajitaboy.constants.HardwareConstants.GB_TIMER_CLOCK_3;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import fajitaboy.FileIOStreamHelper;
import fajitaboy.gb.memory.MemoryInterface;

public class Timer implements StateMachine {
	
    /**
     * Previous cycle at which the Timer was incremented.
     */
    private long timerCycles;

    /**
     * Next cycle at which the Divider will be incremented.
     */
    private long dividerCycles;
    
    /**
     * Default constructor.
     */
    public Timer() {
    	reset();
    }
    
    public void reset() {
    	timerCycles = 0;
        dividerCycles = 0;
    }
    
    /**
     * Updates the timer a certain amount of cycles
     * @param cycleInc Cycles to update timer
     * @param ram Pointer to memory
     */
    public void update( int cycleInc, MemoryInterface ram ) {
    	timerCycles += cycleInc;
        dividerCycles += cycleInc;
    	
    	// Update timer
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
        
        if ( timerCycles >= timerFreq ) {
            if ((tac & 0x04) != 0) {
                int tima = ram.read(ADDRESS_TIMA);
                if (tima == 0xFF) {
                    ram.write(ADDRESS_TIMA, ram.read(ADDRESS_TMA));
                    ram.write(ADDRESS_IF, ram.read(ADDRESS_IF) | 0x04);
                } else {
                    ram.write(ADDRESS_TIMA, tima + 1);
                }
            }
            
            timerCycles %= timerFreq;
        }
        
        // Update Div
        if ( dividerCycles >= GB_DIV_CLOCK ) {
    		ram.forceWrite( ADDRESS_DIV, (ram.read(ADDRESS_DIV) + 1) & 0xFF );
    		dividerCycles %= GB_DIV_CLOCK;
    	}
    }
    
    /**
     * {@inheritDoc}
     */
    public void saveState( FileOutputStream os ) throws IOException {
    	FileIOStreamHelper.writeData(os, dividerCycles, 8);
    	FileIOStreamHelper.writeData(os, timerCycles, 8);
    }
    
    /**
     * {@inheritDoc}
     */
    public void readState( FileInputStream is ) throws IOException {
    	dividerCycles = FileIOStreamHelper.readData(is, 8);
    	timerCycles = FileIOStreamHelper.readData(is, 8);
    }
}
