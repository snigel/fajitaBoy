package fajitaboy.gbc.lcd;

import fajitaboy.gb.lcd.Screen;
import fajitaboy.gb.lcd.Tile;
import fajitaboy.gb.memory.PaletteMemory;

public class ColorScreen extends Screen {
    
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
    public ColorScreen(PaletteMemory bpm, PaletteMemory spm) {
        backgroundPaletteMemory = bpm;
        spritePaletteMemory = spm;
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
                    bits[sy][sx] = spritePaletteMemory.getPalette(palette)[newidx];//0x03 & palette >> newidx*2;
                }
                sx++;
                tx--;
            }
        } else {
            while( sx < 160 && tx < 8 ) {
                int newidx = t.bits[ty][tx];
                if (newidx != 0) {
                    bits[sy][sx] = spritePaletteMemory.getPalette(palette)[newidx];
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
                    bits[sy][sx] = backgroundPaletteMemory.getPalette(palette)[newidx];
                    //System.out.println(sy+" "+sx+" "+newidx+" "+palette+" "+backgroundPaletteMemory.getPalette(palette)[newidx]);
                }
                sx++;
                tx++;
            }
        } else {
            while( sx < 160 && tx < 8 ) {
                int newidx = t.bits[ty][tx];
                bits[sy][sx] = backgroundPaletteMemory.getPalette(palette)[newidx];
                //System.out.println(sy+" "+sx+" "+newidx+" "+palette+" "+backgroundPaletteMemory.getPalette(palette)[newidx]);
                sx++;
                tx++;
            }   
        }
        //System.out.println();
    }

}