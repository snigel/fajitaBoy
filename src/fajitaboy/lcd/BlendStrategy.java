package fajitaboy.lcd;

/**
 * Defines a blending strategy to use.
 * @author arvidj
 *
 */
public interface BlendStrategy {
	/**
	 * Given a background color a, and a new color b, this method returns a blending of the two.
	 * @param a background color
	 * @param b new color
	 * @return a blend of the two colors.
	 */
	public int blend(int a, int b);
}
