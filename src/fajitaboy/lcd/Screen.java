package fajitaboy.lcd;

import static fajitaboy.constants.LCDConstants.*;

public class Screen {
	public int bits[][];

	public Screen() {
		bits = new int[GB_LCD_H][GB_LCD_W];
	}
	
	public void clear() {
		clear(0);
	}
	
	public void clear(int clr) {
		for ( int x = 0; x < GB_LCD_H; x++ ) {
			clearLine(x, clr);
		}
	}
	
	public void clearLine(int clr, int ly) {
		for ( int x = 0; x < GB_LCD_W; x++ ) {
			bits[ly][x] = clr;
		}
	}
	
	/**
	 * Blits a tile onto the screen
	 * 
	 * @param t Tile to be drawn
	 * @param x X-position to draw tile at
	 * @param y Y-position to draw tile at
	 * @param ly Line to blit to on screen
	 * @param transparent If true, palette index 0 is not rendered. 
	 * @throws Exception
	 */
	public void blitTile(Tile t, int palette, int x, int y, int ly, boolean transp ) {
		// Abort if no blitting occurs
		if ( y > ly || y + 8 <= ly || x <= -8 || x >= 160 )
			return;
		
		// Prepare variables
		int sx, sy, tx, ty;
		sy = ly;
		sx = Math.max(0, x);
		if ( x < 0 ) {
			tx = -x;
		} else {
			tx = 0;
		}
		ty = ly - y;

		// Blit pixels to screen
		if ( transp ) {
			while( sx < 160 && tx < 8 ) {
				int newidx = t.bits[ty][tx];
				if (newidx != 0) {
					bits[sy][sx] = 0x03 & palette >> newidx*2;
				}
				sx++;
				tx++;
			}
		} else {
			while( sx < 160 && tx < 8 ) {
				int newidx = t.bits[ty][tx];
				bits[sy][sx] = 0x03 & palette >> newidx*2;
				sx++;
				tx++;
			}	
		}
	}
	
	public void blitSprite(Tile t, int palette, int x, int y, int ly, boolean xFlip, boolean yFlip ) {
		// Abort if no blitting occurs
		if ( y > ly || y + 8 <= ly || x <= -8 || x >= 160 )
			return;
		
		// Prepare variables
		int sx, sy, tx, ty;
		sy = ly;
		sx = Math.max(0, x);
		if ( x < 0 ) {
			tx = -x;
		} else {
			tx = 0;
		}
		ty = ly - y;
		
		if ( yFlip )
			ty = 7 - ty;

		// Blit pixels to screen
		if ( xFlip ) {
			tx = 7 - tx;
			while( sx < 160 && tx >= 0 ) {
				int newidx = t.bits[ty][tx];
				if (newidx != 0) {
					bits[sy][sx] = 0x03 & palette >> newidx*2;
				}
				sx++;
				tx--;
			}
		} else {
			while( sx < 160 && tx < 8 ) {
				int newidx = t.bits[ty][tx];
				if (newidx != 0) {
					bits[sy][sx] = 0x03 & palette >> newidx*2;
				}
				sx++;
				tx++;
			}
		}
	}
	
	public int[][] getBits() {
		return bits;
	}
}