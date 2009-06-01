package fajitaboy.gb.memory;

import static fajitaboy.constants.AddressConstants.*;
import static fajitaboy.constants.HardwareConstants.*;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import fajitaboy.FileIOStreamHelper;
import fajitaboy.gb.lcd.SpriteAttribute;

/**
 * Represents the Sprite table part of the memory.
 *
 * @author Adam Hulin, Johan Gustafsson
 *
 */
public class Oam extends MemoryComponent {

	/** ArrayList containing elements to be drawn behind background. */
	public ArrayList<SpriteAttribute> behindBG = new ArrayList<SpriteAttribute>(40);
	
	/** ArrayList containing elements to be drawn above background. */
	public ArrayList<SpriteAttribute> aboveBG = new ArrayList<SpriteAttribute>(40);
	
	/** Sorted array containing elements to be drawn behind background. */
	public Object[] behindBGArray;
	
	/** Sorted array containing elements to be drawn above background. */
	public Object[] aboveBGArray;
	
	/** True if draw order of sprites has been changed and arrays need to be updated. */
	protected boolean drawOrderChanged;
	
	/** Array containing all sprite attributes in order of index. */
	SpriteAttribute[] attributes;

	/**
	 * Used in dma transfer.
	 */
	protected MemoryInterface memInt;

	/**
	 * @param start
	 *            , address representing where OAM begins in memory space
	 * @param end
	 *            , address representing where OAM ends in memory space These
	 *            two values are used for creating the right size of the OAM
	 *            array and for setting the offset value
	 */
	public Oam(final int start, final int end, MemoryInterface memInt) {
		super(start, end);
		this.memInt = memInt;

		attributes = new SpriteAttribute[GB_SPRITE_ATTRIBUTES];
		for ( int i = 0; i< GB_SPRITE_ATTRIBUTES; i++ ) {
			attributes[i] = new SpriteAttribute();
			aboveBG.add(attributes[i]);
		}
		drawOrderChanged = true;
	}

	public Oam() {
	}

	/**
	 * {@inheritDoc}
	 */
	public void write(final int address, final int data) {
		int addr = address - offset;
		if (address == ADDRESS_DMA) {
			dmaTransfer(data);
		} else if (addr < 0 || addr > ram.length) {
			throw new ArrayIndexOutOfBoundsException("Oam.java");
		} else {
			ram[addr] = data;

			// Update SpriteAttribute
			int type = address & 0x03;
			int n = addr >>> 2;
			SpriteAttribute sa = attributes[n];
			if ( type == 0 ) {
				// Y coordinate
				sa.setY(data);
			} else if ( type == 1 ) {
				// X coordinate
				if ( sa.x != data ) {
					behindBG.remove(sa);
					aboveBG.remove(sa);
					sa.setX(data);
					if ( sa.behindBG )
						behindBG.add(sa);
					else
						aboveBG.add(sa);
					drawOrderChanged = true;
				}
			} else if ( type == 2 ) {
				// Pattern Nr
				sa.setPattern(data);
			} else {
				// Flags
				if ( sa.flags != data ) {
					behindBG.remove(sa); 
					aboveBG.remove(sa);
					sa.setFlags(data);
					if ( sa.behindBG )
						behindBG.add(sa);
					else 
						aboveBG.add(sa);
					drawOrderChanged = true;
				}
			}
		}
		if ( behindBG.size() + aboveBG.size() != 40 ) {
			System.out.println("Error in the goddamn lists");
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public int read(final int address) {
		int addr = address - offset;
		if (address == ADDRESS_DMA) {
			return 0x00;
		} else if (addr < 0 || addr > ram.length) {
			throw new ArrayIndexOutOfBoundsException("Oam.java");
		} else {
			return ram[addr];
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public int forceRead(final int address) {
		int addr = address - offset;
		if (address == ADDRESS_DMA) {
			return 0x00;
		} else if (addr < 0 || addr > ram.length) {
			throw new ArrayIndexOutOfBoundsException("Oam.java");
		} else {
			return ram[addr];
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void forceWrite(final int address, final int data) {
		int addr = address - offset;
		if (address == ADDRESS_DMA) {
			dmaTransfer(data);
		} else if (addr < 0 || addr > ram.length) {
			throw new ArrayIndexOutOfBoundsException("Oam.java");
		} else {
			ram[addr] = data;
		}
	}

	/**
	 * Performs an OAM DMA transfer. This copies the area 0xXX00-0xXXFF from
	 * memory, where 0xXX is specified in the input variable data, into the area
	 * 0xFE00 - 0xFE9F.
	 *
	 * @param data
	 *            Memory area to copy. Must be in range 0x00 - 0xF1
	 */
	protected void dmaTransfer(int data) {
		//System.out.println(String.format("DMA Transfer, start address: %04x", data*0x100));
		if (data < 0 || data > 0xF1)
			return;

		int targAddr = data * 0x100;
		int destination = 0xFE00;
		while (destination < 0xFEA0) {
			write(destination, memInt.read(targAddr));
			targAddr++;
			destination++;
		}
	}
	
	/**
	 * Updates draw order arrays, if necessary.
	 */
	public void updateDrawOrder() {
		if ( drawOrderChanged ) {
			behindBG.trimToSize();
			behindBGArray = behindBG.toArray();
			Arrays.sort(behindBGArray);
			
			aboveBG.trimToSize();
			aboveBGArray = aboveBG.toArray();
			Arrays.sort(aboveBGArray);
			
			drawOrderChanged = false;
		}
	}
	
	/**
     * {@inheritDoc}
     */
    public void readState( FileInputStream fis ) throws IOException {
    	length = (int) FileIOStreamHelper.readData( fis, 4 );
    	offset = (int) FileIOStreamHelper.readData( fis, 4 );
    	ram = new int[length];
    	for ( int i = 0; i < length; i++ ) {
    		write(offset+i, (int) FileIOStreamHelper.readData( fis, 1 ));
    	}
    }
    
    /**
     * {@inheritDoc}
     */
    public void saveState( FileOutputStream fos ) throws IOException {
    	FileIOStreamHelper.writeData( fos, (long) length, 4 );
    	FileIOStreamHelper.writeData( fos, (long) offset, 4 );
    	for ( int i = 0; i < length; i++ ) {
    		FileIOStreamHelper.writeData( fos, read(offset+i), 1 );
    	}
    }
}