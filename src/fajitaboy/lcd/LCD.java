package fajitaboy.lcd;

import static fajitaboy.constants.AddressConstants.*;
import static fajitaboy.constants.MessageConstants.*;
import static fajitaboy.constants.LCDConstants.*;

import fajitaboy.ClockPulseReceiver;
import fajitaboy.MemoryInterface;

/**
 * @author Tobias Svensson
 *
 */
public class LCD implements ClockPulseReceiver {

	/**
	 * Pointer to MemoryInterface class.
	 */
    MemoryInterface ram;
    
    Sprite[] sprites = new Sprite[GB_SPRITES];
    BackgroundMap bgm = new BackgroundMap();
    Screen screen = new Screen();
	
    boolean newScreen = false;
    
	/* (non-Javadoc)
	 * @see ClockPulseReceiver#oscillatorMessage(int)
	 */
	
	/**
	 * Creates a new LCD with default values.
	 * 
	 * @param ram Pointer to an MemoryInterface.
	 */
	public LCD( MemoryInterface ram ) {
		this.ram = ram;
		reset();
	}
	
	/**
	 * Resets LCD to default state.
	 */
	public void reset() {
		
	}
	
	/**
	 * Draws the GameBoy screen once.
	 */
	private void drawScreen() {
//	 		Clear screen
			screen.clear();
			
//			Read OAM (Sprites, Tiles, Backgroudn Map...)
			// Read OAM
			// Read Sprites
			// Read Tiles
			bgm.readBackground(ram, BackgroundMap.MapType.BACKGROUND);
			
//	 		Draw background map
			bgm.draw(screen, ram);
			
//	 		Draw sprites

	}
	
	/**
	 * Converts 2 bytes into 8 4-color pixels.
	 * @param bitsLo Low bits of pixels
	 * @param bitsHi High bits of pixels
	 * @return Array of 8 pixels
	 */
	public static int[] convertToPixels(int bitsLo, int bitsHi) {
		int[] pixels = new int[8];
		
		if ( (bitsLo & 0x80) != 0 ) pixels[0] += 1;
		if ( (bitsLo & 0x40) != 0 ) pixels[1] += 1;
		if ( (bitsLo & 0x20) != 0 ) pixels[2] += 1;
		if ( (bitsLo & 0x10) != 0 ) pixels[3] += 1;
		if ( (bitsLo & 0x08) != 0 ) pixels[4] += 1;
		if ( (bitsLo & 0x04) != 0 ) pixels[5] += 1;
		if ( (bitsLo & 0x02) != 0 ) pixels[6] += 1;
		if ( (bitsLo & 0x01) != 0 ) pixels[7] += 1;
		
		if ( (bitsHi & 0x80) != 0 ) pixels[0] += 2;
		if ( (bitsHi & 0x40) != 0 ) pixels[1] += 2;
		if ( (bitsHi & 0x20) != 0 ) pixels[2] += 2;
		if ( (bitsHi & 0x10) != 0 ) pixels[3] += 2;
		if ( (bitsHi & 0x08) != 0 ) pixels[4] += 2;
		if ( (bitsHi & 0x04) != 0 ) pixels[5] += 2;
		if ( (bitsHi & 0x02) != 0 ) pixels[6] += 2;
		if ( (bitsHi & 0x01) != 0 ) pixels[7] += 2;
		
		return pixels;
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
			drawScreen();
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
	
	public boolean newScreenAvailable() {
		return newScreen;
	}
	
	public int[][] getScreen() {
		newScreen = false;
		return screen.getBits();
	}

}
