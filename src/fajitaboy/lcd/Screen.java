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
	
	/**
	 * Blits a sprite onto the screen.
	 * 
	 * @param s Sprite to be drawn.
	 * @param x X-coordinate to draw tile at (minus 8)
	 * @param y Y-coordinate to draw tile at (minus 16)
	 * @throws Exception
	 */
	public void blit(Sprite s, int palette, int x, int y) {
		int lcdLeft, lcdTop, lcdRight, lcdBottom;
		lcdLeft = Math.max(0, x - 8);
		lcdRight = Math.min(x, GB_LCD_W);
		lcdTop = Math.max(0, y - 16);
		lcdBottom = Math.min(y - 8, GB_LCD_H);
		
		// Exit if sprite outside screen
		if ( lcdLeft >= lcdRight || lcdTop >= lcdBottom ) {
			assert (true) : "Trying to blit outside of screen"; 
			return;
		}
		
		int sx, sy, sw, sh;
		sy = lcdTop - y + 16;
		sx = lcdLeft - x + 8;
		sw = lcdRight - lcdLeft;
		sh = lcdBottom - lcdTop;
		
		assert lcdLeft < lcdRight && lcdBottom < lcdTop : 
			"Trying to blit, left smaller than right, or top smaller than bottom";  
		assert sw != 0 && sh != 0 : "Trying to blit very small sprite";
		
		blitIgnore(s.bits, palette, lcdLeft, lcdTop, sx, sy, sw, sh, 0);
	}
	
	/**
	 * Blits a tile onto the screen
	 * 
	 * @param t Tile to be drawn
	 * @param x X-position to draw tile at
	 * @param y Y-position to draw tile at
	 * @throws Exception
	 */
	public void blit(Tile t, int palette, int x, int y, int ignore) {
		
		int lcdLeft, lcdTop, lcdRight, lcdBottom;
		lcdLeft = Math.max(0, x);
		lcdTop = Math.max(0, y);
		
		if (lcdLeft > GB_LCD_W || lcdTop > GB_LCD_H) 
			return;

		lcdRight = Math.min(x + GB_TILE_W, GB_LCD_W);
		lcdBottom = Math.min(y + GB_TILE_H, GB_LCD_H);
		
		int tx, ty, tw, th;
		ty = lcdTop - y;
		tx = lcdLeft - x;
		tw = lcdRight - lcdLeft;
		th = lcdBottom - lcdTop;

		
		assert lcdLeft < lcdRight && lcdBottom < lcdTop : 
			"Trying to blit, left smaller than right, or top smaller than bottom";  
		assert tw != 0 && th != 0 : "Trying to blit very small sprite";
		
		blitIgnore(t.bits, palette, lcdLeft, lcdTop, tx, ty, tw, th, ignore);
		
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
	private void blitIgnore(int[][] data, int palette, int sx, int sy, int dx, int dy, int dw, int dh, int ignore) {
		for (int cdy = dy, csy = sy; cdy < dy + dh; csy++, cdy++) {
			for (int cdx = dx, csx = sx; cdx < dx + dw; csx++, cdx++) {
				// TODO do something with palette
				int pxl = data[cdy][cdx];
                //if (pxl != ignore) {
                    // bits[csy][csx] = pxl;
                	int clr = 0x03 & palette >> pxl*2; 
                    bits[csy][csx] = clr;
                //}
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
	private void blitExclusive(int[][] data, int palette, int sx, int sy, int dx, int dy, int dw, int dh, int exclusive) {
		for (int cdy = dy, csy = sy; cdy < dy + dh; csy++, cdy++) {
			for (int cdx = dx, csx = sx; cdx < dx + dw; csx++, cdx++) {
				// TODO do something with palette
				int pxl = data[cdy][cdx];
                if (pxl == exclusive) {
                    bits[csy][csx] = pxl;
                	int clr = 0x03 & palette >> pxl*2; 
                    bits[csy][csx] = clr;
                }
			}
		}
	}
	
	public int[][] getBits() {
		return bits;
	}
}