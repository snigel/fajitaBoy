package fajitaboy.gbc.lcd;

import static fajitaboy.constants.LCDConstants.LCD_H;
import static fajitaboy.constants.LCDConstants.LCD_W;
import fajitaboy.gb.lcd.Screen;
import fajitaboy.gb.lcd.Tile;
import fajitaboy.gb.memory.PaletteMemory;

public class CGB_Screen extends Screen {

	/**
	 * Keeps information about the sprite bit data even if something is draw above.
	 * This is useful when window overlaps  
	 */
	private int[][] spriteBits;
	
	/**
	 * To know if a window tile has been draw at bit.
	 */
	private boolean[][] windowBits;
    
    /**
     * Pointer to the Background Palette Memory.
     */
    private PaletteMemory backgroundPaletteMemory;
    
    /**
     * Pointer to the Sprite Palette Memory.
     */
    private PaletteMemory spritePaletteMemory;
    
    /**
     * Creates a new ColorScreen.
     * @param bpm Reference to Background Palette Memory.
     * @param spm Reference to Sprite Palette Memory.
     */
    public CGB_Screen(PaletteMemory bpm, PaletteMemory spm) {
        backgroundPaletteMemory = bpm;
        spritePaletteMemory = spm;
        spriteBits = new int[LCD_H][LCD_W];
        windowBits = new boolean[LCD_H][LCD_W];
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
                	spriteBits[sy][sx] = bits[sy][sx] = spritePaletteMemory.getPalette(palette)[newidx];
                }
                sx++;
                tx--;
            }
        } else {
            while( sx < 160 && tx < 8 ) {
                int newidx = t.bits[ty][tx];
                if (newidx != 0) {
                	spriteBits[sy][sx] = bits[sy][sx] = spritePaletteMemory.getPalette(palette)[newidx];
                }
                sx++;
                tx++;
            }
        }
    }
    
    /**
     * Blits a tile onto the screen
     * 
     * @param t Tile to be drawn
     * @param x X-position to draw tile at
     * @param y Y-position to draw tile at
     * @param ly Line to blit to on screen
     * @param xFlip Enable horisontal sprite flip
     * @param yFlip Enable vertical sprite flip
     * @param windowTile If this is a window tile or not. Window tiles will
     * 				     never be overwritten by other tiles. This is to 
     * 					 make sure that background tiles never overlaps 
     * 					 window tiles. 
     */
    public void blitTile(Tile t, int palette, int x, int y, int ly, boolean xFlip, boolean yFlip, boolean windowTile) {
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
        
        if ( yFlip ) {
            ty = 7 - ty;
        }

        // Blit pixels to screen            
        if(xFlip) {
        	tx = 7 - tx;
        
            while( sx < 160 && tx >= 0 ) {
                int newidx = t.bits[ty][tx];
                if (!windowBits[sy][sx]) {
                	
	                if (!windowTile && newidx == 0 && spriteBits[sy][sx] != 0) {
	            		bits[sy][sx] = spriteBits[sy][sx];
	            	} else {
	            		bits[sy][sx] = backgroundPaletteMemory.getPalette(palette)[newidx];
	            	}
	                
	                if (windowTile) {
	                	windowBits[sy][sx] = true;
	                }
                }
                sx++;
                tx--;
            }
	    
        } else {
            while( sx < 160 && tx < 8 ) {
                int newidx = t.bits[ty][tx];
                if (!windowBits[sy][sx]) {
                	
	                if (!windowTile && newidx == 0 && spriteBits[sy][sx] != 0) {
	            		bits[sy][sx] = spriteBits[sy][sx];
	            	} else {
	            		bits[sy][sx] = backgroundPaletteMemory.getPalette(palette)[newidx];
	            	}
	                
	                if (windowTile) {
	                	windowBits[sy][sx] = true;
	                }
                }
                sx++;
                tx++;
            }
	        
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
			spriteBits[ly][x] = 0;
			windowBits[ly][x] = false;
		}
	}

}
