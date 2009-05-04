package fajitaboy.gb.lcd;

import static fajitaboy.constants.AddressConstants.*;
import static fajitaboy.constants.LCDConstants.*;
import static fajitaboy.constants.HardwareConstants.*;
import fajitaboy.gb.memory.MemoryInterface;
import fajitaboy.gb.memory.Vram;

public class BackgroundMap {
	/**
	 * Contains the id of each tile to be displayed.
	 */
	private int[][] tileAddresses = new int[32][32];

	/**
	 * Default constructor.
	 */
	public BackgroundMap() {
		reset();
	}

	/**
	 * Resets the BackgroundMap with empty data.
	 */
	public void reset() {
		tileAddresses = new int[32][32];
	}
	
	/**
	 * Reads the BackgroundMap from memory. Only reads the line that is currently being rendered.
	 * @param ly Screen line that is currently being rendered
	 * @param ram Pointer to memory interface
	 * @param lcdc Pointer to LCDC information
	 */
	public void readBackgroundWholeLine(int ly, MemoryInterface ram, LCDC lcdc) {
		int scy, firstTileY;
		
		scy = ram.read(ADDRESS_SCY);

		firstTileY = ((scy + ly) / 8) % 32;
		
		int addr_base = 0;
		if (lcdc.bgTileMapSelect) {
			addr_base = 0x9C00;
		} else {
			addr_base = 0x9800;
		}
		
		for (int cx = 0; cx < LCD_MAP_W; cx++) {
			/*
			 * ty & tx, the index in the background array.
			 * ty should be firstTileY or firstTileY + 1
			 */
			int ty = firstTileY; // (firstTileY + (firstTileX + cx)/GB_MAP_H) % GB_MAP_H; 

			// addr, where we read the tile pattern nr.
			int addr = addr_base + ty * LCD_MAP_W + cx;

			int pnr = ram.read(addr);
			if ( lcdc.tileDataSelect ) {
				tileAddresses[ty][cx] = pnr;
			} else {
				// signed
				tileAddresses[ty][cx] = 0x100 + (byte)pnr; 
			} 
		}
	}
	
