package fajitaboy.lcd;

import fajitaboy.MemoryInterface;

public class BackgroundMap {
	private static enum MapType {BACKGROUND, WINDOW}

	private Tile[][] data = new Tile[32][32];
	MapType type;
	
	public void readBackground(MemoryInterface ram, MapType type) {
		LCDC lcdc = new LCDC();
		lcdc.readLCDC(ram);
		
		
		//read tile numbers
		int tileNumbers[] = new int[256];
		for (int i = 0; i < 256; i++) {
			int addr = 0;
			
			if (type == MapType.BACKGROUND) {
				if (lcdc.bgTileMapSelect) {
					addr = 0x9800;
				} else {
					addr = 0x9C00;
				}
			} else if (type == MapType.WINDOW) {
				if (lcdc.windowTileMapSelect) {
					addr = 0x9800;
				} else {
					addr = 0x9C00;
				}
			}
			
			tileNumbers[i] = ram.read(addr + i);
		}
		
		//read tile patterns
		for (int i = 0; i < 32; i++) {
			for (int j = 0; j < 32; j++) {
				Tile t = new Tile();
				int addr;
				
				if (lcdc.tileDataSelect) {
					addr = 0x8800 + tileNumbers[i*32 + j];
				} else {
					addr = 0x9000 + (256 - tileNumbers[i*32 + j]); //TODO antagligen fel
				}
				
				t.readTile(ram, addr);
				data[i][j] = t;
			}
		}
	}
}