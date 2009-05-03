package fajitaboy;

import static fajitaboy.constants.AddressConstants.*;

import fajitaboy.memory.MemoryInterface;

/**
 * Cpu in CGB mode·.
 */
public class CpuCgb extends Cpu {
    
    /**
     * SpeedSwitch object. Used to change the speed.
     */
    private SpeedSwitch speedSwitch;
    
    /**
     * 
     * @param addressbus
     * @param ss SpeedSwitch object that to use if the speed should be changed.
     */
    public CpuCgb(MemoryInterface addressbus, SpeedSwitch ss) {
        super(addressbus);
        speedSwitch = ss;
    }

    /**
     * {@inheritDoc}
     */
    public void reset() {
        super.reset();
        // in CGB hardware a starts with the value 0x11 instead of 0x01.
        a = 0x11;
        if (speedSwitch != null) {
            speedSwitch.reset();
        }
    }
    /**
     * Called each time the STOP instruction is executed.
     * If the Prepare Speed Switch is activated the speed is changed
     */
    protected void stopActions() {
        int data = ram.read(SPEED_SWITCH_REGISTER);
        
        if ((data & 0x01) == 1 ) {
            speedSwitch.toggleSpeed();
            
            if (speedSwitch.getSpeed() == 2) {
                ram.write(0x80, SPEED_SWITCH_REGISTER);
            } else {
                ram.write(0x00, SPEED_SWITCH_REGISTER);
            }
        }
    }

}
