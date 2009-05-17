package fajitaboy;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import fajitaboy.gb.Cpu;
import fajitaboy.gb.GameLinkCable;
import fajitaboy.gb.Oscillator;
import fajitaboy.gb.memory.AddressBus;

/**
 * This is the Game Boy emulation core.
 * 
 * @author Tobias
 */

public class EmulatorCoreGB implements EmulatorCore {

	protected Oscillator oscillator;
	
	public EmulatorCoreGB() {
	}
	
	/**
	 * Creates an emulator core with the specified game cartridge.
	 * @param cartridge Array containg the game cartridge data
	 */
	public EmulatorCoreGB( final int[] cartridge ) {
		AddressBus addressBus = new AddressBus(cartridge);
		Cpu cpu = new Cpu(addressBus);
		oscillator = new Oscillator(cpu, addressBus);
	}
	
	public EmulatorCoreGB( final int[] cartridge, VideoReciever vr, AudioReciever ar ) {
		AddressBus addressBus = new AddressBus(cartridge);
		Cpu cpu = new Cpu(addressBus);
		oscillator = new Oscillator(cpu, addressBus, vr, ar);
	}
	
	public void playerInput(int keys) {
		oscillator.setKeys(keys);
	}

	/** {@inheritDoc} */
	public void reset() {
		oscillator.reset();
	}

	/** {@inheritDoc} */
	public void setVideoReciever(VideoReciever videoReciever) {
		// TODO Auto-generated method stub

	}

	/** {@inheritDoc} */
	public void saveState(FileOutputStream fos) throws IOException {
		oscillator.saveState(fos);
	}

	/** {@inheritDoc} */
	public void readState(FileInputStream is) throws IOException {
		oscillator.readState(is);
	}

	public void stop() {
		oscillator.stop();
	}

	/** {@inheritDoc} */
	public void run(int cycles) {
		oscillator.run(cycles);
	}

	/** {@inheritDoc} */
	public int readSerial() {
		return oscillator.readSerial();
	}

	/** {@inheritDoc} */
	public void writeSerial(int data) {
		oscillator.writeSerial(data);
	}
	
	public void setSerialHost(boolean host) {
		oscillator.setSerialHost(host);
	}
	
	public void setGameLinkCable(GameLinkCable glc) {
		oscillator.setGameLinkCable(glc);
	}
}
