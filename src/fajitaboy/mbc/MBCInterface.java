package fajitaboy.mbc;

import fajitaboy.memory.Cartridge;
import fajitaboy.memory.MemoryInterface;

/**
 * Interface for Memory Bank Controllers
 *
 * @author Tobias Svensson
 *
 */

public interface MBCInterface {
	
	public void reset();
	public void write(int address, int data);
	public void setRomBank();
	public void setRamBank();
}
