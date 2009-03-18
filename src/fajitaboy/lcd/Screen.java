package fajitaboy.lcd;

import static fajitaboy.constants.LCDConstants.GB_LCD_H;
import static fajitaboy.constants.LCDConstants.GB_LCD_W;

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
	public void blit(Sprite s, int x, int y) throws Exception {
//		Prepare variables
		int lcdLeft, lcdTop, lcdRight, lcdBottom, sLeft, sTop, sRight, sBottom, lcdx, lcdy, sx, sy;
		lcdLeft = Math.min(0, x - 8);
		lcdRight = Math.max(x, GB_LCD_W);
		lcdTop = Math.min(0, y - 16);
		lcdBottom = Math.max(y - 16 + s.h, GB_LCD_H);
		
		// Exit if sprite outside screen
		if ( lcdLeft >= lcdRight || lcdTop >= lcdBottom ) {
			return;
		}
		
		sLeft = Math.max(0, -(x-8));
		sRight = Math.min(8, GB_LCD_W - (x-8));
		sTop = Math.max(0, -(y-16));
		sBottom = Math.min(s.h, GB_LCD_H - (y-16)); 
		
		// Exit if sprite outside screen
		if ( sLeft >= sRight || sTop >= sBottom ) {
			return;
		}
		
//			Safety guards for debug purposes...
		if ( lcdRight - lcdLeft != sRight - sLeft ) {
			throw new Exception("blitSprite: Blitted widths on screen and of sprite does not match.");
		}
		if ( lcdBottom - lcdTop != sBottom - sTop ) {
			throw new Exception("blitSprite: Blitted heights on screen and of sprite does not match.");
		}
		
//		Blit sprite to screen
		sy = sTop;
		// For each line...
		for ( lcdy = lcdTop; lcdy < lcdBottom; lcdy++ ) {
			sx = sLeft;
			// For each pixel...
			for ( lcdx = lcdLeft; lcdx < lcdRight; lcdx++ ) {
				bits[lcdy][lcdx] = s.bits[sy][sx];
			}
		}
	}
	
	/**
	 * Blits a tile onto the screen
	 * 
	 * @param t Tile to be drawn
	 * @param x X-position to draw tile at
	 * @param y Y-position to draw tile at
	 * @throws Exception
	 */
	public void blit(Tile t, int x, int y) {
//		Prepare variables
		int lcdLeft, lcdTop, lcdRight, lcdBottom, tLeft, tTop, tRight, tBottom, lcdx, lcdy, tx, ty;
		lcdLeft = Math.max(0, x);
		lcdRight = Math.min(x + 8, GB_LCD_W);
		lcdTop = Math.max(0, y);
		lcdBottom = Math.min(y + 8, GB_LCD_H);
		
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
		/*
//			Safety guards for debug purposes...
		if ( lcdRight - lcdLeft != tRight - tLeft ) {
			throw new Exception("blitSprite: Blitted widths on screen and of tile does not match.");
		}
		if ( lcdBottom - lcdTop != tBottom - tTop ) {
			throw new Exception("blitSprite: Blitted heights on screen and of tile does not match.");
		}
		*/
		
//		Blit sprite to screen
		ty = tTop;
		// For each line...
		for ( lcdy = lcdTop; lcdy < lcdBottom; lcdy++ ) {
			tx = tLeft;
			// For each pixel...
			for ( lcdx = lcdLeft; lcdx < lcdRight; lcdx++ ) {
				bits[lcdy][lcdx] = t.bits[ty][tx];
				tx++;
			}
			ty++;
		}
	}
	
	public int[][] getBits() {
		return bits;
	}
}