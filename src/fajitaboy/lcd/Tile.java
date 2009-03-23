package fajitaboy.lcd;

import fajitaboy.MemoryComponent;
import fajitaboy.MemoryInterface;

public class Tile extends MemoryComponent {
	public int data[] = new int[16];
	public int bits[][] = new int[8][8];
	
	public Tile() {
		
	}
	
	// This function should be removed later....
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
		
		bits = new int[8][8];
		                 
		for (int i = 0; i < 16; i += 2) {
			int lower = data[i];
			int upper = data[i+1];
			
			bits[i/2] = LCD.convertToPixels(lower, upper);
		}
		
	}
	
	public void write(final int address, final int data) {
		// Write to data
		int addr = address & 0x000F;
		this.data[addr] = data;
		
		// Update pixel
		addr = addr & 0x000E;
		int y = addr / 2;
		bits[y] = LCD.convertToPixels(this.data[addr], this.data[addr+1]);
	}
	
	public int read(final int address) {
		return data[address & 0x000F];
	}
	
	public void forceWrite(final int address, final int data) {
		write(address,data);
	}
	
	public int forceRead(final int address) {
		return read(address);
	}
	
	public int[][] getBits() {
		return bits;
	}
	
	public void reset() {
		data = new int[16];
		bits = new int[8][8];
	}
}
