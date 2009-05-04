package fajitaboy.gb.lcd;

import fajitaboy.gb.memory.MemoryComponent;
import fajitaboy.gb.memory.MemoryInterface;

public class Tile extends MemoryComponent {
	
	/**
	 * Raw data for tile
	 */
	public int data[] = new int[16];
	
	/**
	 * Data translated into the pixels that appear on screen
	 */
	public int bits[][] = new int[8][8];
	
	/**
	 * Default constructor
	 */
	public Tile() {
		reset();
	}
	
	/**
	 * Reads tile from memory
	 * @param ram Pointer to memory interface
	 * @param address Address to read tile from (start of 16-byte area)
	 */
	public void readTile(MemoryInterface ram, int address) {
		/* Each Tile occupies 16 bytes, where each 2 bytes represent a line:
		
		   Byte 0-1  First Line (Upper 8 pixels)
		   Byte 2-3  Next Line
		   etc.

		   For each line, the first byte defines the least significant bits of the color numbers for each pixel, and the second byte defines the upper bits of the color numbers. In either case, Bit 7 is the leftmost pixel, and Bit 0 the rightmost.
		 */
		for (int i = 0; i < 16; i += 2) {
			data[i] = ram.read(address + i);
			data[i+1] = ram.read(address + i+1);
			bits[i>>>1] = LCD.convertToPixels(data[i], data[i+1]);
		}
	}
	
	/**
	 * Writes data into memory, and updates the tile pixels with the given data.
	 */
	public void write(final int address, final int data) {
		// Write to data
		int addr = address & 0x000F;
		this.data[addr] = data;
		
		// Update pixel
		addr = addr & 0x000E;
		int y = addr / 2;
		bits[y] = LCD.convertToPixels(this.data[addr], this.data[addr+1]);
	}
	
	/**
	 * Read one byte of raw data.
	 */
	public int read(final int address) {
		return data[address & 0x000F];
	}
	
	/**
	 * {@inheritDoc}
	 */
	public void forceWrite(final int address, final int data) {
		write(address,data);
	}
	
	/**
	 * {@inheritDoc}
	 */
	public int forceRead(final int address) {
		return read(address);
	}
	
	/**
	 * Returns the pixels for this Tile.
	 * @return [8][8] array of pixels
	 */
	public int[][] getBits() {
		return bits;
	}
	
	/**
	 * Clear tile data.
	 */
	public void reset() {
		data = new int[16];
		bits = new int[8][8];
	}
}
