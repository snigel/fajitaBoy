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
		
		blit(s.bits, palette, lcdLeft, lcdTop, sx, sy, sw, sh, new IgnorantBlend(0));
	}
	
	/**
	 * Blits a tile onto the screen
	 * 
	 * @param t Tile to be drawn
	 * @param x X-position to draw tile at
	 * @param y Y-position to draw tile at
	 * @throws Exception
	 */
	public void blit(Tile t, int palette, int x, int y, BlendStrategy bs) {
		
		int lcdLeft, lcdTop, lcdRight, lcdBottom;
		lcdLeft = Math.max(0, x);
		lcdTop = Math.max(0, y);
		lcdRight = Math.min(x + GB_TILE_W, GB_LCD_W);
		lcdBottom = Math.min(y + GB_TILE_H, GB_LCD_H);

		if (lcdLeft > GB_LCD_W || lcdTop > GB_LCD_H) 
			return;
		
		int tx, ty, tw, th;
		ty = lcdTop - y;
		tx = lcdLeft - x;
		tw = lcdRight - lcdLeft;
		th = lcdBottom - lcdTop;

		
		assert lcdLeft < lcdRight && lcdBottom < lcdTop : 
			"Trying to blit, left smaller than right, or top smaller than bottom";  
		assert tw != 0 && th != 0 : "Trying to blit very small sprite";
		
		blit(t.bits, palette, lcdLeft, lcdTop, tx, ty, tw, th, bs);
		
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
	private void blit(int[][] data, int palette, int sx, int sy, int dx, int dy, int dw, int dh, BlendStrategy bs) {
		for (int cdy = dy, csy = sy; cdy < dy + dh; csy++, cdy++) {
			for (int cdx = dx, csx = sx; cdx < dx + dw; csx++, cdx++) {
				int oldpxl = bits[csy][csx];
				// skriv om med lookup istället
				int newpxl = 0x03 & palette >> data[cdy][cdx]*2;
		       	bits[csy][csx] = bs.blend(oldpxl, newpxl);
			}
		}
	}
	
	public int[][] getBits() {
		return bits;
	}
}