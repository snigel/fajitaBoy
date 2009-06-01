package fajitaboy.gbc.memory;

import static fajitaboy.constants.AddressConstants.ADDRESS_DMA;
import static fajitaboy.constants.HardwareConstants.GB_SPRITE_ATTRIBUTES;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import fajitaboy.FileIOStreamHelper;
import fajitaboy.gb.memory.MemoryInterface;
import fajitaboy.gb.memory.Oam;
import fajitaboy.gbc.lcd.CGB_SpriteAttribute;

public class CGB_Oam extends Oam {
	
	/** ArrayList containing elements to be drawn behind background. */
	public ArrayList<CGB_SpriteAttribute> behindBG = new ArrayList<CGB_SpriteAttribute>(40);
	
	/** ArrayList containing elements to be drawn above background. */
	public ArrayList<CGB_SpriteAttribute> aboveBG = new ArrayList<CGB_SpriteAttribute>(40);
	
	/** Array containing all sprite attributes in order of index. */
	CGB_SpriteAttribute[] attributes;

	/**
	 * @param start
	 *            , address representing where OAM begins in memory space
	 * @param end
	 *            , address representing where OAM ends in memory space These
	 *            two values are used for creating the right size of the OAM
	 *            array and for setting the offset value
	 */
	public CGB_Oam(final int start, final int end, MemoryInterface memInt) {
		super(start, end, memInt);

		attributes = new CGB_SpriteAttribute[GB_SPRITE_ATTRIBUTES];
		for ( int i = 0; i< GB_SPRITE_ATTRIBUTES; i++ ) {
			attributes[i] = new CGB_SpriteAttribute();
			aboveBG.add(attributes[i]);
		}
		drawOrderChanged = true;
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
			CGB_SpriteAttribute sa = attributes[n];
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
