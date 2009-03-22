package fajitaboy.lcd;

import static fajitaboy.constants.AddressConstants.*;
import static fajitaboy.constants.LCDConstants.*;
import fajitaboy.AddressBus;
import fajitaboy.MemoryInterface;

public class BackgroundMap {
	public static enum MapType {BACKGROUND, WINDOW}

	private Tile[][] data = new Tile[32][32];
	MapType type;
	
    public BackgroundMap(MapType tp) {
        type = tp;
    }
    
	public void readBackground(MemoryInterface ram, LCDC lcdc) {
		//read tile numbers
		int tileNumbers[] = new int[GB_MAP_W * GB_MAP_H];
		int addr_base = 0;
        
		if (type == MapType.BACKGROUND) {
			if (lcdc.bgTileMapSelect) {
				addr_base = 0x9C00;
			} else {
				addr_base = 0x9800;
			}
		} else if (type == MapType.WINDOW) {
			if (lcdc.windowTileMapSelect) {
				addr_base = 0x9C00;
			} else {
				addr_base = 0x9800;
			}
		}
		
		for (int i = 0; i < GB_MAP_W * GB_MAP_H; i++) {
			tileNumbers[i] = ram.read(addr_base + i);
		}
		
		/*
		 * 
An area of VRAM known as Background Tile Map contains the numbers of tiles to be displayed. 
It is organized as 32 rows of 32 bytes each. Each byte contains a number of a tile to be displayed. 
Tile patterns are taken from the Tile Data Table located either at $8000-8FFF or $8800-97FF. 
In the first case, patterns are numbered with unsigned numbers from 0 to 255 (i.e. pattern #0 lies at address $8000). 
In the second case, patterns have signed numbers from -128 to 127 (i.e. pattern #0 lies at address $9000). 
The Tile Data Table address for the background can be selected via LCDC register.
		 */
		//read tile patterns
		
		for (int i = 0; i < GB_MAP_H; i++) {
			for (int j = 0; j < GB_MAP_W; j++) {
				Tile t = new Tile();
				int addr;
				
				if (lcdc.tileDataSelect) {
					addr = 0x8000 + tileNumbers[i*GB_MAP_H + j]*(GB_TILE_W + GB_TILE_H);
				} else {
                    // antagligen rätt
					addr = 0x8800 + ((byte) tileNumbers[i*GB_MAP_H + j] )*(GB_TILE_W + GB_TILE_H); 
				}
				
				t.readTile(ram, addr);
				data[i][j] = t;
			}
		}
	}
	
	public void draw(Screen screen, MemoryInterface ram) {
// 		Prepare variables
		int scx, scy, firstTileX, firstTileY;
        if (type == MapType.BACKGROUND) {
            scx = ram.read(ADDRESS_SCX);
            scy = ram.read(ADDRESS_SCY);
        } else { //WINDOW 
            scx = ram.read(ADDRESS_WX);
            scy = ram.read(ADDRESS_WY);
            if (scx < 0 || scx > 166 || scy < 0 || scy > 143) {
                return;
            }
            scy -= 7;
        }
        
		firstTileX = (int)(scx/8);
		firstTileY = (int)(scy/8);
		
//		Draw tiles
		int dx, dy, datax, datay;
		// For each row...
		for ( int y = 0; y < GB_LCD_H/8; y += 1 ) {
			dy = (firstTileY+y)*8 - scy;
			datay = (firstTileY+y) % 32;
			// For each column...
			for ( int x = 0; x < GB_LCD_W/8; x += 1 ) {
				dx = (firstTileX+x)*8 - scx;
				datax = (firstTileX+x) % 32;
				Tile t = data[datay][datax];
                screen.blit(t, dx, dy, 0);
			}
		}
	}
}