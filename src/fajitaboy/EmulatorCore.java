package fajitaboy;

import fajitaboy.gb.StateMachine;

/**
 * This is an interface for emulator cores. The Emulator class has
 * the ability to instantiate and run several cores at the same time.
 * 
 * An emulator core receives input data the form of the cartridge and
 * player input, and generates output data in the form of video and
 * audio.
 * 
 * @author Tobias
 */

public interface EmulatorCore extends StateMachine {
	
	/**
	 * Run the emulator for a set amount of cycles.
	 * @param cycles Cycles to proceed emulator
	 */
	public void run(int cycles);
	
	/**
	 * Momentarily halts the core. To resume after halt, call run
	 * with any value (0 recommended)
	 */
	public void stop();
	
	/**
	 * Specify key input.
	 * @param keys Key input
	 */
	public void playerInput(int keys);
	
	/**
	 * Reset core.
	 */
	public void reset();
	
	/**
	 * Specifies the VideoReciever class that will receive the
	 * generated video data.
	 * @param videoReciever VideoReceiver class
	 */
	public void setVideoReciever(VideoReciever videoReciever);
	
	public void disableAudio();
	public boolean isAudioEnabled();
	public void enableAudio();
	public int getVolume();
	public void setVolume(int vol);
}
