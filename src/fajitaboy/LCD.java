package fajitaboy;

import static fajitaboy.constants.AddressConstants.*;
import static fajitaboy.constants.LCDConstants.*;
import static fajitaboy.constants.MessageConstants.*;
import java.lang.Math.*;

import fajitaboy.memory.MemoryInterface;

/**
 * @author Tobias Svensson
 *
 */
public class LCD implements ClockPulseReceiver {

	private class Sprite {
		int bits[][];
	private
	    int h = 8; // Height of sprite in pixels
		
		public Sprite(int height) {
			h = height;
			bits = new int[8][height];
		}
		
		public void readSprite(int address) {
			
		}
		
		public int[][] getSprite() {
			return null;
		}
		
		public void setHeight( int height ) {
			if ( h != height ) {
				h = height;
				bits = new int[8][height];
			}
		}
	}
	
	private class Tile {
		public int bits[][];
		
		public Tile(boolean tall) {
			bits = new int[8][8];
		}
		
		public void readTile(int address) {
			
		}
		
		public int[][] getTile() {
			return null;
		}
	}
	
	private class Screen {
		public int bits[][];
		
		public Screen() {
			bits = new int[GB_LCD_W][GB_LCD_H];
		}
		
		public void clear() {
			for ( int x = 0; x < GB_LCD_W; x++ ) {
				for ( int y = 0; y < GB_LCD_H; y++ ) {
					bits[x][y] = 0;
				}
			}
		}
		
		public void blit(Sprite s, int x, int y) throws Exception {
//			Prepare variables
			int lcdLeft, lcdTop, lcdRight, lcdBottom, sLeft, sTop, sRight, sBottom, lcdx, lcdy, sx, sy;
			lcdLeft = Math.min(0, x);
			lcdRight = Math.max(x + 8, GB_LCD_W);
			lcdTop = Math.min(0, y);
			lcdBottom = Math.max(y + s.h, GB_LCD_H);
			
			// Exit if sprite outside screen
			if ( lcdLeft >= lcdRight || lcdTop >= lcdBottom ) {
				return;
			}
			
			sLeft = Math.max(0, -x);
			sRight = Math.min(8, GB_LCD_W - x);
			sTop = Math.max(0, -y);
			sBottom = Math.min(s.h, GB_LCD_H - y); 
			
			// Exit if sprite outside screen
			if ( sLeft >= sRight || sTop >= sBottom ) {
				return;
			}
			
// 			Safety guards for debug purposes...
			if ( lcdRight - lcdLeft != sRight - sLeft ) {
				throw new Exception("blitSprite: Blitted widths on screen and of sprite does not match.");
			}
			if ( lcdBottom - lcdTop != sBottom - sTop ) {
				throw new Exception("blitSprite: Blitted heights on screen and of sprite does not match.");
			}
			
//			Blit sprite to screen
			sy = sTop;
			// For each line...
			for ( lcdy = lcdTop; lcdy < lcdBottom; lcdy++ ) {
				sx = sLeft;
				// For each pixel...
				for ( lcdx = lcdLeft; lcdx < lcdRight; lcdx++ ) {
					bits[lcdx][lcdy] = s.bits[sx][sy];
				}
			}
		}
		
		public void blit(Tile t, int x, int y) throws Exception {
//			Prepare variables
			int lcdLeft, lcdTop, lcdRight, lcdBottom, tLeft, tTop, tRight, tBottom, lcdx, lcdy, tx, ty;
			lcdLeft = Math.min(0, x);
			lcdRight = Math.max(x + 8, GB_LCD_W);
			lcdTop = Math.min(0, y);
			lcdBottom = Math.max(y + 8, GB_LCD_H);
			
			// Exit if sprite outside screen
			if ( lcdLeft >= lcdRight || lcdTop >= lcdBottom ) {
				return;
			}
			
			tLeft = Math.max(0, -x);
			tRight = Math.min(8, GB_LCD_W - x);
			tTop = Math.max(0, -y);
			tBottom = Math.min(8, GB_LCD_H - y); 
			
			// Exit if sprite outside screen
			if ( tLeft >= tRight || tTop >= tBottom ) {
				return;
			}
			
// 			Safety guards for debug purposes...
			if ( lcdRight - lcdLeft != tRight - tLeft ) {
				throw new Exception("blitSprite: Blitted widths on screen and of tile does not match.");
			}
			if ( lcdBottom - lcdTop != tBottom - tTop ) {
				throw new Exception("blitSprite: Blitted heights on screen and of tile does not match.");
			}
			
//			Blit sprite to screen
			ty = tTop;
			// For each line...
			for ( lcdy = lcdTop; lcdy < lcdBottom; lcdy++ ) {
				tx = tLeft;
				// For each pixel...
				for ( lcdx = lcdLeft; lcdx < lcdRight; lcdx++ ) {
					bits[lcdx][lcdy] = t.bits[tx][ty];
				}
			}
		}
	}
	
	/**
	 * Pointer to MemoryInterface class.
	 */
    MemoryInterface ram;
	
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
	
	public int[] convertToPixels(int bitsLo, int bitsHi) {
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
