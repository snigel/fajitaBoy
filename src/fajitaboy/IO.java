package fajitaboy;
import static fajitaboy.constants.AddressConstants.*;

/**
 * Represent the I/0 part of the memory.
 * @author Adam Hulin, Johan Gustafsson
 */
public class IO extends MemoryComponent {

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
     * {@inheritDoc}
     */
    public final void write(final int address, final int data) {
        int addr = address - offset;
        if (addr < 0 || addr > ram.length) {
            throw new ArrayIndexOutOfBoundsException("RamHigh.java");
        }
        if (address == DIV_REGISTER) {
            ram[addr] = 0;
        } else if (address != LY_REGISTER) { //LY is read only, don't handle.
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
    }
}
