package fajitaboy.gb.lcd;

import static fajitaboy.constants.AddressConstants.*;
import static fajitaboy.constants.LCDConstants.*;
import fajitaboy.gb.memory.MemoryInterface;
import fajitaboy.gb.memory.Vram;

public class WindowMap {
	/**
	 * Contains the id of each tile to be displayed.
	 */
	private int[][] tileAddresses;

	/**
	 * Default constructor.
	 */
	public WindowMap() {
		reset();
	}

	/**
	 * Clear WindowMap data.
	 */
	public void reset() {
		tileAddresses = new int[32][32];
	}

	/**
	 * Reads the Window from memory.
	 * @param ram Pointer to memory interface
	 * @param lcdc Pointer to LCDC information
	 */
	// TODO Decrepit function? Remove?
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
	
	/**
	 * Reads the Window from memory. Only reads the line that is currently being rendered.
	 * @param ly Screen line that is currently being rendered
	 * @param ram Pointer to memory interface
	 * @param lcdc Pointer to LCDC information
	 */
	public void readWindowLine(int ly, MemoryInterface ram, LCDC lcdc) {
		int scx, scy;

		scx = ram.read(ADDRESS_WX);
		scy = ram.read(ADDRESS_WY);
		
		// The window becomes visible (if enabled) when positions are set in range WX=0..166, WY=0..143. 
		// A postion of WX=7, WY=0 locates the window at upper left, it is then completely covering normal background.
        if (!visibleAt(ly, scx, scy)) {
            return;
        }
		scx -= 7;
		
		// we should not need mod here
		int firstTileY = ((ly - scy) / 8);
		int firstTileX = (scx / 8);
		
		int addr_base = 0;
		if (lcdc.windowTileMapSelect) {
			addr_base = 0x9C00;
		} else {
			addr_base = 0x9800;
		}
		
        // only reads relevant tiles
        for (int cx = scx, tx = firstTileX; cx < GB_LCD_W; cx += 8, tx++) {
            int ty = firstTileY;
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
	
	/**
	 * Draws one line of the window onto the screen.
	 * @param screen Pointer to screen surface
	 * @param ram Pointer to memory interface
	 * @param vram Pointer to VRAM
	 * @param ly Screen line to draw onto
	 */
	public void drawLine(Screen screen, MemoryInterface ram, Vram vram, int ly) {
// 		Prepare variables
		int scx, scy;
		Tile[] tiles = vram.getTiles();

		scx = ram.read(ADDRESS_WX);
		scy = ram.read(ADDRESS_WY);
		// The window becomes visible (if enabled) when positions are set in range WX=0..166, WY=0..143. 
		// A postion of WX=7, WY=0 locates the window at upper left, it is then completely covering normal background.
		if (!visibleAt(ly, scx, scy)) {
            return;
        }
		scx -= 7;

        int firstTileY = ((ly - scy) / 8);  // y blir == 32 här, resultat -> knas 
        int firstTileX = (scx / 8);
        
		//	Draw tiles
        
        int p = ram.read(PALETTE_BG_DATA);
        int sy = firstTileY * 8 + scy; // ly - (ly - scy); 
       
        
        for (int x = scx, tx = firstTileX; x < GB_LCD_W; x += 8, tx++) {
            int tileId = tileAddresses[firstTileY][tx];
            
            screen.blitTile(tiles[tileId], p, x, sy, ly, false);
        }
	}

	/**
	 * Draws the window onto the screen.
	 * @param screen Pointer to screen surface
	 * @param ram Pointer to memory interface
	 * @param vram Pointer to VRAM
	 * @param ly Screen line to draw onto
	 */
	// TODO Decrepit function? Remove?
	public void draw(Screen screen, MemoryInterface ram, Vram vram, int ly) {
		// 		Prepare variables
		int scx, scy;

		scx = ram.read(ADDRESS_WX);
		scy = ram.read(ADDRESS_WY);
		// The window becomes visible (if enabled) when positions are set in range WX=0..166, WY=0..143. 
		// A postion of WX=7, WY=0 locates the window at upper left, it is then completely covering normal background.
        if (!visibleAt(ly, scx, scy)) {
            return;
        }
		scx -= 7;

		//	Draw tiles
		Tile[] tiles = vram.getTiles();
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

	/**
	 * Returns if window is visible on a given screen line
	 * @param ly Screen line to check
	 * @param scx Window X scroll coordinate
	 * @param scy Window Y scroll coordinate
	 * @return True if visible at given line
	 */
    private static boolean visibleAt(int ly, int scx, int scy) {
        return !(scx < 0 || scx > 166 || scy < 0 || scy > 143 || scy > ly);
    }
}
