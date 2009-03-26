package fajitaboy.memory;

import fajitaboy.lcd.Tile;
import static fajitaboy.constants.LCDConstants.*;
import static fajitaboy.constants.AddressConstants.*;
/**
 * Represents the graphic ram, also known as VRAM.
 *
 * @author Adam Hulin, Johan Gustafsson
 */
public class Vram extends MemoryComponent {

	private Tile[] tiles;

    /**
     * @param start
     *            , address representing where vram begins in memory space
     * @param end
     *            , address representing where vram ends in memory space These
     *            two values are used for creating the right size of the vram
     *            array and for setting the offset value
     */
    public Vram(final int start, final int end) {
        super(start, end);
    }
    
    public int read(final int address) {
        int addr = address - offset;
        if (addr < 0 || addr > ram.length) {
            throw new ArrayIndexOutOfBoundsException("RamLow.java");
        } else if ( address < TILE_DATA_END ) {
        	return tiles[addr / 16].read(address);
        } else {
        	return ram[addr];
        }
    }

    /**
     * {@inheritDoc}
     */
    public void write(final int address, final int data) {
        int addr = address - offset;
        if (addr < 0 || addr > ram.length) {
            throw new ArrayIndexOutOfBoundsException("RamHigh.java");
        } else if ( address < TILE_DATA_END ) {
        	tiles[(addr & 0xFFF0) / 16].write(address, data);
        } else {
        	ram[addr] = data;
        }
    }

    /**
     * {@inheritDoc}
     */
    public void reset() {
        ram = new int[length];
        tiles = new Tile[2*GB_TILES];
        for ( int i = 0; i < 2*GB_TILES; i++) {
        	tiles[i] = new Tile();
        }
    }


    /**
     * {@inheritDoc}
     */
    public int forceRead(final int address) {
    	int addr = address - offset;
        if (addr < 0 || addr > ram.length) {
            throw new ArrayIndexOutOfBoundsException("RamLow.java");
        } else if ( address < TILE_DATA_END ) {
        	return tiles[(addr & 0xFFF0) / 16].read(address);
        } else {
        	return ram[addr];
        }
    }

    /**
     * {@inheritDoc}
     */
    public void forceWrite(final int address, final int data) {
        int addr = address - offset;
        if (addr < 0 || addr > ram.length) {
            throw new ArrayIndexOutOfBoundsException("RamHigh.java");
        } else if ( address < TILE_DATA_END ) {
        	tiles[(addr & 0xFFF0) / 16].write(address, data);
        } else {
        	ram[addr] = data;
        }
    }

	public Tile[] getTiles() {
		return tiles;
	}
}
