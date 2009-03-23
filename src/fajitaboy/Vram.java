package fajitaboy;

import fajitaboy.lcd.Tile;

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
        } else if ( address < 0x9800 ) {
        	return getTiles()[addr / 16].read(address);
        }
        return ram[addr];
    }

    /**
     * {@inheritDoc}
     */
    public void write(final int address, final int data) {
        int addr = address - offset;
        if (addr < 0 || addr > ram.length) {
            throw new ArrayIndexOutOfBoundsException("RamHigh.java");
        } else if ( address < 0x9800 ) {
        	getTiles()[(addr & 0xFFF0) / 16].write(address, data);
        }
        ram[addr] = data;
    }

    /**
     * {@inheritDoc}
     */
    public void reset() {
        ram = new int[length];
        tiles = new Tile[384];
        for ( int i = 0; i < 384; i++ )
        	getTiles()[i] = new Tile();
    }


    /**
     * {@inheritDoc}
     */
    public int forceRead(final int address) {
    	int addr = address - offset;
        if (addr < 0 || addr > ram.length) {
            throw new ArrayIndexOutOfBoundsException("RamLow.java");
        } else if ( address < 0x9800 ) {
        	return getTiles()[(addr & 0xFFF0) / 16].read(address);
        }
        return ram[addr];
    }

    /**
     * {@inheritDoc}
     */
    public void forceWrite(final int address, final int data) {
        int addr = address - offset;
        if (addr < 0 || addr > ram.length) {
            throw new ArrayIndexOutOfBoundsException("RamHigh.java");
        } else if ( address < 0x9800 ) {
        	getTiles()[(addr & 0xFFF0) / 16].write(address, data);
        }
        ram[addr] = data;
    }

	public Tile[] getTiles() {
		return tiles;
	}
}
