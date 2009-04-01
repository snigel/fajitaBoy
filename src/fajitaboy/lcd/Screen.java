package fajitaboy.lcd;

import static fajitaboy.constants.LCDConstants.*;

public class Screen {
	public int bits[][];

	public Screen() {
		bits = new int[GB_LCD_H][GB_LCD_W];
	}
	
	public void clear() {
		for ( int x = 0; x < GB_LCD_W; x++ ) {
			for ( int y = 0; y < GB_LCD_H; y++ ) {
				bits[y][x] = 0;
			}
		}
	}
	
	public void clear(int clr) {
		for ( int x = 0; x < GB_LCD_W; x++ ) {
			for ( int y = 0; y < GB_LCD_H; y++ ) {
				bits[y][x] = clr;
			}
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
	
	
	
	/**
	 * @param data 
	 * 		data to blit
	 * @param palette
	 * 		palette to use
	 * @param sx
	 * 		start x value in screen
	 * @param sy
	 * 		start y value in screen
	 * @param dx
	 * 		start x value in data
	 * @param dy
	 * 		start y value in data
	 * @param dw
	 * 		width of data to blit
	 * @param dh
	 * 		height of data to blit
	 * @param ignore
	 * 		data value to ignore
	 */
/*	private void blit(int[][] data, int palette, int sx, int sy, int dx, int dy, int dw, int dh) {
		for (int cdy = dy, csy = sy; cdy < dy + dh; csy++, cdy++) {
			for (int cdx = dx, csx = sx; cdx < dx + dw; csx++, cdx++) {
				int newidx = data[cdy][cdx];
				if (newidx != 0) {
					bits[csy][csx] = 0x03 & palette >> newidx*2;
				}
			}
		}
	} */
	
	public int[][] getBits() {
		return bits;
	}
}