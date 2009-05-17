package fajitaboy;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import fajitaboy.gb.GameLinkCable;
import fajitaboy.gb.StateMachine;
import fajitaboy.gb.audio.SoundReciever;

import static fajitaboy.constants.HardwareConstants.*;
import static fajitaboy.constants.AudioConstants.*;

/**
 * Encapsulates the emulator.
 * 
 * @author Marcus, Peter, Tobias
 */
public class Emulator implements Runnable, StateMachine {

	// Handles keys for Player 1 and Player 2
	public enum Player {PLAYER1, PLAYER2};
	public enum Keys {UP, DOWN, LEFT, RIGHT, A, B, START, SELECT };
	int p1keys;
	int p2keys;
	
	/** Pointer to a class that renders video for Player 1. */
	VideoReciever videoReciever1;
	
	/** Pointer to a class that renders video for Player 2. */
	VideoReciever videoReciever2;
	
	/** Pointer to a class that renders sound. */
	SoundReciever soundReciever;
	
	/** Points to a GameLinkCable class if multiplayer is enabled. */
	GameLinkCable gameLinkCable;
	
	/** True if the emulator is currently running the emulator cores. */
	boolean running;
	
	/** True if the emulator is a multiplayer emulator */
	boolean multiplayer;
	
	/** Player 1 emulator core */
	EmulatorCore core1;
	
	/** Player 2 emulator core */
	EmulatorCore core2;
	
	/** Cycles to run per step */
	int cycleRate;
	
	/**
	 * Standard constructor
	 * @param path ROM path
	 */
	Emulator(final String path) {
		this(path, null);
	}
	
	/**
	 * Initializes a single player emulator.
	 * @param path ROM path
	 */
	Emulator(final String path, VideoReciever vr ) {
		multiplayer = false;
		videoReciever1 = vr;
		loadRom(path);
	}
	
	/**
	 * Initialises a multiplayer emulator.
	 * @param path ROM path
	 */
	Emulator(final String path, VideoReciever vr1, VideoReciever vr2 ) {
		multiplayer = true;
		videoReciever1 = vr1;
		videoReciever2 = vr2;
		loadRom(path);
	}
	
	public void loadRom(final String path) {
		try {
			final int rom[] = ReadRom.readRom(path);
			int cgbFlag = rom[0x143];
			
			soundReciever = new SoundReciever(AUDIO_SAMPLERATE);
			
			if ( multiplayer == false ) {
			
				cycleRate = GB_CYCLES_PER_FRAME;
				
				if ((cgbFlag & 0x80) != 0) {
					// Game Boy Color
					core1 = new EmulatorCoreCGB(rom, videoReciever1, soundReciever);
				} else {
					// Game Boy
					core1 = new EmulatorCoreGB(rom, videoReciever1, soundReciever);
				}
			
			} else {
				
				if ((cgbFlag & 0x80) != 0) {
					// Game Boy Color
					EmulatorCoreCGB c1 = new EmulatorCoreCGB(rom, videoReciever1, soundReciever);
					EmulatorCoreCGB c2 = new EmulatorCoreCGB(rom.clone(), videoReciever2, null);
					gameLinkCable = new GameLinkCable(c1, c2);
					core1 = c1;
					core2 = c2;
					cycleRate = GLC_CYCLES_PER_TRANSFER_FAST;
	
				} else {
					// Game Boy
					EmulatorCoreGB c1 = new EmulatorCoreGB(rom, videoReciever1, soundReciever);
					EmulatorCoreGB c2 = new EmulatorCoreGB(rom.clone(), videoReciever2, null);
					gameLinkCable = new GameLinkCable(c1, c2);
					core1 = c1;
					core2 = c2;
					cycleRate = GLC_CYCLES_PER_TRANSFER;
				}
			}
			
			reset();

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void reset() {
		running = false;
		if ( core1 != null )
			core1.reset();
		if ( core2 != null )
			core2.reset();
		p1keys = 0xFF;
		p2keys = 0xFF;
	}
	
	public void run() {
		running = true;
		if ( multiplayer == false ) {
			while ( running ) {
				core1.run(cycleRate);
			}
		} else {
			while ( running ) {
				core1.run(cycleRate);
				core2.run(cycleRate);
				gameLinkCable.performTransfer();
			}
		}
	}

	/**
	 * Pauses the emulator.
	 */
	public void stop() {
		running = false;
		if ( core1 != null )
			core1.stop();
		if ( core2 != null )
			core2.stop();
	}

	/** {@inheritDoc} */
	public void saveState(FileOutputStream fos) throws IOException {
		if ( core1 != null )
			core1.saveState(fos);
		if ( core2 != null )
			core2.saveState(fos);
	}

	/** {@inheritDoc} */
	public void readState(FileInputStream fis) throws IOException {
		if ( core1 != null )
			core1.readState(fis);
		if ( core2 != null )
			core2.readState(fis);
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
			core1.playerInput(p1keys);
			break;
		case PLAYER2:
			if ( !pressed ) {
				p2keys |= bitmask;
			} else {
				p2keys &= 0xFF - bitmask;
			}
			if ( core2 != null )
				core2.playerInput(p2keys);
			break;
		}
	}

	public void disableAudio() {
		soundReciever.disableAudio();
	}

	public boolean isAudioEnabled() {
		return soundReciever.isAudioEnabled();
	}

	public void enableAudio() {
		soundReciever.enableAudio();
	}

	public int getVolume() {
		return soundReciever.getVolume();
	}

	public void setVolume(int vol) {
		soundReciever.setVolume(vol);
	}
}
