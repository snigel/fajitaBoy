package fajitaboy;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import fajitaboy.gb.Cpu;
import fajitaboy.gb.Oscillator;
import fajitaboy.gb.audio.SoundHandler;
import fajitaboy.gb.memory.AddressBus;

import static fajitaboy.constants.HardwareConstants.*;

/**
 * This is the Game Boy emulation core.
 * 
 * @author Tobias
 */

public class EmulatorCoreGB implements EmulatorCore {

	private AddressBus addressBus;
	
	private Cpu cpu;
	
	private Oscillator oscillator;
	
	private SoundHandler soundHandler;
	
	private int cycleStep;
	
	/**
	 * Creates an emulator core with the specified game cartridge.
	 * @param cartridge Array containg the game cartridge data
	 */
	public EmulatorCoreGB( final int[] cartridge ) {
		addressBus = new AddressBus(cartridge);
		cpu = new Cpu(addressBus);
		oscillator = new Oscillator(cpu, addressBus);
		soundHandler = oscillator.getSoundHandler();
	}
	
	public EmulatorCoreGB( final int[] cartridge, VideoReciever videoReciever ) {
		addressBus = new AddressBus(cartridge);
		cpu = new Cpu(addressBus);
		oscillator = new Oscillator(cpu, addressBus, videoReciever);
	}
	
	public void playerInput(int keys) {
		addressBus.getJoyPad().setKeys(keys);
	}

	@Override
	public void reset() {
		addressBus.reset();
		cpu.reset();
		oscillator.reset();
		cycleStep = GB_CYCLES_PER_FRAME;
	}

	@Override
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

	public void stop() {
		oscillator.stop();
	}

	@Override
	public void run(int cycles) {
		oscillator.run(cycles);
	}

	@Override
	public void disableAudio() {
		oscillator.disableAudio();
	}

	@Override
	public void enableAudio() {
		oscillator.enableAudio();
	}

	@Override
	public int getVolume() {
		return oscillator.getVolume();
	}

	@Override
	public boolean isAudioEnabled() {
		return oscillator.isAudioEnabled();
	}

	@Override
	public void setVolume(int vol) {
		oscillator.setVolume(vol);
	}
	
}
