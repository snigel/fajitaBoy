package fajitaboy.lcd;

import static fajitaboy.constants.LCDConstants.*;

import java.util.PriorityQueue;
import java.util.Queue;

import fajitaboy.MemoryInterface;

public class SpriteAttributeTable {
	
	Sprite[] sprites = new Sprite[GB_SPRITES];
	//SpriteAttribute[] spriteAttributes = new SpriteAttribute[GB_SPRITES];
    
    Queue<SpriteAttribute> behindBG = new PriorityQueue<SpriteAttribute>();
    Queue<SpriteAttribute> aboveBG = new PriorityQueue<SpriteAttribute>();
    
	public SpriteAttributeTable() {
		// Init Sprite array
		/*
        for ( int i = 0; i < GB_SPRITES; i++ ) {
			sprites[i] = new Sprite();
		}
        */
	}
	
	public void readSprites(MemoryInterface ram) {
		// Read sprites
		
        int spriteDataAddr = 0x8000;
        for (int i = 0; i < GB_SPRITES; i++) {
            Sprite s = new Sprite();
            s.readSprite(ram, spriteDataAddr + 16*i);
            sprites[i] = s;
        }

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
	
	public void draw(Screen screen, boolean drawBehindBG, MemoryInterface ram, LCDC lcdc) {
	    Queue<SpriteAttribute> toDraw;
        if (drawBehindBG) {
            toDraw = behindBG;
        } else {
            toDraw = aboveBG;
        }
        
        for (SpriteAttribute sa : toDraw) {
            int id = sa.patternNr;
            if ( lcdc.objSpriteSize ) {
                // 8x16 sprite
                int idLo = id & 0xFE;
                int idHi = id | 0x01;
                screen.blit(sprites[idLo], sa.x, sa.y);
                screen.blit(sprites[idHi], sa.x, sa.y + 8);
            } else {
                // 8x8 sprite
                screen.blit(sprites[id], sa.x, sa.y);
            }
        }
        
        /*
        // 		Draw sprites
		int sprNo = 0;
		int x, y, id, idHi, idLo, attr, addr;
		while ( sprNo < 40 ) {
			addr = 0xFE00 + 4*sprNo;
			
			// Read sprite data
			y =    ram.read(addr);
			x =    ram.read(addr+1);
			id =   ram.read(addr+2);
			attr = ram.read(addr+3);
			
			// Draw sprite
			if ( lcdc.objSpriteSize ) {
				// 8x16 sprite
				idLo = id & 0xFE;
				idHi = id | 0x01;
				screen.blit(sprites[idLo], x, y);
				screen.blit(sprites[idHi], x, y + 8);
			} else {
				// 8x8 sprite
				screen.blit(sprites[id], x, y);
			}
			
			sprNo++;
            
		}*/
	}
}
