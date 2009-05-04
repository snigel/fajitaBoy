package fajitaboy.gb;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public interface StateMachine {
	/**
	 * Writes the object's state to the given file stream.
	 * @param os FileOutputStream to a file to write to 
	 */
	public void saveState( FileOutputStream os ) throws IOException;
	
	/**
	 * Reads the object's state from the given file stream.
	 * @param is FileInputStream to read from
	 */
	public void readState( FileInputStream is ) throws IOException;
}
