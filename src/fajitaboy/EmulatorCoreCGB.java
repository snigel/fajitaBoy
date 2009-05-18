package fajitaboy;

import fajitaboy.gbc.CGB_Cpu;
import fajitaboy.gbc.CGB_Oscillator;
import fajitaboy.gbc.memory.CGB_AddressBus;

/**
 * This is the Game Boy emulation core.
 * 
 * @author Tobias
 */

public class EmulatorCoreCGB extends EmulatorCoreGB {
	
	/**
	 * Creates an emulator core with the specified game cartridge.
	 * @param cartridge Array containg the game cartridge data
	 */
	public EmulatorCoreCGB( final int[] cartridge ) {
		CGB_AddressBus addressBus = new CGB_AddressBus(cartridge);
		CGB_Cpu cpu = new CGB_Cpu(addressBus);
		oscillator = new CGB_Oscillator(cpu, addressBus);
	}
	
	public EmulatorCoreCGB( final int[] cartridge, VideoReciever vr, AudioReciever ar) {
		CGB_AddressBus addressBus = new CGB_AddressBus(cartridge);
		CGB_Cpu cpu = new CGB_Cpu(addressBus);
		oscillator = new CGB_Oscillator(cpu, addressBus, vr, ar);
	}
}
