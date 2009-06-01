package fajitaboy.gb;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import fajitaboy.EmulatorCoreGB;
import fajitaboy.FileIOStreamHelper;

public class GameLinkCable implements StateMachine {
	
	EmulatorCoreGB core1;
	EmulatorCoreGB core2;
	
	boolean transfer = false;
	int host;
	
public GameLinkCable() {
	}
	
	public GameLinkCable( EmulatorCoreGB c1, EmulatorCoreGB c2 ) {
		core1 = c1;
		core2 = c2;
		c1.setGameLinkCable( new EntryPoint(1, this) );
		c2.setGameLinkCable( new EntryPoint(2, this) );
		host = 1;
		core1.setSerialHost(true);
		core1.setSerialHost(false);
	}
	
	public void setSerialHost() {
		// Dummy class.....
		// Ugly implementation but... yeah. Can come up with a better solution once this works.
	}
	
	public void setSerialHost(int id) {
		if ( id == 1 ) {
			core2.setSerialHost(false);
		} else {
			core1.setSerialHost(false);
		}
	}
	
	public void enableTransfer() {
		transfer = true;
	}
	
	public void performTransfer() {
		if (transfer == false) {
			return;
		}
		
		int d1 = core1.readSerial();
		int d2 = core2.readSerial();
		core1.writeSerial(d2);
		core2.writeSerial(d1);
		
		transfer = false;
	}
	
	class EntryPoint extends GameLinkCable {
		int id;
		GameLinkCable glc;
		
		public EntryPoint(int id, GameLinkCable glc) {
			this.id = id;
			this.glc = glc;
		}
		
		public void setSerialHost() {
			glc.setSerialHost(id);
		}
		
		public void enableTransfer() {
			glc.enableTransfer();
		}
	}

	@Override
	public void readState(FileInputStream is) throws IOException {
		transfer = FileIOStreamHelper.readBoolean(is);
		host = (int)FileIOStreamHelper.readData(is, 4);
	}

	@Override
	public void saveState(FileOutputStream os) throws IOException {
		FileIOStreamHelper.writeBoolean(os, transfer);
		FileIOStreamHelper.writeData(os, (long)host, 4);
	}
}
