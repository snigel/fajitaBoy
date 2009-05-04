package fajitaboy.gbc.lcd;

import static fajitaboy.constants.AddressConstants.ADDRESS_SCX;
import static fajitaboy.constants.AddressConstants.ADDRESS_SCY;
import static fajitaboy.constants.AddressConstants.PALETTE_BG_DATA;
import static fajitaboy.constants.LCDConstants.GB_LCD_H;
import static fajitaboy.constants.LCDConstants.GB_LCD_W;
import static fajitaboy.constants.LCDConstants.GB_MAP_H;
import static fajitaboy.constants.LCDConstants.GB_MAP_VISIBLE_W;
import static fajitaboy.constants.LCDConstants.GB_MAP_W;
import static fajitaboy.constants.LCDConstants.GB_TILES;
import static fajitaboy.constants.LCDConstants.GB_TILE_W;
import fajitaboy.gb.lcd.LCDC;
import fajitaboy.gb.lcd.Tile;
import fajitaboy.gb.memory.AddressBus;
import fajitaboy.gb.memory.MemoryInterface;
import fajitaboy.gb.memory.Vram;
import fajitaboy.gbc.memory.CGB_AddressBus;
import fajitaboy.gbc.memory.CGB_Vram;

/**
 * BackgroundMap that handles colors.
 */
public class CGB_BackgroundMap {
    /**
     * Contains the id of each tile to be displayed.
     */
    private int[][] tileAddresses = new int[32][32];
    private CGB_MapAttribute[][] tileAttributes = new CGB_MapAttribute[32][32];

    /**
     * Default constructor.
     */
    public CGB_BackgroundMap() {
        reset();
        
        for(int i = 0; i < 32; i++) {
            for(int j = 0; j < 32; j++) {
                tileAttributes[i][j] = new CGB_MapAttribute();
            }
        }
    }

    /**
     * Resets the BackgroundMap with empty data.
     */
    public void reset() {
        tileAddresses = new int[32][32];
        tileAttributes = new CGB_MapAttribute[32][32];
    }
    
    /**
     * Reads the BackgroundMap from memory. Only reads the line that is currently being rendered.
     * @param ly Screen line that is currently being rendered
     * @param ram Pointer to memory interface
     * @param lcdc Pointer to LCDC information
     */
    public void readBackgroundWholeLine(int ly, MemoryInterface ram, CGB_Vram vram, LCDC lcdc) {
        int scy, firstTileY;
        
        scy = ram.read(ADDRESS_SCY);

        firstTileY = ((scy + ly) / 8) % 32;
        
        int addr_base = 0;
        if (lcdc.bgTileMapSelect) {
            addr_base = 0x9C00;
        } else {
            addr_base = 0x9800;
        }
        
        for (int cx = 0; cx < GB_MAP_W; cx++) {
            /*
             * ty & tx, the index in the background array.
             * ty should be firstTileY or firstTileY + 1
             */
            int ty = firstTileY; // (firstTileY + (firstTileX + cx)/GB_MAP_H) % GB_MAP_H; 

            // addr, where we read the tile pattern nr.
            int addr = addr_base + ty * GB_MAP_W + cx;
            
            tileAttributes[ty][cx].update(vram.read(addr, 1));

            int pnr = vram.read(addr, 0);//vram.read(addr, tileAttributes[ty][cx].vramBank);
            
            if ( lcdc.tileDataSelect ) {
                tileAddresses[ty][cx] = pnr;
            } else {
                // signed
                tileAddresses[ty][cx] = 0x100 + (byte)pnr; 
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
    public void drawLine(CGB_Screen screen, MemoryInterface ram, CGB_Vram vram, int ly) {
        int scx, scy, firstTileX, firstTileY;
        Tile[] tiles = vram.getTiles();

        scx = ram.read(ADDRESS_SCX);
        scy = ram.read(ADDRESS_SCY);

        firstTileX = scx/8;
        firstTileY = (scy + ly) / 8;
        
        //      Draw tiles
        /**
         * dx, x on screen (from 0 - (GB_LCD_W - 1))
         * dy, y on screen (from 0 - (GB_LCD_H - 1))
         * datax, x of tile (from 0 - 31)
         * datay, y of tile (from 0 - 31)
         */
        int dx, dy, datax, datay, tileId;
        dy = (firstTileY)*8 - scy;
        datay = firstTileY % 32;

        for ( int x = 0; x < GB_MAP_VISIBLE_W + 1; x++ ) {
            dx = (firstTileX + x)*GB_TILE_W - scx;
            datax = (firstTileX + x) % GB_MAP_W;
            tileId = tileAddresses[datay][datax] + tileAttributes[datay][datax].vramBank * 2 * GB_TILES;
            screen.blitTile(tiles[tileId], tileAttributes[datay][datax].PaletteNo, dx, dy, ly, true);
            
        }

    }
    

}