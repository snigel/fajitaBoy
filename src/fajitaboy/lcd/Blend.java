package fajitaboy.lcd;

/**
 * Regular blending. Always overwrites background color.
 * @author arvidj
 *
 */
public class Blend implements BlendStrategy {
	public int blend(int a, int b) {
		return b;
	}
}
