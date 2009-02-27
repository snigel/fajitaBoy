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
//			Trigger VBlank interrupt if enabled
			int stat = ram.read(ADDRESS_STAT);
			if ( (stat & 0x010) != 0 ) {
				ram.write( ADDRESS_IF, ram.read(ADDRESS_IF) | 0x01 );
			}
			ram.write( ADDRESS_STAT, ram.read( ADDRESS_STAT ) & 0xFC ); // Replace with forcedWrite
		}
		
		if ( message == MSG_LCD_CHANGE_MODE ) {
			int stat = ram.read(ADDRESS_STAT);
			int mode = stat | 0x03;
			stat = stat & 0xFC;
			
			switch ( mode ) {
			case 0:
			case 1:
				stat += 2;
//				Trigger LCDSTAT interrupt if enabled
				if ( (stat & 0x020) != 0 ) {
					ram.write( ADDRESS_IF, ram.read( ADDRESS_IF ) | 0x02 );
				}
				break;
			case 2:
				stat += 3;
				break;
			case 3:
//				Trigger HBlank interrupt if enabled
				if ( (stat & 0x08) != 0 ) {
					
					ram.write( ADDRESS_IF, ram.read( ADDRESS_IF ) | 0x02 );
				}
				break;
			}
			ram.write( ADDRESS_STAT, stat ); // Replace with forcedWrite
		}
		
		if ( message == MSG_LCD_NEXT_LINE ) {
			// Increment LY by one
		}
	}

}
