package fajitaboy.gb.memory;
import static fajitaboy.constants.AddressConstants.*;
import static fajitaboy.constants.BitmaskConstants.*;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import fajitaboy.FileIOStreamHelper;
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

    /**
     * The joypad is a part of the IO memory space.
     * This class handles everything about the joypad address.
     * @author DJ_BISSE & MC_ARVIXX
     *
     */
    public class JoyPad implements StateMachine {
        /**
         * Variables for all keys. True if pressed.
         */
        private boolean up, down, left, right, a, b, start, select;

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
            up = false;
            down = false;
            left = false;
            right = false;
            a = false;
            b = false;
            start = false;
            select = false;
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
                joyPad |= a ? 0x00 : 0x01;
                joyPad |= b ? 0x00 : 0x02;
                joyPad |= select ? 0x00 : 0x04;
                joyPad |= start ? 0x00 : 0x08;
            } else if (direction) {
                joyPad |= right ? 0x00 : 0x01;
                joyPad |= left ? 0x00 : 0x02;
                joyPad |= up ? 0x00 : 0x04;
                joyPad |= down ? 0x00 : 0x08;
            }

            ram[ADDRESS_JOYPAD - offset] = joyPad;
        }
        /**
         * Creates a interrupt from the keypad.
         */
        private void keyInterrupt() {
            ram[ADDRESS_IF - offset] |= 0x10;
        }

        /**
         * @return the a
         */
        public final boolean isA() {
            return a;
        }

        /**
         * @param input the value to set a with
         */
        public final void setA(final boolean input) {
            a = input;
            refresh();
            keyInterrupt();
        }

        /**
         * @return the b.
         */
        public final boolean isB() {
            return b;
        }

        /**
         * @param input the value to set b with
         */
        public final void setB(final boolean input) {
            b = input;
            refresh();
            keyInterrupt();
        }

        /**
         * @return the down
         */
        public final boolean isDown() {
            return down;
        }

        /**
         * @param input the value to set down with
         */
        public final void setDown(final boolean input) {
            down = input;
            refresh();
            keyInterrupt();
        }

        /**
         * @return the left
         */
        public final boolean isLeft() {
            return left;
        }

        /**
         * @param input sets left to the value of input
         */
        public final void setLeft(final boolean input) {
            left = input;
            refresh();
            keyInterrupt();
        }

        /**
         * @return the right
         */
        public final boolean isRight() {
            return right;
        }

        /**
         * @param input sets right to the value of input.
         */
        public final void setRight(final boolean input) {
            right = input;
            refresh();
            keyInterrupt();
        }

        /**
         * @return the select
         */
        public final boolean isSelect() {
            return select;
        }

        /**
         * @param input Sets select to the value of input.
         */
        public final void setSelect(final boolean input) {
            select = input;
            refresh();
            keyInterrupt();
        }

        /**
         * @return the start
         */
        public final boolean isStart() {
            return start;
        }

        /**
         * @param input sets start to the value of input.
         */
        public final void setStart(final boolean input) {
            start = input;
            refresh();
            keyInterrupt();
        }

        /**
         * @return the up
         */
        public final boolean isUp() {
            return up;
        }

        /**
         * @param input sets up to the value of input.
         */
        public final void setUp(final boolean input) {
            up = input;
            refresh();
            keyInterrupt();
        }
        
        public void readState( FileInputStream fis ) throws IOException {
        	up = FileIOStreamHelper.readBoolean( fis );
        	down = FileIOStreamHelper.readBoolean( fis );
        	left = FileIOStreamHelper.readBoolean( fis );
        	right = FileIOStreamHelper.readBoolean( fis );
        	a = FileIOStreamHelper.readBoolean( fis );
        	b = FileIOStreamHelper.readBoolean( fis );
        	start = FileIOStreamHelper.readBoolean( fis );
        	select = FileIOStreamHelper.readBoolean( fis );
        	refresh(); // Update joypad register to correct value
        }
        
        public void saveState( FileOutputStream fos ) throws IOException {
        	FileIOStreamHelper.writeBoolean( fos, up );
        	FileIOStreamHelper.writeBoolean( fos, down );
        	FileIOStreamHelper.writeBoolean( fos, left );
        	FileIOStreamHelper.writeBoolean( fos, right );
        	FileIOStreamHelper.writeBoolean( fos, a );
        	FileIOStreamHelper.writeBoolean( fos, b );
        	FileIOStreamHelper.writeBoolean( fos, start );
        	FileIOStreamHelper.writeBoolean( fos, select );
        }
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
