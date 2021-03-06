package fajitaboy.gbc.lcd;

import static fajitaboy.constants.AddressConstants.*;
import static fajitaboy.constants.HardwareConstants.*;
import static fajitaboy.constants.LCDConstants.*;
import fajitaboy.gb.lcd.LCDC;
import fajitaboy.gb.lcd.Tile;
import fajitaboy.gb.memory.MemoryInterface;
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
		
		int addr_base = 0;
		if (lcdc.windowTileMapSelect) {
			addr_base = 0x9C00;
		} else {
			addr_base = 0x9800;
		}
		
        // only reads relevant tiles
        for (int cx = scx, tx = 0; cx < LCD_W; cx += 8, tx++) {
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
	 * 
	 * Draws one line of the window onto the screen.
	 * @param drawAboveSprite Draws the window tiles with map attribute BG-to-OAM Priority set if true.
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
        
		//	Draw tiles
 
        int sy = firstTileY * 8 + scy; // ly - (ly - scy); 
       
        
        for (int x = scx, tx = 0; x < LCD_W; x += 8, tx++) {
        	CGB_MapAttribute tileAttr = tileAttributes[firstTileY][tx];
        	if (tileAttr.aboveSprites == drawAboveSprite) {
	            int tileId = tileAddresses[firstTileY][tx] + tileAttr.vramBank * CGB_TILES;
	            screen.blitTile(tiles[tileId], tileAttr.PaletteNo, x, sy, ly, tileAttr.flipX, tileAttr.flipY, true);
        	}
        }
	}
	
    private static boolean visibleAt(int ly, int scx, int scy) {
        return !(scx < 0 || scx > 166 || scy < 0 || scy > 143 || scy > ly);
    }
}
