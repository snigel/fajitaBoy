package fajitaboy.lcd;

public class Sprite {
	int bits[][];
	int h = 8; // Height of sprite in pixels
	
	public Sprite(int height) {
		h = height;
		bits = new int[8][height];
	}
	
	public void readSprite(int address) {
		
	}
	
	public int[][] getSprite() {
		return null;
	}
	
	public void setHeight( int height ) {
		if ( h != height ) {
			h = height;
			bits = new int[8][height];
		}
	}
}