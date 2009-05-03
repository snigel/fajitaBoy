package fajitaboy;

import fajitaboy.lcd.ColorLCD;
import fajitaboy.lcd.LCD;
import fajitaboy.memory.AddressBus;
import fajitaboy.memory.AddressBusCgb;

/**
 * Oscillator in CGB mode.
 */
public class OscillatorCgb extends Oscillator {
    
    /**
     * Creates a new OscillatorCgb object.
     * @param cpu
     *            Pointer to CPU instance.
     * @param ram
     *            Pointer to MemoryInterface instance.
     * @param ss
     *            Pointer to SpeedSwitch instance.
     */
    public OscillatorCgb(Cpu cpu, AddressBusCgb ram, SpeedSwitch ss) {
        super(cpu, ram, new ColorLCD(ram), new Timer());   
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
    public OscillatorCgb(Cpu cpu, AddressBusCgb ram, SpeedSwitch ss, DrawsGameboyScreen dgs,
            boolean enableAudio) {
        super(cpu, ram, new ColorLCD(ram), new Timer());
        this.dgs = dgs;
        if ( enableAudio )
            enableAudio();
        speedSwitch = ss;
    }
}
