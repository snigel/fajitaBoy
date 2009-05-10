package fajitaboy;

import static fajitaboy.constants.HardwareConstants.GB_CYCLES_PER_FRAME;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import fajitaboy.gb.Cpu;
import fajitaboy.gb.Oscillator;
import fajitaboy.gb.memory.AddressBus;
import fajitaboy.gbc.CGB_Cpu;
import fajitaboy.gbc.CGB_Oscillator;
import fajitaboy.gbc.memory.CGB_AddressBus;

/**
 * This is the Game Boy emulation core.
 * 
 * @author Tobias
 */

public class EmulatorCoreCGB implements EmulatorCore {

	private CGB_AddressBus addressBus;
	
	private CGB_Cpu cpu;
	
	private CGB_Oscillator oscillator;
	
	int cycleStep;
	
	/**
	 * Creates an emulator core with the specified game cartridge.
	 * @param cartridge Array containg the game cartridge data
	 */
	public EmulatorCoreCGB( final int[] cartridge ) {
		addressBus = new CGB_AddressBus(cartridge);
		cpu = new CGB_Cpu(addressBus);
		oscillator = new CGB_Oscillator(cpu, addressBus);
	}
	
	public EmulatorCoreCGB( final int[] cartridge, VideoReciever videoReciever) {
		addressBus = new CGB_AddressBus(cartridge);
		cpu = new CGB_Cpu(addressBus);
		oscillator = new CGB_Oscillator(cpu, addressBus, videoReciever);
		cycleStep = GB_CYCLES_PER_FRAME;
	}
	
	public void playerInput(int keys) {
		addressBus.getJoyPad().setKeys(keys);
	}

	/** {@inheritDoc} */
	public void reset() {
		addressBus.reset();
		cpu.reset();
		oscillator.reset();
	}

	/** {@inheritDoc} */
	public void setVideoReciever(VideoReciever videoReciever) {
		// TODO Auto-generated method stub

	}

	/** {@inheritDoc} */
	public void saveState(FileOutputStream fos) throws IOException {
		oscillator.saveState(fos);
		addressBus.saveState(fos);
	}

	/** {@inheritDoc} */
	public void readState(FileInputStream is) throws IOException {
		oscillator.readState(is);
		addressBus.readState(is);
	}

	/** {@inheritDoc} */
	public void run(int cycles) {
		// TODO Auto-generated method stub
		oscillator.run(cycles);
	}

	/** {@inheritDoc} */
	public void stop() {
		oscillator.stop();
		
	}
	
	/** {@inheritDoc} */
	public void disableAudio() {
		oscillator.disableAudio();
	}

	/** {@inheritDoc} */
	public void enableAudio() {
		oscillator.enableAudio();
	}

	/** {@inheritDoc} */
	public int getVolume() {
		return oscillator.getVolume();
	}

	/** {@inheritDoc} */
	public boolean isAudioEnabled() {
		return oscillator.isAudioEnabled();
	}

	/** {@inheritDoc} */
	public void setVolume(int vol) {
		oscillator.setVolume(vol);
	}
	
}
