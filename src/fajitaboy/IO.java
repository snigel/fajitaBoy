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
	IO(final int start, final int end) {
		super(start, end);
	}

	public void reset() {
			ram = new int[length];  
			write(FF10, 0x80);   
			write(FF11, 0xBF);   
			write(FF12, 0xF3);   
			write(FF14, 0xBF);   
			write(FF16, 0x3F);   
			write(FF19, 0xBF);   
			write(FF1A, 0x7F); 
			write(FF1B, 0xFF);
			write(FF1C, 0x9F);
			write(FF1E, 0xBF);
			write(FF20, 0xFF);
			write(FF23, 0xBF);
			write(FF24, 0x77);
			write(FF25, 0xF3);
			write(FF26, 0xF1); //SGB uses F0 instead.
			write(FF40, 0x91);
			write(FF47, 0xFC)   
			write(FF48, 0xFF)   
			write(FF49, 0xFF)    
    }
}
