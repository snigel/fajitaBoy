package fajitaboy.lcd;

import fajitaboy.MemoryInterface;


public class LCDC {
	/*
Bit 7 - LCD Display Enable             (0=Off, 1=On)
Bit 6 - Window Tile Map Display Select (0=9800-9BFF, 1=9C00-9FFF)
Bit 5 - Window Display Enable          (0=Off, 1=On)
Bit 4 - BG & Window Tile Data Select   (0=8800-97FF, 1=8000-8FFF)
Bit 3 - BG Tile Map Display Select     (0=9800-9BFF, 1=9C00-9FFF)
Bit 2 - OBJ (Sprite) Size              (0=8x8, 1=8x16)
Bit 1 - OBJ (Sprite) Display Enable    (0=Off, 1=On)
Bit 0 - BG Display (for CGB see below) (0=Off, 1=On)
	 */ 
	public boolean lcdDisplayEnable;
	public boolean windowTileMapSelect;
	public boolean windowDisplayEnable;
	public boolean tileDataSelect;
	public boolean bgTileMapSelect;
	public boolean objSpriteSize;
	public boolean objSpriteDisplay;
	public boolean bgDisplay;
	
	public void readLCDC(MemoryInterface ram) {
		int b = ram.read(0xFF40);
		lcdDisplayEnable = (b & 0x80) > 0;
		windowTileMapSelect = (b & 0x40) > 0;
		windowDisplayEnable = (b & 0x20) > 0;
		tileDataSelect = (b & 0x10) > 0;
		bgTileMapSelect = (b & 0x08) > 0;
		objSpriteSize = (b & 0x04) > 0;
		objSpriteDisplay = (b & 0x02) > 0;
		bgDisplay = (b & 0x01) > 0;
	}
}
