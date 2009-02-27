/*
Debug klass för CPU eftersom AddressBus inte täcker alla minnesplatser ännu.
*/
public class DebugAddressBus implements MemoryInterface {
	private MemoryInterface[] module=new MemoryInterface[0x10000]; //Size of memory address space
	
	//All modules and their address space must be declared here
	private InterruptRegister interruptRegister;
	
	//Ramobjektet finns inte längre, se AddressBus.java ramHigh, ramLow
	// adam & johan
	/*
	private Ram ram;
	private int ramStart=0xC000;
	private int ramEnd=0xE000;
	*/
	
	private Cartridge cartridge;
	private int cartridgeStart=0x0000;
	private int cartridgeEnd=0x8000;
	
	private Vram vram;
	private int vramStart=0x8000;
	private int vramEnd=0xA000;
	
	private Oam oam;
	private int oamStart=0xFE00;
	private int oamEnd=0xFEA0;
	
	private Hram hram;
	private int hramStart=0xFF80;
	private int hramEnd=0xFFFF;
	
	DebugAddressBus(String romPath){
		//interruptRegister = new InterruptRegister();
		//module[0xFFFF]=interruptRegister;
		
		
		//All modules must be initialized here
		// ram=new Ram(ramStart, ramEnd); se ramlow / ramhigh i addressbus
		//initialize(ram, ramStart, ramEnd);
		
		vram=new Vram(vramStart, vramEnd);
		initalize(vram, vramStart, vramEnd);
		
		hram=new Hram(hramStart, hramEnd);
		initalize(hram, hramStart, hramEnd);
		
		oam=new Oam(oamStart, oamEnd);
		initalize(oam, oamStart, oamEnd);
		
		cartridge=new Cartridge(cartridgeStart, romPath);
		initalize(cartridge, cartridgeStart, cartridgeEnd);
		
		//hack to cover null memory
		NullMemory nm = new NullMemory();
		for(int i=0; i<0x10000;i++)	{
			if(module[i] == null)
				module[i] = nm;
		}
		
	
	}
	
	private void initalize(MemoryInterface object, int start, int end){
		for(int i=start; i<end; i++){
			module[i]=object;
		}
	}
	
	public int read(int address) {
		if(address < 0 || address > module.length)
			throw new ArrayIndexOutOfBoundsException("Addressbus.java");
		//System.err.println("NullMemory: read: "+Integer.toHexString(module[address].read(address))+" "+Integer.toHexString(address));
		return module[address].read(address);
	}

 	public void write(int address, int data) {
 		if(address < 0 || address > module.length)
			throw new ArrayIndexOutOfBoundsException("AddressBus.java");
		module[address].write(address, data);
	}
 	
 	class NullMemory implements MemoryInterface {
 		private int m[] = new int[0x10000];
		
 		public int read(int address) {
			//System.out.println("NullMemory: read: "+Integer.toHexString(m[address])+" "+Integer.toHexString(address));
			return m[address];
		}

		public void write(int address, int data) {
			m[address] = data;
			//System.out.println("NullMemory: write: "+Integer.toHexString(m[address])+" "+Integer.toHexString(address)+" "+Integer.toHexString(data));
		}
 		
 	}

}
