package fajitaboy.mbc;

import fajitaboy.Cartridge;
import static fajitaboy.constants.CartridgeConstants.*;

/**
 * Used when there is no Memory Bank Controller.
 * @author Tobias Svensson
 *
 */
public class RomOnly implements MBCInterface {

	int[] romBankHi;
	int[] romBankLo;
	Cartridge cart;
	
	
	public RomOnly(Cartridge cart, int[] cartBytes ) {
		this.cart = cart;
		romBankLo = new int[CART_BANKSIZE_ROM];
		romBankHi = new int[CART_BANKSIZE_ROM];
		
		int j = 0x4000;
		for ( int i = 0; i < CART_BANKSIZE_ROM; i++ ) {
			romBankLo[i] = cartBytes[i];
			romBankHi[i] = cartBytes[j];
			j++;
		}
		
		cart.setRomBankLo(romBankLo);
		cart.setRomBankHi(romBankHi);
		cart.setRamBank(null);
	}
	
	/**
	 * {@inheritDoc}
	 */
	public void reset() {

	}

	/**
	 * {@inheritDoc}
	 */
	public void setRamBank() {
		cart.setRamBank(null);
	}

	/**
	 * {@inheritDoc}
	 */
	public void setRomBank() {
		cart.setRomBankHi(romBankHi);
	}

	/**
	 * {@inheritDoc}
	 */
	public void write(int address, int data) {
		// Do Nothing
	}

}
