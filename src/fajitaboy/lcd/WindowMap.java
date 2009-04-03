package fajitaboy.lcd;

import static fajitaboy.constants.AddressConstants.*;
import static fajitaboy.constants.LCDConstants.*;
import fajitaboy.memory.MemoryInterface;
import fajitaboy.memory.Vram;

public class WindowMap {
	/*
	 * Contains the id of each tile to be displayed.
	 */
	private int[][] tileAddresses;

	public WindowMap() {
		reset();
	}

	public void reset() {
		tileAddresses = new int[32][32];
	}

	public void readWindow(MemoryInterface ram, LCDC lcdc) {
		// find base address
		int addr_base;
		if (lcdc.windowTileMapSelect) {
			addr_base = 0x9C00;
		} else {
			addr_base = 0x9800;
		}

		// read tile numbers
		for (int i = 0; i < GB_MAP_H; i++) {
			for (int j = 0; j < GB_MAP_W; j++, addr_base++) {
				int pnr = ram.read(addr_base);
				if ( lcdc.tileDataSelect ) {
					tileAddresses[i][j] = pnr;
				} else {
					tileAddresses[i][j] = 0x100 + (byte)pnr; 
				}
			}
		}
	}
	
	public void readWindowLine(int ly, MemoryInterface ram, LCDC lcdc) {
		int scx, scy;

		scx = ram.read(ADDRESS_WX);
		scy = ram.read(ADDRESS_WY);
		
		// The window becomes visible (if enabled) when positions are set in range WX=0..166, WY=0..143. 
		// A postion of WX=7, WY=0 locates the window at upper left, it is then completely covering normal background.
		if (scx < 0 || scx > 166 || scy < 0 || scy > 143 || scy > ly) {
			return;
		}
		scx -= 7;
		
		// vi borde inte behöva köra modulus här
		int firstTileY = ((scy + ly) / 8);
		int firstTileX = (scx / 8);
		
		if (firstTileY < 0 || firstTileY >= 32 || firstTileX < 0 || firstTileX >= 32) {
			System.out.println("not supposed to happen");
		}
		
		int addr_base = 0;
		if (lcdc.windowTileMapSelect) {
			addr_base = 0x9C00;
		} else {
			addr_base = 0x9800;
		}
		
		// read tile-numbers
		for (int cx = 0; cx < GB_MAP_VISIBLE_W + 1; cx++) {
			int ty = (firstTileY + (firstTileX + cx)/GB_MAP_H) % GB_MAP_H; 
			int tx = (firstTileX + cx) % GB_MAP_W;
			
			// addr, where we read the tile pattern nr.
			int addr = addr_base + ty * GB_MAP_W + tx; 
			
			int pnr = ram.read(addr);
			if ( lcdc.tileDataSelect ) {
				tileAddresses[ty][tx] = pnr;
			} else {
				// signed
				tileAddresses[ty][tx] = 0x100 + (byte)pnr; 
			} 
		}
	}
	
	public void drawLine(Screen screen, MemoryInterface ram, Vram vram, int ly) {
// 		Prepare variables
		int scx, scy;
		Tile[] tiles = vram.getTiles();

		scx = ram.read(ADDRESS_WX);
		scy = ram.read(ADDRESS_WY);
		// The window becomes visible (if enabled) when positions are set in range WX=0..166, WY=0..143. 
		// A postion of WX=7, WY=0 locates the window at upper left, it is then completely covering normal background.
		if (scx < 0 || scx > 166 || scy < 0 || scy > 143 || scy > ly) {
			return;
		}
		scx -= 7;

		//	Draw tiles
		int datax, datay = 0, tileId;
		// For each row...
		for ( int y = scy; y <= GB_LCD_H; y += 8 ) {
			datax = 0;
			// For each column...
			for ( int x = scx; x <= GB_LCD_W; x += 8 ) {
				tileId = tileAddresses[datay][datax];
				screen.blitTile(tiles[tileId], ram.read(PALETTE_BG_DATA), x, y, ly, false);
				datax++;
			}
			datay++;
		}
	}

	public void draw(Screen screen, MemoryInterface ram, Vram vram, int ly) {
		// 		Prepare variables
		int scx, scy;
		Tile[] tiles = vram.getTiles();

		scx = ram.read(ADDRESS_WX);
		scy = ram.read(ADDRESS_WY);
		// The window becomes visible (if enabled) when positions are set in range WX=0..166, WY=0..143. 
		// A postion of WX=7, WY=0 locates the window at upper left, it is then completely covering normal background.
		if (scx < 0 || scx > 166 || scy < 0 || scy > 143 || scy > ly) {
			return;
		}
		scx -= 7;

		//	Draw tiles
		int datax, datay = 0, tileId;
		// For each row...
		for ( int y = scy; y <= GB_LCD_H; y += 8 ) {
			datax = 0;
			// For each column...
			for ( int x = scx; x <= GB_LCD_W; x += 8 ) {
				tileId = tileAddresses[datay][datax];
				screen.blitTile(tiles[tileId], ram.read(PALETTE_BG_DATA), x, y, ly, false);
				datax++;
			}
			datay++;
		}
	}
}
