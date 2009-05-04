package fajitaboy.gb.memory;

import fajitaboy.gb.StateMachine;

public interface MemoryBankController extends MemoryInterface, StateMachine {
	public MemoryComponent getEram();
}
