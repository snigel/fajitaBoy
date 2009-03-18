package fajitaboy.lcd;

import fajitaboy.MemoryInterface;

public class Sprite {
	int bits[][];
	int h = 8; // Height of sprite in pixels
	int x, y;
	int tileNumber;
	
	boolean behindBg;
	boolean flipX;
	boolean flipY;
	
	public Sprite(int height) {
		h = height;
		bits = new int[height][8];
	}
	
	public void readSprite(MemoryInterface ram, int address) {
		y = ram.read(address);
		x = ram.read(address+1);
		tileNumber = ram.read(address+2);
		int flags = ram.read(address+3);
		
		behindBg = (flags & 0x10) > 0;
		flipY = (flags & 0x08) > 0;
		flipX = (flags & 0x04) > 0;
	}
	
	public int[][] getSprite(MemoryInterface ram) {
		return getTile(ram).getTile();
	}
	
	public Tile getTile(MemoryInterface ram) {
		Tile t = new Tile();
		t.readTile(ram, 0x8000 + tileNumber);
		return t;
	}
	
	public void setHeight( int height ) {
		if ( h != height ) {
			h = height;
			bits = new int[height][8];
		}
	}
}