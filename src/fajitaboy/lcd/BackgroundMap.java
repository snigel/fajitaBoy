package fajitaboy.lcd;

import static fajitaboy.constants.AddressConstants.*;
import static fajitaboy.constants.LCDConstants.*;
import fajitaboy.memory.MemoryInterface;
import fajitaboy.memory.Vram;

public class BackgroundMap {
	public static enum MapType {BACKGROUND, WINDOW}

	/*
	 * Contains the id of each tile to be displayed.
	 */
	private int[][] tileAddresses = new int[32][32];
	MapType type;

	public BackgroundMap() {
		reset();
	}

	public void reset() {
		tileAddresses = new int[32][32];
	}

	public void readBackground(MemoryInterface ram, LCDC lcdc) {
		// find base address
		int addr_base = 0;
		if (lcdc.bgTileMapSelect) {
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
	}

	public void draw(Screen screen, MemoryInterface ram, Vram vram, int ly) {
		// 		Prepare variables
		int scx, scy, firstTileX, firstTileY;
		Tile[] tiles = vram.getTiles();

		scx = ram.read(ADDRESS_SCX);
		scy = ram.read(ADDRESS_SCY);

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
				screen.blitTile(tiles[tileId], ram.read(PALETTE_BG_DATA), dx, dy, ly, true);
			}
		}
	}
}