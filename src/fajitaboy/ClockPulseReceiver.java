package fajitaboy;


/**
 * @author Tobias Svensson
 * 
 *  This interface enables a device to receive messages from the oscillator.
 */


public interface ClockPulseReceiver {
	
	/**
     * @param message
     *            A message to send to the reciever. Could be
     *            a constant or the current clock pulse.
     * 
     */
	public int oscillatorMessage(int message);
}
