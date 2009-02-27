import static constants.HardwareConstants.*;
import static constants.AddressConstants.*;
import static constants.MessageConstants.*;

/**
 * Oscillator emulates the clock frequency of the Game Boy and
 * tells different devices to do different things at different
 * clock cycles. 
 * 
 * @author Tobias S
 */
public class Oscillator {
	int cycles; // Cycles since init
	int nextVBlank, nextLineInc, nextModeChange, prevTimerInc, nextDividerInc;
	Cpu cpu;
	AddressBus ram;
	LCD lcd;

	public Oscillator(Cpu cpu, AddressBus ram) {
		this.cpu = cpu;
		this.ram = ram;
		reset();
	}

	public void reset() {
		cycles = 0;
		nextVBlank = GB_CYCLES_PER_FRAME;
		nextLineInc = GB_CYCLES_PER_LINE;
		prevTimerInc = 0;
		nextDividerInc = 0;
		
	}

	public void step() {
		// Step CPU
		cycles += cpu.step();

		
		/*if ( cycles >= nextVBlank ) {
			// Set V-Blank interrupt flag
			ram.write(ADDRESS_IF, ram.read(ADDRESS_IF) | 0x01);
			nextVBlank += GB_CYCLES_PER_FRAME;
		}*/

		// Increment Timer
		int tac = ram.read(ADDRESS_TAC);
		int timerFreq = 0;
		switch(tac & 0x03) {
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
				if(tima == 0xFF) {
					ram.write(ADDRESS_TIMA, ram.read(ADDRESS_TMA));
					ram.write(ADDRESS_IF, ram.read(ADDRESS_IF) | 0x04);
				} else {
					ram.write(ADDRESS_TIMA, tima + 1);
				}
			}
			prevTimerInc += timerFreq;
		}
		
		// Check for LCD register status change
		// (Inte ens i närheten av klart....)
		if ( cycles > nextModeChange ) {
			lcd.oscillatorMessage( MSG_LCD_CHANGE_MODE );
			
			int ly = ram.read(ADDRESS_LY);
			if ( ly == 144 ) {
				lcd.oscillatorMessage( MSG_LCD_VBLANK );
			}
		}
	}
}
