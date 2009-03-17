package fajitaboy.lcd;

import fajitaboy.MemoryInterface;

public class Tile {
	public int bits[][];
	
	public Tile(boolean tall) {
		bits = new int[8][8];
	}
	
	public void readTile(MemoryInterface ram, int address) {
		/** 
		 * 
Each Tile occupies 16 bytes, where each 2 bytes represent a line:

Byte 0-1  First Line (Upper 8 pixels)
Byte 2-3  Next Line
etc.

For each line, the first byte defines the least significant bits of the color numbers for each pixel, and the second byte defines the upper bits of the color numbers. In either case, Bit 7 is the leftmost pixel, and Bit 0 the rightmost.

		 */
		int data[] = new int[16];
		for (int i = 0; i < 16; i++) {
			data[i] = ram.read(address + i);
		}
		
		for (int i = 0; i < 8; i += 2) {
			int lower = data[i];
			int upper = data[i+1];
			
			bits[i] = LCD.convertToPixels(lower, upper);
		}
		
	}
	
	public int[][] getTile() {
		return bits;
	}
}
