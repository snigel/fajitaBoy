import static constants.AddressConstants.*;
import static constants.MessageConstants.*;

/**
 * @author Tobias Svensson
 *
 */
public class LCD implements ClockPulseReceiver {

	AddressBus ram;
	
	/* (non-Javadoc)
	 * @see ClockPulseReceiver#oscillatorMessage(int)
	 */
	
	public LCD( AddressBus ram ) {
		this.ram = ram;
	}
	
	public void reset() {
		
	}
	
	public void oscillatorMessage(int message) {
		if ( message == MSG_LCD_VBLANK ) {
			ram.write( ADDRESS_IF, ram.read(ADDRESS_IF) | 0x01 );
			// Forced write to LCDSTAT 
		}
		
		if ( message == MSG_LCD_CHANGE_MODE ) {
			// Change mode in order 2, 3, 0
			// Call HBlank interrupt when applicable
		}
		
		if ( message == MSG_LCD_NEXT_LINE ) {
			// Increment LY by one
		}
	}

}
