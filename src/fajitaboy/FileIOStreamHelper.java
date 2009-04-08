package fajitaboy;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public final class FileIOStreamHelper {
	
	/**
	 * Private constructor to prevent instantiation
	 */
	private FileIOStreamHelper() {
	}
	
	public static void writeBoolean( FileOutputStream os, boolean data ) throws IOException {
		if ( data )
			os.write(1);
		else
			os.write(0);
	}
	
	public static void writeData( FileOutputStream os, long data, int bytes ) throws IOException {
		while (bytes > 0) {
			os.write((int)(data & 0xff));
			data >>>= 8;
			bytes--;
		}
	}
	
	public static boolean readBoolean( FileInputStream is ) throws IOException {
		return (is.read() != 0);
	}
	
	public static long readData( FileInputStream is, int bytes ) throws IOException {
		long data = 0;
		int bytesRead = 0;
		while ( bytes > 0 ) {
			data += is.read() << bytesRead;
			bytes--;
			bytesRead += 8;
		}
		return data;
	}
}
