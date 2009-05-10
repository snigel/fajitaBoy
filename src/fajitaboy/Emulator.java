package fajitaboy;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import fajitaboy.gb.StateMachine;

import static fajitaboy.constants.HardwareConstants.*;

/**
 * Encapsulates the emulator.
 * 
 * @author Marcus, Peter, Tobias
 */
public class Emulator implements Runnable, StateMachine{

	// Handles keys for Player 1 and Player 2
	public enum Player {PLAYER1, PLAYER2};
	public enum Keys {UP, DOWN, LEFT, RIGHT, A, B, START, SELECT };
	int p1keys;
	int p2keys;
	
	/**
	 * Pointer to a class that renders video.
	 */
	VideoReciever videoReciever;
	
	
	boolean running;
	
	/**
	 * Array containing emulator cores
	 */
	EmulatorCore[] cores;
	
	Emulator(final String path) {
		this(path, null);
	}
	
	/**
	 * Standard constructor.
	 * 
	 * @param path Rom path
	 */
	Emulator(final String path, VideoReciever videoReciever) {
		this.videoReciever = videoReciever;
		loadRom(path);
	}
	
	public void loadRom(final String path) {
		try {
			final int rom[] = ReadRom.readRom(path);
			int cgbFlag = rom[0x143];
			
			if ((cgbFlag & 0x80) != 0) {
				// Game Boy Color
				cores = new EmulatorCore[1];
				cores[0] = new EmulatorCoreCGB(rom, videoReciever);

			} else {
				// Game Boy
				cores = new EmulatorCore[1];
				cores[0] = new EmulatorCoreGB(rom, videoReciever);
			}
			
			reset();

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void reset() {
		running = false;
		for ( int i = 0; i < cores.length; i++ ) {
			cores[i].reset();
		}
		p1keys = 0xFF;
		p2keys = 0xFF;
	}
	
	public void run() {
		running = true;
		while ( running ) {
			for ( int i = 0; i < cores.length; i++ ) {
				cores[i].playerInput(p1keys);
				cores[i].run( GB_CYCLES_PER_FRAME );
			}
		}
	}

	/**
	 * Pauses the emulator.
	 */
	public void stop() {
		running = false;
		for ( int i = 0; i < cores.length; i++ ) {
			cores[i].stop();
		}
	}

	/** {@inheritDoc} */
	public void saveState(FileOutputStream fos) throws IOException {
		for ( int i = 0; i < cores.length; i++ ) {
			cores[i].saveState(fos);
		}
	}

	/** {@inheritDoc} */
	public void readState(FileInputStream fis) throws IOException {
		for ( int i = 0; i < cores.length; i++ ) {
			cores[i].readState(fis);
		}
	}
	
	public void setKey(Keys key, boolean pressed, Player player) {
		int bitmask;
		
		switch (key) {
		case UP: bitmask = 0x04; break;
		case DOWN: bitmask = 0x08; break;
		case LEFT: bitmask = 0x02; break;
		case RIGHT: bitmask = 0x01; break;
		case A: bitmask = 0x10; break;
		case B: bitmask = 0x20; break;
		case START: bitmask = 0x80; break;
		case SELECT: bitmask = 0x40; break;
		default: bitmask = 0; break;
		}
		
		switch (player) {
		case PLAYER1:
			if ( !pressed ) {
				p1keys |= bitmask;
			} else {
				p1keys &= 0xFF - bitmask;
			}
			break;
		case PLAYER2:
			if ( !pressed ) {
				p2keys |= bitmask;
			} else {
				p2keys &= 0xFF - bitmask;
			}
			break;
		}
		
		cores[0].playerInput(p1keys);
	}

	public void disableAudio() {
		cores[0].disableAudio();
	}

	public boolean isAudioEnabled() {
		return cores[0].isAudioEnabled();
	}

	public void enableAudio() {
		cores[0].enableAudio();
	}

	public int getVolume() {
		return cores[0].getVolume();
	}

	public void setVolume(int vol) {
		cores[0].setVolume(vol);
	}
}
