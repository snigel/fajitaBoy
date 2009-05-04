package fajitaboy.gb.lcd;

import static fajitaboy.constants.LCDConstants.*;

public class Screen {
	/**
	 * Contains the pixels that have been drawn onto the screen. 
	 */                    
	public int bits[][];

	public Screen() {
		bits = new int[LCD_H][LCD_W];
	}
	
	/**
	 * Clear screen. Fills screen with white.
	 */
	// TODO Decrepit function? Remove?
	public void clear() {
		clear(0);
	}
	
	/**
	 * Clears screen by filling it with a color.
	 * @param clr Color to fill screen with
	 */
	public void clear(int clr) {
		for ( int x = 0; x < LCD_H; x++ ) {
			clearLine(x, clr);
		}
	}
	
	/**
	 * Clears one line on the screen with the given color.
	 * @param clr Color to fill screen with
	 * @param ly Line to clear
	 */
	public void clearLine(int clr, int ly) {
		for ( int x = 0; x < LCD_W; x++ ) {
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
	
	/**
	 * Blits a sprite onto screen.
	 * @param t Tile to blit
	 * @param palette Sprite palette
	 * @param x Sprite X coordinate
	 * @param y Sprite Y Coordinate
	 * @param ly Screen line to blit onto
	 * @param xFlip Enable horisontal sprite flip
	 * @param yFlip Enable vertical sprite flip
	 */
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
	
	/**
	 * Returns the screen pixels
	 * @return Array of screen pixels
	 */
	public int[][] getBits() {
		return bits;
	}
}