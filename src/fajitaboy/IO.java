package fajitaboy;

/**
 * Represent the I/0 part of the memory.
 *
 * @author Adam Hulin, Johan Gustafsson
 *
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
	public void write(final int address, final int data) {
	        int addr = address - offset;
	        if (addr < 0 || addr > ram.length) {
	            throw new ArrayIndexOutOfBoundsException("RamHigh.java");
	        }
	        if(address==0xFF04){ //DIV Register
	        	ram[addr] = 0;
	        }
	        else if(address==0xFF44){
	        	//Read only register, do nothing
	        }
	        else
	        ram[addr] = data;
	}

	/**
     * Writes the initial values to some addresses in I/0. 
     */
	public void reset() {
			ram = new int[length];
			write(0xFF10, 0x80);
			write(0xFF11, 0xBF);
			write(0xFF12, 0xF3);
			write(0xFF14, 0xBF);
			write(0xFF16, 0x3F);
			write(0xFF19, 0xBF);
			write(0xFF1A, 0x7F);
			write(0xFF1B, 0xFF);
			write(0xFF1C, 0x9F);
			write(0xFF1E, 0xBF);
			write(0xFF20, 0xFF);
			write(0xFF23, 0xBF);
			write(0xFF24, 0x77);
			write(0xFF25, 0xF3);
			write(0xFF26, 0xF1); //SGB uses F0 instead.
			write(0xFF40, 0x91);
			write(0xFF47, 0xFC);
			write(0xFF48, 0xFF);
			write(0xFF49, 0xFF);
    }
}
