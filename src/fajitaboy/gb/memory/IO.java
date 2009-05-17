package fajitaboy.gb.memory;
import static fajitaboy.constants.AddressConstants.*;
import static fajitaboy.constants.BitmaskConstants.*;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import fajitaboy.FileIOStreamHelper;
import fajitaboy.gb.GameLinkCable;
import fajitaboy.gb.StateMachine;

/**
 * Represent the I/0 part of the memory.
 * @author Adam Hulin, Johan Gustafsson
 */
public class IO extends MemoryComponent {

    /**
     * Creates the internal IO class joypad.
     */
    private JoyPad jp = new JoyPad();
    
    /**
     * Pointer to a GameLinkCable.
     */
    private GameLinkCable gameLinkCable;
    
    
    
    /**
     * The joypad is a part of the IO memory space.
     * This class handles everything about the joypad address.
     * @author DJ_BISSE & MC_ARVIXX
     *
     */
    public class JoyPad implements StateMachine {
        /**
         * Variable for all keys. Bit is 0 if pressed.
         * Bit 7 - Start
  		 * Bit 6 - Select
  		 * Bit 5 - B
  		 * Bit 4 - A
  		 * Bit 3 - Down
  		 * Bit 2 - Up
  		 * Bit 1 - Left
  		 * Bit 0 - Right

         */
        private int keys;

        /**
         * Starts the JoyPad and resets all variables to their default value.
         */
        public JoyPad() {
            reset();
        }

        /**
         * Resets all JoyPad variables to their default value.
         */
        public final void reset() {
            keys = 0xFF;
            refresh();
        }

        /**
         * Checks the status bits to see whether we're interested in the
         * buttons or the cross and updates the joypad register accordingly.
         */
        public final void refresh() {
            // http://nocash.emubase.de/pandocs.htm#joypadinput

            int joyPad = ram[ADDRESS_JOYPAD - offset];

            // bit 5: select button keys (0 = Select)
            boolean button = (joyPad & 0x20) == 0;
            // bit 4: select direction keys (0 = Select)
            boolean direction = (joyPad & 0x10) == 0;

            // reset lower nibble
            joyPad &= BITMASK_UPPER_NIBBLE_MASK;
            if (!(button ^ direction)) {
                // if neither direction nor button is selected
                // or if both are selected, set lower nibble to
                // 1 == no button pressed
                joyPad |= BITMASK_LOWER_NIBBLE_MASK;
            } else if (button) {
                joyPad |= (keys & 0xF0) >> 4;
            } else if (direction) {
                joyPad |= keys & 0x0F;
            }

            ram[ADDRESS_JOYPAD - offset] = joyPad;
        }
        
        public void setKeys ( int newKeys ) {
        	if ( keys != newKeys ) {
        		keyInterrupt();
        	}
        	keys = newKeys;
        	refresh();
        }
        
        /**
         * Creates a interrupt from the keypad.
         */
        private void keyInterrupt() {
            ram[ADDRESS_IF - offset] |= 0x10;
        }
        
        public void readState( FileInputStream fis ) throws IOException {
        	keys = (int) FileIOStreamHelper.readData( fis, 1 );
        	refresh(); // Update joypad register to correct value
        }
        
        public void saveState( FileOutputStream fos ) throws IOException {
        	FileIOStreamHelper.writeData( fos, (long) keys, 1 );
        }
    }
    
    

    /**
     * @param start
     *            , address representing where IO begins in memory space
     * @param end
     *            , address representing where IO ends in memory space These two
     *            values are used for creating the right size of the IO array
     *            and for setting the offset value
     */
    public IO(final int start, final int end) {
        super(start, end);
    }

    /**
     * The read method reads data from the IO addresses.
     * @param address the memory address to read from.
     * @return the value at the address
     */
    public final int read(final int address) {
        int addr = address - offset;
        if (addr < 0 || addr > ram.length) {
            throw new ArrayIndexOutOfBoundsException(
                    String.format("RamLow.java: %04x", address));
        }
        return ram[addr];
    }

    /**
     * {@inheritDoc}
     */
    public void write(final int address, final int data) {
        int addr = address - offset;
        if (addr < 0 || addr > ram.length) {
            throw new ArrayIndexOutOfBoundsException(
                    String.format("RamLow.java: %04x", address));
        }
        switch (address) {
            case ADDRESS_DIV: ram[addr] = 0; break;
            case ADDRESS_LY: break;
            //Used for hack to get sound length, envelope and sweep to work.
            case ADDRESS_NR10:ram[addr] = data & 0xFF; break;
            case ADDRESS_NR11:ram[addr] = data & 0xFF; break;
            case ADDRESS_NR12:ram[addr] = data & 0xFF; break;
            case ADDRESS_NR21:ram[addr] = data & 0xFF; break;            
            case ADDRESS_NR31:ram[addr] = data & 0xFF; break;
            case ADDRESS_NR41:ram[addr] = data & 0xFF; break;
            case ADDRESS_NR42:ram[addr] = data & 0xFF; break;
            case ADDRESS_JOYPAD: ram[addr] = data; jp.refresh(); break;
            case ADDRESS_SC:
            	if ( (data & 0x01) != 0 && gameLinkCable != null ) {
            		gameLinkCable.setSerialHost();
            		if ( (data & 0x80) != 0 ) {
                		gameLinkCable.enableTransfer();
                	}
            	}
            	ram[addr] = data;
            default: ram[addr] = data; break;

        }
    }

    /**
     * Writes the initial values to some addresses in I/0.
     */
    public final void reset() {
        ram = new int[length];
        write(ADDRESS_NR10, 0x80);
        write(ADDRESS_NR11, 0xBF);
        write(ADDRESS_NR12, 0xF3);
        write(ADDRESS_NR14, 0xBF);
        write(ADDRESS_NR21, 0x3F);
        write(ADDRESS_NR24, 0xBF);
        write(ADDRESS_NR30, 0x7F);
        write(ADDRESS_NR31, 0xFF);
        write(ADDRESS_NR32, 0x9F);
        write(ADDRESS_NR34, 0xBF);
        write(ADDRESS_NR41, 0xFF);
        write(ADDRESS_NR44, 0xBF);
        write(ADDRESS_NR50, 0x77);
        write(ADDRESS_NR51, 0xF3);
        write(ADDRESS_NR52, 0xF1); // SGB uses F0 instead.
        write(ADDRESS_LCDC, 0x91);
        write(ADDRESS_BGB, 0xFC);
        write(ADDRESS_OBP0, 0xFF);
        write(ADDRESS_OBP1, 0xFF);
        write(ADDRESS_SOUND_ON_OFF, 0x80);
    }

    /**
     * @return returns a joypad object.
     */
    public final JoyPad getJoyPad() {
        return jp;
    }
    
    public void setGameLinkCable(GameLinkCable gameLinkCable) {
		this.gameLinkCable = gameLinkCable;
	}

	public GameLinkCable getGameLinkCable() {
		return gameLinkCable;
	}
    
    /**
     * {@inheritDoc}
     */
    public void readState( FileInputStream fis ) throws IOException {
    	super.readState(fis);
    	jp.readState(fis);
    }
    
    /**
     * {@inheritDoc}
     */
    public void saveState( FileOutputStream fos ) throws IOException {
    	super.saveState(fos);
    	jp.saveState(fos);
    }
}
