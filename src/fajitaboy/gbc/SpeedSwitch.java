package fajitaboy.gbc;

/**
 * The SpeedSwitch should be used to know if which speed mode that is being used.
 */
public class SpeedSwitch {
    
    private int speed;
    
    /**
     * Creates a new SpeedSwitch in Normal Speed Mode.
     */
    public SpeedSwitch() {
        speed = 1;
    }
    
    /**
     * Change from Normal Speed Mode to Double Speed Mode or vice versa.
     */
    public void toggleSpeed() {
        if(speed == 1) {
            speed = 2;
        } else {
            speed = 1;
        }
    }
    
    /**
     * Returns the speed mode
     * @return the current speed mode. 1 = normal, 2 = double speed.
     */
    public int getSpeed() {
        return speed;
    }
    /**
     * Set the Speed Mode to normal speed (1).
     */
    public void reset() {
        speed = 1;
    }

}
