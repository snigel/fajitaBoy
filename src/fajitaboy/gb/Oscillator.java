package fajitaboy.gb;

import static fajitaboy.constants.AudioConstants.*;
import static fajitaboy.constants.EmulationConstants.*;
import static fajitaboy.constants.AddressConstants.*;
import static fajitaboy.constants.HardwareConstants.*;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.concurrent.Semaphore;

import javax.sound.sampled.LineUnavailableException;

import fajitaboy.AudioReciever;
import fajitaboy.FileIOStreamHelper;
import fajitaboy.VideoReciever;
import fajitaboy.gb.audio.SoundHandler;
import fajitaboy.gb.lcd.LCD;
import fajitaboy.gb.memory.AddressBus;

/**
 * Oscillator emulates the clock frequency of the Game Boy and tells different
 * devices to do different things at different clock cycles.
 * @author Tobias S
 */
public class Oscillator implements StateMachine {

    private boolean running;

    /**
     * Next cycle to stop running core
     */
    private long nextHaltCycle;
    
    /**
     * Next cycle to update video and audio
     */
    private long nextUpdateCycle;

    /**
     * Cycles since Oscillator initialization.
     */
    protected long cycles;

    /**
     * Pointer to CPU instance.
     */
    protected Cpu cpu;

    /**
     * Pointer to MemoryInterface instance.
     */
    protected AddressBus ram;

    /**
     * Pointer to LCD instance.
     */
    protected LCD lcd;

    /**
     * Pointer to Timer instance.
     */
    protected Timer timer;

    /**
     * Pointer to a class with the proper interface to draw the GB pixels to screen.
     */
    protected VideoReciever videoReciever;

    /**
     * Audio handler.
     */
    protected SoundHandler soundHandler;

    /**
     * Audio is disabled by default.
     */
    protected boolean audioEnabled;

    /**
     * Sample rate for audio, default = 44100.
     */
    private int sampleRate = 44100;

    /**
     * Sound samples per frame... or something?
     */
    private int samples = 735;

    /**
     * Sound volume.
     */
    private int volume = 100;

    protected Oscillator() {}

    public Oscillator(Cpu cpu, AddressBus ram) {
    	this(cpu, ram, null, null);
    }
    
    /**
     * Creates a new Oscillator with default values.
     * @param cpu
     *            Pointer to CPU instance.
     * @param ram
     *            Pointer to MemoryInterface instance.
     */
    public Oscillator(Cpu cpu, AddressBus ram, VideoReciever videoReciever, AudioReciever audioReciever) {
    	this.cpu = cpu;
        this.ram = ram;
        this.videoReciever = videoReciever;
        
        lcd = new LCD(ram);
        timer = new Timer();

        audioEnabled = true;
		soundHandler = new SoundHandler(ram, AUDIO_SAMPLERATE, AUDIO_SAMPLES, audioReciever);
			
        reset();
    }

    /**
     * Resets the Oscillator to default values.
     */
    public void reset() {
        cycles = 0;
        nextHaltCycle = GB_CYCLES_PER_FRAME;
        nextUpdateCycle = 0;
        running = false;
        timer.reset();
        
        cpu.reset();
        ram.reset();
        soundHandler.reset();
    }

    /**
     * Generates one frame of Audio.
     */
    private void generateAudio() {
        if ( !audioEnabled )
            return;

        soundHandler.generateTone();
    }

    /**
     * Step the Oscillator once. The Oscillator causes the CPU to step once, and
     * sends messages to other components at certain cycles.
     * @return Returns step time in cycles.
     */
    public int step() {
        // Step CPU
        int cycleInc = cpu.step();
        cycles += cycleInc;

        timer.update(cycleInc, ram);

        // Update LCD
        // hack so that LCD never will have to care about speedSwitch (ugly or beautiful?).
        lcd.updateLCD(cycleInc);
        if ( videoReciever != null && lcd.newScreenAvailable() ) {
        	videoReciever.transmitVideo(lcd.getPixels());
        }
        return cycleInc;
    }

    public LCD getLCD() {
        return lcd;
    }

    /**
     * Starts the execution. Create a new thread and call start() to
     * make this method run in a separate thread.
     */
    public final void run(int runCycles) {
        running = true;
        nextHaltCycle += runCycles;
        
        while (running) {
            step();

            if (cycles > nextUpdateCycle) {
                generateAudio();
                
                nextUpdateCycle += GB_CYCLES_PER_FRAME;
            }
            
            if ( cycles > nextHaltCycle ) {
            	running = false;
            }
        }
    }

    /**
     * Method to call to stop the run() loop.
     * Lock until the run() loop has terminated.
     */
    public final void stop() {
        running = false;
        
        

    }

    /**
     * To know if run() is running.
     * @return true if the Oscillator is running, otherwise false.
     */
    public final boolean isRunning() {
        return running;
    }

    public void setSoundHandler(SoundHandler soundHandler) {
		this.soundHandler = soundHandler;
	}

	public SoundHandler getSoundHandler() {
		return soundHandler;
	}
	
	public void setKeys(int keys) {
		ram.getJoyPad().setKeys(keys);
	}
	
	public void setGameLinkCable(GameLinkCable glc) {
		ram.getIO().setGameLinkCable(glc);
	}
	
	public int readSerial() {
		return ram.read(ADDRESS_SB);
	}

	public void writeSerial(int data) {
		ram.write(ADDRESS_SB, data);
		
		// Disable Transfer Start Flag in Serial Control
		int sc = ram.read(ADDRESS_SC) & 0x7F;
		ram.write(ADDRESS_SC, sc);
		
		// Trigger Serial interrupt
		ram.write(ADDRESS_IF, ram.read(ADDRESS_IF) | 0x08);
	}
	
	public void setSerialHost(boolean host) {
		if ( host ) {
			// Disable Shift Clock Flag in Serial Control
			int sc = ram.read(ADDRESS_SC) | 0x01;
			ram.write(ADDRESS_SC, sc);
		} else {
			// Disable Shift Clock Flag in Serial Control
			int sc = ram.read(ADDRESS_SC) & 0xFE;
			ram.write(ADDRESS_SC, sc);
		}
	}

	/**
     * {@inheritDoc}
     */
    public void saveState( FileOutputStream os ) throws IOException {
        FileIOStreamHelper.writeData(os, cycles, 8);
        FileIOStreamHelper.writeData(os, nextHaltCycle, 8);
        FileIOStreamHelper.writeData(os, nextUpdateCycle, 8);
        FileIOStreamHelper.writeBoolean(os, audioEnabled);
        FileIOStreamHelper.writeData(os, sampleRate, 4);
        FileIOStreamHelper.writeData(os, samples, 4);

        ram.saveState(os);
        timer.saveState(os);
        cpu.saveState(os);
        lcd.saveState(os);
        soundHandler.saveState(os);
    }

    /**
     * {@inheritDoc}
     */
    public void readState( FileInputStream is ) throws IOException {
        cycles = FileIOStreamHelper.readData(is, 8);
        nextHaltCycle = FileIOStreamHelper.readData(is, 8);
        nextUpdateCycle = FileIOStreamHelper.readData(is, 8);
        audioEnabled = FileIOStreamHelper.readBoolean(is);
        sampleRate = (int) FileIOStreamHelper.readData(is, 4);
        samples = (int) FileIOStreamHelper.readData(is, 4);

        ram.readState(is);
        timer.readState(is);
        cpu.readState(is);
        lcd.readState(is);
        soundHandler.readState(is);
    }
}
