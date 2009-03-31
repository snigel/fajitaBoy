package fajitaboy.lcd;

/**
 * Observant blend will only blend colors of a specific color, and ignore all others.
 * @author arvidj
 *
 */
public class ObservantBlend implements BlendStrategy {
	private int obs;
	
	public ObservantBlend(int toObserve) {
		obs = toObserve;
	}

	@Override
	public int blend(int a, int b) {
		return b == obs ? b : a;  
	}

}
