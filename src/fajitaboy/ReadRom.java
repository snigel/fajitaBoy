package fajitaboy;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public final class ReadRom {

	/**
	 * Private constructor to prevent instantiation
	 */
	private ReadRom() {
	}
	
	/**
	 * Reads ROM data from the given file
	 * @param romPath Path to ROM file
	 * @return ROM data
	 */
	public static int[] readRom(final String romPath) {
        try {
            // Read ROM data from file
        	int[] rom;
            
            DataInputStream dis;
            FileInputStream fis = new FileInputStream(romPath);
            ZipInputStream zis;
            
            if (romPath.substring(romPath.length() - 3, romPath.length())
                    .equals("zip")) {
                zis = new ZipInputStream(fis);
                ZipEntry entry = zis.getNextEntry();
                rom = new int[(int) entry.getSize()];
                dis = new DataInputStream(zis);
                for (int i = 0; i < rom.length; i++) {
                    rom[i] = dis.readUnsignedByte();
                }
                zis.close();
            } else {
                File romFile = new File(romPath);
                rom = new int[(int) romFile.length()];
                System.out.println("romfile length " + romFile.length());
              
                dis = new DataInputStream(fis);
                for (int i = 0; i < rom.length; i++) {
                    rom[i] = dis.readUnsignedByte();
                }
                dis.close();
            }
            fis.close();
            
            return rom;

        } catch (Exception e) {
            System.out.println("Exception: " + e);
            return null;
        }
    }
	
}
