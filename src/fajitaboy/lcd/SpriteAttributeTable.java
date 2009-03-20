package fajitaboy.lcd;

import static fajitaboy.constants.LCDConstants.*;
import fajitaboy.MemoryInterface;

public class SpriteAttributeTable {
	
	Sprite[] sprites = new Sprite[GB_SPRITES];
	
	public SpriteAttributeTable() {
		// Init Sprite array
		for ( int i = 0; i < GB_SPRITES; i++ ) {
			sprites[i] = new Sprite();
		}
	}
	
	public void readSprites(MemoryInterface ram) {
		// Read sprites
		int addr = 0x8000;
		for ( int i = 0; i < GB_SPRITES; i++ ) {
			sprites[i].readSprite(ram, addr);
			addr += 16;
		}
	}
	
	public void draw(Screen screen, MemoryInterface ram, LCDC lcdc) {
// 		Draw sprites
		int sprNo = 0;
		int x, y, id, idHi, idLo, attr, addr;
		while ( sprNo < 40 ) {
			addr = 0xFE00 + 4*sprNo;
			
			// Read sprite data
			x =    ram.read(addr);
			y =    ram.read(addr+1);
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
		}
	}
}
