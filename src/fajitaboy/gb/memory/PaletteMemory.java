package fajitaboy.gb.memory;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import fajitaboy.FileIOStreamHelper;
import fajitaboy.gb.StateMachine;
import fajitaboy.gb.memory.MemoryInterface;

public class PaletteMemory implements MemoryInterface, StateMachine {
    
    /**
     * Palette Index address
     */
    private final int PALETTE_INDEX;
    
    /**
     * Palette Data address
     */
    private final int PALETTE_DATA;
    
    /**
     * Palette index.
     */
    private int index;
    
    /**
     * If true when writing to Palette memory the index is incremented.
     */
    private boolean autoIncrement;
    /**
     * Color palette array.
     */
    private int[][] palettes;
    
    /**
     * Creates a new PaletteMemory.
     * @param indexAddress Palette Index address
     * @param dataAddress Palette Data address
     */
    public PaletteMemory(final int indexAddress, final int dataAddress) {
        PALETTE_INDEX = indexAddress;
        PALETTE_DATA  = dataAddress;
        reset();
    }

    /**
     * Reading from palette data returns the data in palette memory where
     * palette index point to.
     */
    public int read(int address) {
        if (address == PALETTE_INDEX) {
            return (autoIncrement ? (index | 0x80) : index);
        } else if (address == PALETTE_DATA) {
            
            int paletteNo = index >>> 3;
            int colorNo = (index & 7) >> 1;
            boolean evenIndex = (index & 0x01) == 0;
            if (evenIndex) {
                return (palettes[paletteNo][colorNo] >>> 8); 
            } else {
                return (palettes[paletteNo][colorNo] & 0xFF);
            }
        } 
        return -1;
    }
  
    /**
     * Writing to palette index changes the index (and the auto increment bit)
     * Writing to the palette data register writes the data to the palette memory at palette index.
     */
    public void write(int address, int data) {
        if (address == PALETTE_INDEX) {
            index = data & 0x3F;
            autoIncrement = ((data & 0x80) != 0);
        } else if (address == PALETTE_DATA) { 
            int paletteNo = index >>> 3;
            int colorNo = (index & 7) >> 1;
            boolean evenIndex = (index & 0x01) == 0;
            int colorValue;
            if (evenIndex) {
                colorValue = (palettes[paletteNo][colorNo] & 0xFF00) + data; 
            } else {
                colorValue = (palettes[paletteNo][colorNo] & 0x00FF) + (data << 8);
            }
            palettes[paletteNo][colorNo] = colorValue;
            if (autoIncrement) {
                index = ((index + 1) & 0x3F);
            }
        }
        
    }

    /**
     * {@inheritDoc}
     */
    public void reset() {
        index = 0;
        autoIncrement = false;
        palettes = new int[8][4];
        
        // Initially all colors are initialized as white.
        for (int p = 0; p < 8; p++) {
        	for (int c = 0; c < 4; c++) {
        		palettes[p][c] = 0x7FFF; //white
        	}
        }
    }
    
    /**
     * Returns an color palette array.
     * @param i palette number (0-7)
     * @return the color palette array with index i.
     */
    public int[] getPalette(int i) {
        return palettes[i];
    }
    
    /**
     * {@inheritDoc}
     */
    public int forceRead(int address) {
        return read(address);
    }
    
    /**
     * {@inheritDoc}
     */
    public void forceWrite(int address, int data) {
        write(address, data); 
    }

    /** {@inheritDoc} */
	public void readState(FileInputStream is) throws IOException {
		index = (int)FileIOStreamHelper.readData(is, 4);
		autoIncrement = FileIOStreamHelper.readBoolean(is);
		
		for ( int i = 0; i < 8; i++ ) {
			for ( int j = 0; j < 4; j++ ) {
				palettes[i][j] = (int)FileIOStreamHelper.readData(is, 4);
			}
		}
	}

	/** {@inheritDoc} */
	public void saveState(FileOutputStream os) throws IOException {
		FileIOStreamHelper.writeData(os, (long)index, 4);
		FileIOStreamHelper.writeBoolean(os, autoIncrement);
		
		for ( int i = 0; i < 8; i++ ) {
			for ( int j = 0; j < 4; j++ ) {
				FileIOStreamHelper.writeData(os, (long)palettes[i][j], 4);
			}
		}
	}
    
}
