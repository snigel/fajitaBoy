package fajitaboy.gb;

import static fajitaboy.constants.HardwareConstants.*;
import static fajitaboy.constants.EmulationConstants.*;
import static fajitaboy.constants.AudioConstants.*;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.concurrent.Semaphore;

import javax.sound.sampled.LineUnavailableException;

import fajitaboy.VideoReciever;
import fajitaboy.FileIOStreamHelper;
import fajitaboy.gb.audio.SoundHandler;
import fajitaboy.gb.lcd.LCD;
import fajitaboy.gb.memory.AddressBus;
import fajitaboy.gb.memory.MemoryInterface;

/**
 * Oscillator emulates the clock frequency of the Game Boy and tells different
 * devices to do different things at different clock cycles.
 * @author Tobias S
 */
public class Oscillator implements StateMachine {

    private boolean running;
    
    
    /**
     * Semaphore to ask for permission to run oscillator.
     */
    Semaphore runningMutex = new Semaphore(1);

    /**
     * Next cycle to stop and wait if going too fast
     */
    private long nextHaltCycle;

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
    protected MemoryInterface ram;

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
    	this(cpu, ram, null);
    }
    
    /**
     * Creates a new Oscillator with default values.
     * @param cpu
     *            Pointer to CPU instance.
     * @param ram
     *            Pointer to MemoryInterface instance.
     */
    public Oscillator(Cpu cpu, AddressBus ram, VideoReciever videoReciever) {
    	this.cpu = cpu;
        this.ram = ram;
        this.videoReciever = videoReciever;
        
        lcd = new LCD(ram);
        timer = new Timer();
        try {
			soundHandler = new SoundHandler(ram, AUDIO_SAMPLERATE, AUDIO_SAMPLES);
		} catch (LineUnavailableException e) {
			e.printStackTrace();
			soundHandler = null;
			audioEnabled = false;
		}
        
        reset();
    }

    /**
     * Resets the Oscillator to default values.
     */
    public void reset() {
        cycles = 0;
        nextHaltCycle = GB_CYCLES_PER_FRAME;
        running = false;
        timer.reset();
        resetAudio();
        enableAudio();
    }

    /**
     * Initializes the Audio module.
     */
    public void resetAudio() {
        try {
            soundHandler = new SoundHandler( (AddressBus)ram, sampleRate, samples );
        } catch ( LineUnavailableException e ) {
            System.out.println("Error when initializing audio: " + e);
            audioEnabled = false;
        }
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
    	try {
			runningMutex.acquire();
		} catch (InterruptedException e1) {}
        running = true;
        long sleepTime;
        long nextUpdate = System.nanoTime();
        int frameSkipCount = 0;
        nextHaltCycle += runCycles;
        
        while (running) {
            step();

            if (cycles > nextHaltCycle) {
                generateAudio();
                nextUpdate += GB_NANOS_PER_FRAME;
                sleepTime = nextUpdate - System.nanoTime();

                // Sleep if running too fast
                if (sleepTime >= 0) {
                    lcd.disableFrameSkip();
                    frameSkipCount = 0;
                    try {
                        Thread.sleep(sleepTime / 1000000);
                    } catch (InterruptedException e) {}
                } else if (frameSkipCount >= EMU_MAX_FRAMESKIP) {
                    lcd.disableFrameSkip();
                    frameSkipCount = 0;
                    nextUpdate = System.nanoTime();
                } else {
                    lcd.enableFrameSkip();
                    frameSkipCount++;
                }
                running = false;
            }
        }
        runningMutex.release();
    }

    /**
     * Method to call to stop the run() loop.
     * Lock until the run() loop has terminated.
     */
    public final void stop() {
        running = false;
        
        // lock until running loop has stopped.
        try {
 	        runningMutex.acquire();
	        runningMutex.release();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

    }

    /**
     * To know if run() is running.
     * @return true if the Oscillator is running, otherwise false.
     */
    public final boolean isRunning() {
        return running;
    }
    
    public void enableAudio() {
        if ( audioEnabled == false ) {
            audioEnabled = true;
            resetAudio();
            setVolume(volume);
        }
    }

    public void disableAudio() {
        if ( audioEnabled ) {
            soundHandler.close();
        }
        audioEnabled = false;
    }

    /**
     * Sets emulator volume.
     *
     * @param volume
     */
    public final void setVolume(int vol) {
        volume = Math.max(0, vol);
        volume = Math.min(100, volume);
        soundHandler.setVolume(volume);
    }

    /**
     * Returns emulator volume.
     * @return int
     */
    public final int getVolume() {
        return volume;
    }
    

    public void setSoundHandler(SoundHandler soundHandler) {
		this.soundHandler = soundHandler;
	}

	public SoundHandler getSoundHandler() {
		return soundHandler;
	}

	/**
     * {@inheritDoc}
     */
    public void saveState( FileOutputStream os ) throws IOException {
        FileIOStreamHelper.writeData(os, cycles, 8);
        FileIOStreamHelper.writeData(os, nextHaltCycle, 8);

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
        audioEnabled = FileIOStreamHelper.readBoolean(is);
        sampleRate = (int) FileIOStreamHelper.readData(is, 4);
        samples = (int) FileIOStreamHelper.readData(is, 4);

        timer.readState(is);
        cpu.readState(is);
        lcd.readState(is);
        soundHandler.readState(is);
    }

    /**
     * Getter for audioEnable, ie if the sound is enabled or not.
     *
     * @return the audioEnabled
     */
    public final boolean isAudioEnabled() {
        return audioEnabled;
    }
}
