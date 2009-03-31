package fajitaboy.lcd;

import static fajitaboy.constants.AddressConstants.*;
import static fajitaboy.constants.LCDConstants.*;
import fajitaboy.memory.AddressBus;
import fajitaboy.memory.MemoryInterface;
import fajitaboy.memory.Vram;

public class BackgroundMap {
	public static enum MapType {BACKGROUND, WINDOW}
	
	/*
	 * Contains the id of each tile to be displayed.
	 */
	private int[][] tileAddresses = new int[32][32];
	MapType type;
	
    public BackgroundMap(MapType tp) {
        type = tp;
    }
    
	public void readBackground(MemoryInterface ram, LCDC lcdc) {
		// find base address
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
		
		// read tile numbers
		for (int i = 0; i < GB_MAP_H; i++) {
			for (int j = 0; j < GB_MAP_W; j++, addr_base++) {
				int pnr = ram.read(addr_base);
				if ( lcdc.tileDataSelect ) {
					tileAddresses[i][j] = pnr;
				} else {
					/*
					 * i think the problem is here. 
					 * pnr == 0 should translate to 0x9000 
					 * 0x100 + (byte)pnr = 0x100. it will select tile[0x100] aka tile[256]. This will
					 * correspond to memory 0x8000 + 256*16 = 0x8000 + 0x100*0x10 = 0x9000
					 */
					tileAddresses[i][j] = 0x100 + (byte)pnr; 
				}
				
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
        	/*
        	 * 
The window becomes visible (if enabled) when positions are set in range WX=0..166, WY=0..143. 
A postion of WX=7, WY=0 locates the window at upper left, it is then completly covering normal background.
        	 */
            scx = ram.read(ADDRESS_WX);
            scy = ram.read(ADDRESS_WY);
            if (scx < 0 || scx > 166 || scy < 0 || scy > 143) {
                return;
            }
            scx -= 7;
        }
        
		firstTileX = (int)(scx/8);
		firstTileY = (int)(scy/8);
		
//		Draw tiles
		int dx, dy, datax, datay, tileId;
		// For each row...
		
		for ( int y = 0; y < GB_LCD_H/8 + 1; y++ ) {
			dy = (firstTileY+y)*8 - scy;
			datay = (firstTileY+y) % 32;
			// For each column...

			// we probably want to write one more tile to the right, but i want
			// to confirm it's a bug. (can be confirmed in alien3)
			for ( int x = 0; x < GB_LCD_W/8 + 1; x++ ) {
				dx = (firstTileX+x)*8 - scx;
				datax = (firstTileX+x) % 32;
				tileId = tileAddresses[datay][datax];
                screen.blit(tiles[tileId], ram.read(PALETTE_BG_DATA), dx, dy); 
			}
		}
	}
}