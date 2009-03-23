package fajitaboy;
import static fajitaboy.constants.AddressConstants.*;

/**
 * Represent the I/0 part of the memory.
 * @author Adam Hulin, Johan Gustafsson
 */
public class IO extends MemoryComponent {

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
    
    public int read(final int address) {
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
    public final void write(final int address, final int data) {
        int addr = address - offset;
        if (addr < 0 || addr > ram.length) {
            throw new ArrayIndexOutOfBoundsException("RamHigh.java");
        }
        
        if (address == DIV_REGISTER) {
            ram[addr] = 0;
        } else if (address == LY_REGISTER) { 
            // LY is read only, don't handle.
        } else if (address == ADDRESS_JOYPAD) {
            ram[addr] = data;
            jp.refresh();
        } else {
            ram[addr] = data;
        }
        
        
    }

    /**
     * Writes the initial values to some addresses in I/0.
     */
    public final void reset() {
        ram = new int[length];
        write(NR10_REGISTER, 0x80);
        write(NR11_REGISTER, 0xBF);
        write(NR12_REGISTER, 0xF3);
        write(NR14_REGISTER, 0xBF);
        write(NR21_REGISTER, 0x3F);
        write(NR24_REGISTER, 0xBF);
        write(NR30_REGISTER, 0x7F);
        write(NR31_REGISTER, 0xFF);
        write(NR32_REGISTER, 0x9F);
        write(NR34_REGISTER, 0xBF);
        write(NR41_REGISTER, 0xFF);
        write(NR44_REGISTER, 0xBF);
        write(NR50_REGISTER, 0x77);
        write(NR51_REGISTER, 0xF3);
        write(NR52_REGISTER, 0xF1); // SGB uses F0 instead.
        write(LCDC_REGISTER, 0x91);
        write(BGB_REGISTER, 0xFC);
        write(OBP0_REGISTER, 0xFF);
        write(OBP1_REGISTER, 0xFF);
        write(SOUND_ON_OFF, 0x80);
    }
    
    public JoyPad getJoyPad() {
        return jp;
    }
    
    public class JoyPad {
        /*
         * True if pressed.
         */
        private boolean up, down, left, right, a, b, start, select;
        
        public JoyPad() {
            reset();
        }
        
        public void reset() {
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
        
        public void refresh() {
            // http://nocash.emubase.de/pandocs.htm#joypadinput
            
            int jp = ram[ADDRESS_JOYPAD - offset];
            
            boolean button = (jp & 0x20) > 0;
            boolean direction = (jp & 0x10) > 0;
            
            if (!(button ^ direction)) {
                // if neither direction nor button is selected
                // or if both are selected, set lower nibble to 
                // 1 == no button pressed
                jp |= 0x0F;
            } else if (button) {
                // ugly as hell. ocha här istället om false.
                jp |= !a ? 0x01 : 0x00;
                jp |= !b ? 0x02 : 0x00;
                jp |= !select ? 0x04 : 0x00;
                jp |= !start ? 0x08 : 0x00;
            } else if (direction) {
                // ugly as hell.
                jp |= !down ? 0x01 : 0x00;
                jp |= !up ? 0x02 : 0x00;
                jp |= !left ? 0x04 : 0x00;
                jp |= !right ? 0x08 : 0x00;
            }
            
            ram[ADDRESS_JOYPAD - offset] = jp;
        }

        private void fireInterrupt() {
            ram[ADDRESS_IF - offset] |= 0x10; 
        }
        
        /**
         * @return the a
         */
        public boolean isA() {
            return a;
        }

        /**
         * @param a the a to set
         */
        public void setA(boolean a) {
            this.a = a;
            refresh();
            fireInterrupt();
        }

        /**
         * @return the b
         */
        public boolean isB() {
            return b;
        }

        /**
         * @param b the b to set
         */
        public void setB(boolean b) {
            this.b = b;
            refresh();
            fireInterrupt();
        }

        /**
         * @return the down
         */
        public boolean isDown() {
            return down;
        }

        /**
         * @param down the down to set
         */
        public void setDown(boolean down) {
            this.down = down;
            refresh();
            fireInterrupt();
        }

        /**
         * @return the left
         */
        public boolean isLeft() {
            return left;
        }

        /**
         * @param left the left to set
         */
        public void setLeft(boolean left) {
            this.left = left;
            refresh();
            fireInterrupt();
        }

        /**
         * @return the right
         */
        public boolean isRight() {
            return right;
        }

        /**
         * @param right the right to set
         */
        public void setRight(boolean right) {
            this.right = right;
            refresh();
            fireInterrupt();
        }

        /**
         * @return the select
         */
        public boolean isSelect() {
            return select;
        }

        /**
         * @param select the select to set
         */
        public void setSelect(boolean select) {
            this.select = select;
            refresh();
            fireInterrupt();
        }

        /**
         * @return the start
         */
        public boolean isStart() {
            return start;
        }

        /**
         * @param start the start to set
         */
        public void setStart(boolean start) {
            this.start = start;
            refresh();
            fireInterrupt();
        }

        /**
         * @return the up
         */
        public boolean isUp() {
            return up;
        }

        /**
         * @param up the up to set
         */
        public void setUp(boolean up) {
            this.up = up;
            refresh();
            fireInterrupt();
        }
    }
}
