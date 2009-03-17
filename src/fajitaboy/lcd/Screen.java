package fajitaboy.lcd;

import static fajitaboy.constants.LCDConstants.GB_LCD_H;
import static fajitaboy.constants.LCDConstants.GB_LCD_W;

public class Screen {
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
//		Prepare variables
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
				bits[lcdx][lcdy] = s.bits[sx][sy];
			}
		}
	}
	
	public void blit(Tile t, int x, int y) throws Exception {
//		Prepare variables
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
		
//			Safety guards for debug purposes...
		if ( lcdRight - lcdLeft != tRight - tLeft ) {
			throw new Exception("blitSprite: Blitted widths on screen and of tile does not match.");
		}
		if ( lcdBottom - lcdTop != tBottom - tTop ) {
			throw new Exception("blitSprite: Blitted heights on screen and of tile does not match.");
		}
		
//		Blit sprite to screen
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