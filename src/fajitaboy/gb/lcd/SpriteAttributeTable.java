package fajitaboy.gb.lcd;

import fajitaboy.gb.memory.MemoryInterface;
import fajitaboy.gb.memory.Oam;
import fajitaboy.gb.memory.Vram;

public class SpriteAttributeTable {
    
	Vram vram;
	MemoryInterface ram;
	LCDC lcdc;
	Screen screen;
	Oam oam;
	
	public SpriteAttributeTable(MemoryInterface ram, LCDC lcdc, Vram vram, Screen screen, Oam oam) {
		this.ram = ram;
		this.lcdc = lcdc;
		this.vram = vram;
		this.screen = screen;
		this.oam = oam;
	}
	
	/**
	 * Draws sprites onto screen.
	 * @param drawBehindBG Whether to draw the sprites that are above or behind the background
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
        SpriteAttribute sa;
        for ( int i = 0; i < sprAttr.length; i++ ) {
        	sa = (SpriteAttribute)sprAttr[i];
            int id = sa.patternNr;
            int palette = ram.read(sa.paletteAddr);
            
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
}
