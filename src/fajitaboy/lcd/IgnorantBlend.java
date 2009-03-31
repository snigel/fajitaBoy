package fajitaboy.lcd;

/**
 * IgnorantBlend will ignore a specific colors, and blend all others regularly. 
 * @author arvidj
 *
 */
public class IgnorantBlend implements BlendStrategy {

	private int ign;
	
	public IgnorantBlend(int toIgnore) {
		ign = toIgnore;
	}
	
	@Override
	public int blend(int a, int b) {
		return b == ign ? a : b;  
	}

}
