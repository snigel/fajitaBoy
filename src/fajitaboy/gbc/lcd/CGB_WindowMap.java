package fajitaboy.gbc.lcd;

import static fajitaboy.constants.AddressConstants.ADDRESS_PALETTE_BG_DATA;
import static fajitaboy.constants.AddressConstants.ADDRESS_WX;
import static fajitaboy.constants.AddressConstants.ADDRESS_WY;
import static fajitaboy.constants.HardwareConstants.GB_TILES;
import static fajitaboy.constants.LCDConstants.LCD_MAP_W;
import static fajitaboy.constants.LCDConstants.LCD_W;
import fajitaboy.gb.lcd.LCDC;
import fajitaboy.gb.lcd.Screen;
import fajitaboy.gb.lcd.Tile;
import fajitaboy.gb.memory.MemoryInterface;
import fajitaboy.gb.memory.Vram;
import fajitaboy.gbc.memory.CGB_Vram;

public class CGB_WindowMap {
	/**
	 * Contains the id of each tile to be displayed.
	 */
	private int[][] tileAddresses;
	private CGB_MapAttribute[][] tileAttributes;
	
	/**
	 * Default constructor.
	 */
	public CGB_WindowMap() {
		tileAttributes = new CGB_MapAttribute[32][32];
		reset();
	}

	/**
	 * Clear WindowMap data.
	 */
	public void reset() {
		tileAddresses = new int[32][32];
		
        for(int i = 0; i < 32; i++) {
            for(int j = 0; j < 32; j++) {
            	tileAttributes[i][j] = new CGB_MapAttribute();
            }
        }
	}
	
	/**
	 * Reads the Window from memory. Only reads the line that is currently being rendered.
	 * @param ly Screen line that is currently being rendered
	 * @param ram Pointer to memory interface
	 * @param lcdc Pointer to LCDC information
	 */
	public void readWindowLine(int ly, MemoryInterface ram, CGB_Vram vram, LCDC lcdc) {
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
        for (int cx = scx, tx = firstTileX; cx < LCD_W; cx += 8, tx++) {
            int ty = firstTileY;
            // addr, where we read the tile pattern nr.
            int addr = addr_base + ty * LCD_MAP_W + tx; 
            
            tileAttributes[ty][tx].update(vram.read(addr, 1));
            
            int pnr = vram.read(addr, 0);
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
	public void drawLine(CGB_Screen screen, boolean drawAboveSprite, MemoryInterface ram, CGB_Vram vram, int ly) {
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
        
        int sy = firstTileY * 8 + scy; // ly - (ly - scy); 
       
        
        for (int x = scx, tx = firstTileX; x < LCD_W; x += 8, tx++) {
        	CGB_MapAttribute tileAttr = tileAttributes[firstTileY][tx];
        	if (tileAttr.aboveSprites == drawAboveSprite) {
	            int tileId = tileAddresses[firstTileY][tx] + tileAttr.vramBank * GB_TILES;
	            screen.blitTile(tiles[tileId], tileAttr.PaletteNo, x, sy, ly, false, tileAttr.flipX, tileAttr.flipY);
        	}
        }
	}
	
    private static boolean visibleAt(int ly, int scx, int scy) {
        return !(scx < 0 || scx > 166 || scy < 0 || scy > 143 || scy > ly);
    }
}
