package fajitaboy;

public interface AudioReciever {
	public void transmitAudio(byte[] data);
	public void enableAudio(boolean enable);
}
