package fajitaboy.gbc;

import fajitaboy.DrawsGameboyScreen;
import fajitaboy.SpeedSwitch;
import fajitaboy.gb.Cpu;
import fajitaboy.gb.Oscillator;
import fajitaboy.gb.Timer;
import fajitaboy.gb.lcd.LCD;
import fajitaboy.gb.memory.AddressBus;
import fajitaboy.gbc.lcd.CGB_LCD;
import fajitaboy.gbc.memory.CGB_AddressBus;

/**
 * Oscillator in CGB mode.
 */
public class CGB_Oscillator extends Oscillator {
    
	/**
     * To know if the speed is normal or double.
     * The speed should always be normal here but is here to reduce duplicate
     * code in OscillatorCgb.
     */
    protected SpeedSwitch speedSwitch;
	
	/**
     * Creates a new OscillatorCgb object.
     * @param cpu
     *            Pointer to CPU instance.
     * @param ram
     *            Pointer to MemoryInterface instance.
     * @param ss
     *            Pointer to SpeedSwitch instance.
     */
    public CGB_Oscillator(Cpu cpu, CGB_AddressBus ram, SpeedSwitch ss) {
        this.cpu = cpu;
        this.ram = ram;
        this.lcd = new CGB_LCD(ram);
        this.timer = new Timer();
        reset();  
        speedSwitch = ss;
    }

    /**
     * Creates a new Oscillator with a screen rendering instance and possibility to enable audio.
     * @param cpu
     *            Pointer to CPU instance.
     * @param ram
     *            Pointer to MemoryInterface instance.
     * @param ss
     *            Pointer to SpeedSwitch instance.
     * @param dgs
     *            Pointer to DrawsGameboyScreen instance.
     */
    public CGB_Oscillator(Cpu cpu, CGB_AddressBus ram, SpeedSwitch ss, DrawsGameboyScreen dgs,
            boolean enableAudio) {
        this.cpu = cpu;
        this.ram = ram;
        this.lcd = new CGB_LCD(ram);
        this.timer = new Timer();
        speedSwitch = ss;
        reset();
        this.dgs = dgs;
        if ( enableAudio )
            enableAudio();
    }
    
    /**
     * {@inheritDoc}
     */
    public void reset() {
    	super.reset();
    	speedSwitch.reset();
    }

    /**
     * {@inheritDoc}
     */
    public int step() {
        // Step CPU
        int cycleInc = cpu.step();
        if ( speedSwitch.getSpeed() == 2)
        	cycleInc >>= 1;
        cycles += cycleInc;

        timer.update(cycleInc, ram);

        // Update LCD
        // hack so that LCD never will have to care about speedSwitch (ugly or beautiful?).
        lcd.updateLCD(cycleInc);
        if ( dgs != null && lcd.newScreenAvailable() ) {
            dgs.drawGameboyScreen(lcd.getScreen());
        }

        return cycleInc;
    }
}
