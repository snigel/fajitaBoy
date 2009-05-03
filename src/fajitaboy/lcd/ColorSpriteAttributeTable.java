package fajitaboy.lcd;

import static fajitaboy.constants.LCDConstants.GB_SPRITE_ATTRIBUTES;
import static fajitaboy.constants.LCDConstants.GB_TILES;

import java.util.PriorityQueue;
import java.util.Queue;

import fajitaboy.memory.MemoryInterface;
import fajitaboy.memory.Vram;
import fajitaboy.memory.VramCgb;

public class ColorSpriteAttributeTable {
    
    /**
     * Collection of sprites to be rendered behind background.
     */
    Queue<ColorSpriteAttribute> behindBG = new PriorityQueue<ColorSpriteAttribute>();
    
    /**
     * Collection of sprites to be rendered above background.
     */
    Queue<ColorSpriteAttribute> aboveBG = new PriorityQueue<ColorSpriteAttribute>();
    
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
            ColorSpriteAttribute sa =  new ColorSpriteAttribute();
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
    public void draw(ColorScreen screen, boolean drawBehindBG, MemoryInterface ram, LCDC lcdc, VramCgb vram, int ly) {
        Queue<ColorSpriteAttribute> toDraw;
        if (drawBehindBG) {
            toDraw = behindBG;
        } else {
            toDraw = aboveBG;
        }
        
        Tile[] tiles = vram.getTiles();
        
        for (ColorSpriteAttribute sa : toDraw) {
            int id = sa.patternNr + sa.vramBank * 2 * GB_TILES;
            
            int palette = sa.paletteNumber;
            
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
