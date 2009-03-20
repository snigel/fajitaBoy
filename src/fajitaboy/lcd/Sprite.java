package fajitaboy.lcd;

import fajitaboy.MemoryInterface;

public class Sprite {
	int bits[][];

	public Sprite() {
		bits = new int[8][8];
	}

	public void readSprite(MemoryInterface ram, int address) {
		
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
}