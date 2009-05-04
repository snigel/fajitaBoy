package fajitaboy.gb.lcd;

import static fajitaboy.constants.LCDConstants.*;
import static fajitaboy.constants.HardwareConstants.*;

import java.util.PriorityQueue;
import java.util.Queue;

import fajitaboy.gb.memory.MemoryInterface;
import fajitaboy.gb.memory.Vram;

public class SpriteAttributeTable {
    
	/**
	 * Collection of sprites to be rendered behind background.
	 */
    Queue<SpriteAttribute> behindBG = new PriorityQueue<SpriteAttribute>();
    
    /**
     * Collection of sprites to be rendered above background.
     */
    Queue<SpriteAttribute> aboveBG = new PriorityQueue<SpriteAttribute>();
    
    /**
     * Default constructor.
     */
	public SpriteAttributeTable() {
	}
	
	/**
	 * Reads the sprite attribute table from memory.
	 * @param ram Pointer to memory
	 */
	public void readSpriteAttributes(MemoryInterface ram) {
		// Read sprite attribute table
        int spriteAttrAddr = 0xFE00;
        behindBG.clear();
        aboveBG.clear();
		for ( int i = 0; i < GB_SPRITE_ATTRIBUTES; i++ ) {
            int saa = spriteAttrAddr + i*4;
            SpriteAttribute sa =  new SpriteAttribute();
            sa.read(ram, saa);

            if (sa.behindBG) {
                behindBG.add(sa);
            } else {
                aboveBG.add(sa);
            }
            
		}
	}
	
	/**
	 * Draws sprites onto screen.
	 * @param screen Pointer to screen
	 * @param drawBehindBG Whether to draw the sprites that are above or behind the background
	 * @param ram Pointer to memory
	 * @param lcdc Pointer to LCDC information
	 * @param vram Pointer to VRAM
	 * @param ly Screen line to blit at
	 */
	public void draw(Screen screen, boolean drawBehindBG, MemoryInterface ram, LCDC lcdc, Vram vram, int ly) {
	    Queue<SpriteAttribute> toDraw;
        if (drawBehindBG) {
            toDraw = behindBG;
        } else {
            toDraw = aboveBG;
        }
        
        Tile[] tiles = vram.getTiles();
        
        for (SpriteAttribute sa : toDraw) {
            int id = sa.patternNr;
            int palette = ram.read(sa.paletteAddr);
            
            if ( lcdc.objSpriteSize ) {
                // 8x16 sprite
                int idLo = id & 0xFE;
                int idHi = id | 0x01;
                if ( sa.flipY ) {
                	screen.blitSprite(tiles[idLo], palette, sa.x - 8, sa.y - 8, ly, sa.flipX, sa.flipY);
                	screen.blitSprite(tiles[idHi], palette, sa.x - 8, sa.y - 16, ly, sa.flipX, sa.flipY);
                } else {
                	screen.blitSprite(tiles[idLo], palette, sa.x - 8, sa.y - 16, ly, sa.flipX, sa.flipY);
                	screen.blitSprite(tiles[idHi], palette, sa.x - 8, sa.y - 8, ly, sa.flipX, sa.flipY);
                }
            } else {
                // 8x8 sprite
                screen.blitSprite(tiles[id], palette, sa.x - 8, sa.y - 16, ly, sa.flipX, sa.flipY);
            }
        }
	}
}
