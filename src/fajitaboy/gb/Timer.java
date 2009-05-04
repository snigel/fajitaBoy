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
    private long prevTimerInc;

    /**
     * Next cycle at which the Divider will be incremented.
     */
    private long nextDividerInc;
    
    /**
     * Default constructor.
     */
    public Timer() {
    	reset();
    }
    
    public void reset() {
    	prevTimerInc = 0;
        nextDividerInc = GB_DIV_CLOCK;
    }
    
    /**
     * Updates the timer a certain amount of cycles
     * @param cycleInc Cycles to update timer
     * @param ram Pointer to memory
     */
    public void update( int cycleInc, MemoryInterface ram ) {
    	prevTimerInc -= cycleInc;
        nextDividerInc -= cycleInc;
    	
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
        
        if ( prevTimerInc < -timerFreq ) {
            if ((tac & 0x04) != 0) {
                int tima = ram.read(ADDRESS_TIMA);
                if (tima == 0xFF) {
                    ram.write(ADDRESS_TIMA, ram.read(ADDRESS_TMA));
                    ram.write(ADDRESS_IF, ram.read(ADDRESS_IF) | 0x04);
                } else {
                    ram.write(ADDRESS_TIMA, tima + 1);
                }
            }
            
            while ( prevTimerInc < 0 )  // TODO Not the best way to do it...
            	prevTimerInc += timerFreq;   /* Could maybe use prevTimerInc &= (timerFreq-1)
            								    but will that remove the sign? */ 
        }
        
        // Update Div
        if ( nextDividerInc < 0 ) {
    		ram.forceWrite( ADDRESS_DIV, (ram.read(ADDRESS_DIV) + 1) & 0xFF );
    		nextDividerInc += GB_DIV_CLOCK;
    	}
    }
    
    /**
     * {@inheritDoc}
     */
    public void saveState( FileOutputStream os ) throws IOException {
    	FileIOStreamHelper.writeData(os, nextDividerInc, 8);
    	FileIOStreamHelper.writeData(os, prevTimerInc, 8);
    }
    
    /**
     * {@inheritDoc}
     */
    public void readState( FileInputStream is ) throws IOException {
    	nextDividerInc = FileIOStreamHelper.readData(is, 8);
    	prevTimerInc = FileIOStreamHelper.readData(is, 8);
    }
}