	/**
	 * Reads the BackgroundMap from memory. Only reads the line that is currently being rendered.
	 * Is optimized compared to readBackgroundWholeLine.
	 * @param ly Screen line that is currently being rendered
	 * @param ram Pointer to memory interface
	 * @param lcdc Pointer to LCDC information
	 */
	public void readBackgroundLine(int ly, MemoryInterface ram, LCDC lcdc) {
		int scx, scy, firstTileX, firstTileY;
		
		scx = ram.read(ADDRESS_SCX);
		scy = ram.read(ADDRESS_SCY);

		firstTileX = scx/8;
		
		/*
		 * krånglar när scy == 255 och mindre
		 */
		firstTileY = ((scy + ly) / 8) % 32;
		
		/*
		if (ly == 0) {
			System.out.printf("%03d, %03d\n", firstTileX, firstTileY);
		}
		*/
		
		int addr_base = 0;
		if (lcdc.bgTileMapSelect) {
			addr_base = 0x9C00;
		} else {
			addr_base = 0x9800;
		}
		// addr_base += firstTileY * GB_MAP_H + firstTileX;
		
		/*
		 * We have to read (GB_MAP_VISIBLE_W + 1) nr of tiles,
		 * since we could need an extra partial tile. we could
		 * figure out when we dont need that extra tile and save
		 * some cycles.
		 */
		for (int cx = 0; cx < LCD_MAP_VISIBLE_W + 1; cx++) {
			/*
			 * ty & tx, the index in the background array.
			 * ty should be firstTileY or firstTileY + 1
			 */
			int ty = (firstTileY + (firstTileX + cx)/LCD_MAP_H) % LCD_MAP_H; 
			int tx = (firstTileX + cx) % LCD_MAP_W;
			//int addr = addr_base + cx % (GB_MAP_H * GB_MAP_W);
			
			// addr, where we read the tile pattern nr.
			int addr = addr_base + ty * LCD_MAP_W + tx;
			assert lcdc.bgTileMapSelect && addr >= 0x9C00 && addr < 0x9C00 + (LCD_MAP_H - 1)*LCD_MAP_W
					|| !lcdc.bgTileMapSelect && addr >= 0x9800 && addr < 0x9800 + (LCD_MAP_H - 1)*LCD_MAP_W
				: "trying to read tile from fu place"; 
			
			int pnr = ram.read(addr);
			if ( lcdc.tileDataSelect ) {
				tileAddresses[ty][tx] = pnr;
			} else {
				// signed
				tileAddresses[ty][tx] = 0x100 + (byte)pnr; 
			} 
		}
	}
	
	
	/**
	 * Draws one line of the BackgroundMap onto the screen.
	 * @param screen Pointer to screen surface
	 * @param ram Pointer to memory interface
	 * @param vram Pointer to VRAM
	 * @param ly Screen line to draw onto
	 */
	public void drawLine(Screen screen, MemoryInterface ram, Vram vram, int ly) {
		int scx, scy, firstTileX, firstTileY;
		Tile[] tiles = vram.getTiles();

		scx = ram.read(ADDRESS_SCX);
		scy = ram.read(ADDRESS_SCY);

		firstTileX = scx/8;
		firstTileY = (scy + ly) / 8;
		
		//		Draw tiles
		/**
		 * dx, x on screen (from 0 - (GB_LCD_W - 1))
		 * dy, y on screen (from 0 - (GB_LCD_H - 1))
		 * datax, x of tile (from 0 - 31)
		 * datay, y of tile (from 0 - 31)
		 */
		int dx, dy, datax, datay, tileId;
		dy = (firstTileY)*8 - scy;
		datay = firstTileY % 32;

		for ( int x = 0; x < LCD_MAP_VISIBLE_W + 1; x++ ) {
			dx = (firstTileX + x)*GB_TILE_W - scx;
			datax = (firstTileX + x) % LCD_MAP_W;
			tileId = tileAddresses[datay][datax]; 
			screen.blitTile(tiles[tileId], ram.read(ADDRESS_PALETTE_BG_DATA), dx, dy, ly, true);
		}
	}
	
	
	/**
	 * Reads the BackgroundMap from memory.
	 * @param ly Screen line that is currently being rendered
	 * @param ram Pointer to memory interface
	 * @param lcdc Pointer to LCDC information
	 */
	// TODO Decrepit function? Remove?
	public void readBackground(MemoryInterface ram, LCDC lcdc) {
		// find base address
		int addr_base = 0;
		if (lcdc.bgTileMapSelect) {
			addr_base = 0x9C00;
		} else {
			addr_base = 0x9800;
		}


		// read tile numbers
		for (int i = 0; i < LCD_MAP_H; i++) {
			for (int j = 0; j < LCD_MAP_W; j++, addr_base++) {
				int pnr = ram.read(addr_base);
				if ( lcdc.tileDataSelect ) {
					tileAddresses[i][j] = pnr;
				} else {
					/*
					 * i think the problem is here. 
					 * pnr == 0 should translate to 0x9000 first
					 * 0x100 + (byte)pnr = 0x100. it will select tile[0x100] aka tile[256]. This will
					 * correspond to memory 0x8000 + 256*16 = 0x8000 + 0x100*0x10 = 0x9000
					 */
					tileAddresses[i][j] = 0x100 + (byte)pnr; 
				}
			}
		}
	}
	
	
	/**
	 * Draws the BackgroundMap onto the screen.
	 * @param screen Pointer to screen surface
	 * @param ram Pointer to memory interface
	 * @param vram Pointer to VRAM
	 * @param ly Screen line to draw onto
	 */
	// TODO Decrepit function? Remove?
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

		for ( int y = 0; y < LCD_H/8 + 1; y++ ) {
			dy = (firstTileY+y)*8 - scy;
			datay = (firstTileY+y) % 32;
			// For each column...
			for ( int x = 0; x < LCD_W/8 + 1; x++ ) {
				dx = (firstTileX+x)*8 - scx;
				datax = (firstTileX+x) % 32;
				tileId = tileAddresses[datay][datax];
				screen.blitTile(tiles[tileId], ram.read(ADDRESS_PALETTE_BG_DATA), dx, dy, ly, true);
			}
		}
	}
}