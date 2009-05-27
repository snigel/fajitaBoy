package fajitaboy.gbc.lcd;

import static fajitaboy.constants.HardwareConstants.*;

import fajitaboy.gb.lcd.LCDC;
import fajitaboy.gb.lcd.Tile;
import fajitaboy.gb.memory.MemoryInterface;
import fajitaboy.gbc.memory.CGB_Oam;
import fajitaboy.gbc.memory.CGB_Vram;

public class CGB_SpriteAttributeTable {
    
	CGB_Vram vram;
	MemoryInterface ram;
	LCDC lcdc;
	CGB_Screen screen;
	CGB_Oam oam;
    
	public CGB_SpriteAttributeTable(MemoryInterface ram, LCDC lcdc, CGB_Vram vram, CGB_Screen screen, CGB_Oam oam) {
		this.ram = ram;
		this.lcdc = lcdc;
		this.vram = vram;
		this.screen = screen;
		this.oam = oam;
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
	public void draw(boolean drawBehindBG, int ly) {
		// Refresh draw order arrays in OAM
		oam.updateDrawOrder();
		
		// Retrieve correct draw order array
	    Object[] sprAttr;
        if (drawBehindBG) {
            sprAttr = oam.behindBGArray;
        } else {
        	sprAttr = oam.aboveBGArray;
        }
        
        Tile[] tiles = vram.getTiles();

        // Iterate through draw order array
        CGB_SpriteAttribute sa;
        for ( int i = 0; i < sprAttr.length; i++ ) {
        	sa = (CGB_SpriteAttribute)sprAttr[i];
        	int id = sa.patternNr + sa.vramBank * 2 * GB_TILES;
            int palette = sa.paletteNumber;
            
            if ( lcdc.objSpriteSize ) {
                // 8x16 sprite
                int idLo = id & 0xFFFE;
                int idHi = id | 0x0001;
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
	/*
    public void draw(boolean drawBehindBG, int ly) {
        Queue<CGB_SpriteAttribute> toDraw;
        if (drawBehindBG) {
            toDraw = behindBG;
        } else {
            toDraw = aboveBG;
        }
        
        Tile[] tiles = vram.getTiles();
        
        for (CGB_SpriteAttribute sa : toDraw) {
            int id = sa.patternNr + sa.vramBank * 2 * GB_TILES;
            
            int palette = sa.paletteNumber;
            
            if ( lcdc.objSpriteSize ) {
                // 8x16 sprite
                int idLo = id & 0xFFFE;
                int idHi = id | 0x0001;
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
    */

}
