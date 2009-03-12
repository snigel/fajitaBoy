package fajitaboy;

import static fajitaboy.constants.AddressConstants.*;
import static fajitaboy.constants.MessageConstants.*;

/**
 * @author Tobias Svensson
 *
 */
public class LCD implements ClockPulseReceiver {

	/**
	 * Pointer to AddressBus class.
	 */
	AddressBus ram;
	
	/* (non-Javadoc)
	 * @see ClockPulseReceiver#oscillatorMessage(int)
	 */
	
	/**
	 * Creates a new LCD with default values.
	 * 
	 * @param ram Pointer to an AddressBus.
	 */
	public LCD( AddressBus ram ) {
		this.ram = ram;
		reset();
	}
	
	/**
	 * Resets LCD to default state.
	 */
	public void reset() {
		
	}
	
	/**
	 * Receives and handles messages from the Oscillator.
	 * 
	 * @param message Message constant
	 */
	public int oscillatorMessage(int message) {
		if ( message == MSG_LCD_VBLANK ) {
//			Trigger VBlank interrupt if enabled
			int stat = ram.read(ADDRESS_STAT);
			if ( (stat & 0x010) != 0 ) {
				ram.write( ADDRESS_IF, ram.read(ADDRESS_IF) | 0x01 );
			}
			ram.forceWrite( ADDRESS_STAT, (ram.read( ADDRESS_STAT ) & 0xFC) + 1 ); // Replace with forcedWrite
		}
		
		if ( message == MSG_LCD_CHANGE_MODE ) {
			int stat = ram.read(ADDRESS_STAT);
			int mode = stat & 0x03;
			stat = stat & 0xFC;
			
			switch ( mode ) {
			case 0:
			case 1:
				stat += 2;
//				Trigger LCDSTAT interrupt if Mode 2 OAM interrupt is enabled
				if ( (stat & 0x020) != 0 ) {
					ram.write( ADDRESS_IF, ram.read( ADDRESS_IF ) | 0x02 );
				}
				break;
			case 2:
				stat += 3;
				break;
			case 3:
//				Trigger LCDSTAT interrupt if Mode 0 HBlank interrupt is enabled
				if ( (stat & 0x08) != 0 ) {
					
					ram.write( ADDRESS_IF, ram.read( ADDRESS_IF ) | 0x02 );
				}
				break;
			}
			
			ram.forceWrite( ADDRESS_STAT, stat );
		}
		
		if ( message == MSG_LCD_NEXT_LINE ) {
			// Increment LY by one
			int ly = ram.read( ADDRESS_LY ) + 0x01;
			int lyc = ram.read( ADDRESS_LYC );
			int stat = ram.read( ADDRESS_STAT );
			ram.forceWrite( ADDRESS_LY, ly );
			if ( ly == lyc ) {
// 				Coincidence bit on
				ram.forceWrite( ADDRESS_STAT, stat | 0x04 );
				return MSG_LCD_LYC_HIT;
			} else {
// 				Coincidence bit off
				ram.forceWrite( ADDRESS_STAT, stat & 0xFB );
			}
		}
		
		return 0;
	}

}
