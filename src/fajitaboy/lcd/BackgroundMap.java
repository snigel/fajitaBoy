package fajitaboy.lcd;

import static fajitaboy.constants.AddressConstants.*;
import static fajitaboy.constants.LCDConstants.*;
import fajitaboy.memory.AddressBus;
import fajitaboy.memory.MemoryInterface;
import fajitaboy.memory.Vram;

public class BackgroundMap {
	public static enum MapType {BACKGROUND, WINDOW}
	private int[][] data = new int[32][32];
	MapType type;
	
    public BackgroundMap(MapType tp) {
        type = tp;
    }
    
	public void readBackground(MemoryInterface ram, LCDC lcdc) {
		//read tile numbers
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
		
		for (int i = 0; i < GB_MAP_H; i++) {
			for (int j = 0; j < GB_MAP_W; j++) {
				if ( lcdc.tileDataSelect ) {
					data[i][j] = ram.read(addr_base);
				} else {
					data[i][j] = (0x100 + (byte)ram.read(addr_base));
				}
				addr_base++;
			}
		}
		
		/*
		 * An area of VRAM known as Background Tile Map contains the numbers of tiles to be displayed. 
		 * It is organized as 32 rows of 32 bytes each. Each byte contains a number of a tile to be displayed. 
		 * Tile patterns are taken from the Tile Data Table located either at $8000-8FFF or $8800-97FF. 
		 * In the first case, patterns are numbered with unsigned numbers from 0 to 255 (i.e. pattern #0 lies at address $8000). 
		 * In the second case, patterns have signed numbers from -128 to 127 (i.e. pattern #0 lies at address $9000). 
		 * The Tile Data Table address for the background can be selected via LCDC register.
		 */
	}
	
	public void draw(Screen screen, MemoryInterface ram, Vram vram) {
// 		Prepare variables
		int scx, scy, firstTileX, firstTileY;
		Tile[] tiles = vram.getTiles();
		
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
		int dx, dy, datax, datay, tileId;
		// For each row...
		for ( int y = 0; y < GB_LCD_H/8; y += 1 ) {
			dy = (firstTileY+y)*8 - scy;
			datay = (firstTileY+y) % 32;
			// For each column...
			for ( int x = 0; x < GB_LCD_W/8; x += 1 ) {
				dx = (firstTileX+x)*8 - scx;
				datax = (firstTileX+x) % 32;
				tileId = data[datay][datax];
                screen.blit(tiles[tileId], dx, dy, 0);
			}
		}
	}
}