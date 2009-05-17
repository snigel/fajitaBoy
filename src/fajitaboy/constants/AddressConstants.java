package fajitaboy.constants;

public final class AddressConstants {

    public static final int ADDRESS_IE = 0xFFFF;
    public static final int ADDRESS_IF = 0xFF0F;

    public static final int ADDRESS_INT_VBLANK = 0x0040;
    public static final int ADDRESS_INT_LCDSTAT = 0x0048;
    public static final int ADDRESS_INT_TIMER = 0x0050;
    public static final int ADDRESS_INT_SERIAL = 0x0058;
    public static final int ADDRESS_INT_JOYPAD = 0x0060;

    public static final int ADDRESS_DIV = 0xFF04;
    public static final int ADDRESS_TIMA = 0xFF05;
    public static final int ADDRESS_TMA = 0xFF06;
    public static final int ADDRESS_TAC = 0xFF07;

    public static final int ADDRESS_STAT = 0xFF41;
    public static final int ADDRESS_SCY = 0xFF42;
    public static final int ADDRESS_SCX = 0xFF43;
    public static final int ADDRESS_LY = 0xFF44;
    public static final int ADDRESS_LYC = 0xFF45;
    public static final int ADDRESS_WY = 0xFF4A;
    public static final int ADDRESS_WX = 0xFF4B;
    public static final int ADDRESS_DMA = 0xFF46;
    
    public static final int ADDRESS_VRAM_BANK = 0xFF4F;
    public static final int ADDRESS_CGB_FLAG = 0x0143;
    
    public static final int ADDRESS_VRAM_DMA_SOURCE_H = 0xFF51;
    public static final int ADDRESS_VRAM_DMA_SOURCE_L = 0xFF52;
    public static final int ADDRESS_VRAM_DMA_DEST_H = 0xFF53;
    public static final int ADDRESS_VRAM_DMA_DEST_L = 0xFF54;
    public static final int ADDRESS_VRAM_DMA_START = 0xFF55;
    
    public static final int ADDRESS_SVBK = 0xFF70;

    public static final int ADDRESS_JOYPAD = 0xFF00;

    public static final int ADDRESS_CARTRIDGE_TYPE = 0x147;
    public static final int ADDRESS_ROM_SIZE = 0x148;
    public static final int ADDRESS_RAM_SIZE = 0x149;
    
    public static final int ADDRESS_DEBUG_START = 0x0000;
    public static final int ADDRESS_DEBUG_END = 0x10000;
    public static final int ADDRESS_CARTRIDGE_START = 0x0000;
    public static final int ADDRESS_CARTRIDGE_END = 0x8000;
    public static final int ADDRESS_ROMLOW_START = 0x0000;
    public static final int ADDRESS_ROMLOW_END = 0x4000;
    public static final int ADDRESS_ROMHIGH_START = 0x4000;
    public static final int ADDRESS_ROMHIGH_END = 0x8000;

    public static final int ADDRESS_VRAM_START = 0x8000;
    public static final int ADDRESS_TILE_DATA_START = 0x8000;
    public static final int ADDRESS_TILE_DATA_END = 0x9800;
    public static final int ADDRESS_VRAM_END = 0xA000;

    public static final int ADDRESS_ERAM_START = 0xA000;
    public static final int ADDRESS_ERAM_END = 0xC000;
    public static final int ADDRESS_ERAM_4BIT_END = 0xA200;
    public static final int ADDRESS_RAML_START = 0xC000;
    public static final int ADDRESS_RAML_END = 0xD000;
    public static final int ADDRESS_RAMH_START = 0xD000;
    public static final int ADDRESS_RAMH_END = 0xE000;
    public static final int ADDRESS_ECHO_START = 0xE000;
    public static final int ADDRESS_ECHO_END = 0xFE00;
    public static final int ADDRESS_OAM_START = 0xFE00;
    public static final int ADDRESS_OAM_END = 0xFEA0;
    public static final int ADDRESS_AREA51_START = 0xFEA0;
    public static final int ADDRESS_AREA51_END = 0xFF00;
    public static final int ADDRESS_IO_START = 0xFF00;
    public static final int ADDRESS_IO_END = 0xFF80;
    public static final int ADDRESS_HRAM_START = 0xFF80;
    public static final int ADDRESS_HRAM_END = 0xFFFF;
    public static final int ADDRESS_INTERRUPT = 0xFFFF;

    public static final int ADDRESS_NR10 = 0xFF10;
    public static final int ADDRESS_NR11 = 0xFF11;
    public static final int ADDRESS_NR12 = 0xFF12;
    public static final int ADDRESS_NR13 = 0xFF13;
    public static final int ADDRESS_NR14 = 0xFF14;

    public static final int ADDRESS_NR21 = 0xFF16;
    public static final int ADDRESS_NR22 = 0xFF17;
    public static final int ADDRESS_NR23 = 0xFF18;
    public static final int ADDRESS_NR24 = 0xFF19;

    public static final int ADDRESS_NR30 = 0xFF1A;
    public static final int ADDRESS_NR31 = 0xFF1B;
    public static final int ADDRESS_NR32 = 0xFF1C;
    public static final int ADDRESS_NR33 = 0xFF1D;
    public static final int ADDRESS_NR34 = 0xFF1E;

    public static final int ADDRESS_NR41 = 0xFF20;
    public static final int ADDRESS_NR42 = 0xFF21;
    public static final int ADDRESS_NR43 = 0xFF22;
    public static final int ADDRESS_NR44 = 0xFF23;

    public static final int ADDRESS_NR50 = 0xFF24;
    public static final int ADDRESS_NR51 = 0xFF25;
    public static final int ADDRESS_NR52 = 0xFF26;

    public static final int ADDRESS_LCDC = 0xFF40;
    public static final int ADDRESS_SPEED_SWITCH = 0xFF4D;
    public static final int ADDRESS_BGB = 0xFF47;
    public static final int ADDRESS_OBP0 = 0xFF48;
    public static final int ADDRESS_OBP1 = 0xFF49;

    public static final int ADDRESS_PALETTE_BG_DATA = 0xFF47;
    public static final int ADDRESS_PALETTE_SPRITE0_DATA = 0xFF48;
    public static final int ADDRESS_PALETTE_SPRITE1_DATA = 0xFF49;
    
    public static final int ADDRESS_PALETTE_BACKGROUND_INDEX = 0xFF68;
    public static final int ADDRESS_PALETTE_BACKGROUND_DATA = 0xFF69;
        
    public static final int ADDRESS_PALETTE_SPRITE_INDEX = 0xFF6A;
    public static final int ADDRESS_PALETTE_SPRITE_DATA = 0xFF6B;

    //The real status register for the keypad
    public static final int KEYPAD_STATUS_REGISTER = 0xFF00;
    //The direction cross in the secret memory area
    public static final int KEYPAD_CROSS_REGISTER = 0xFEA0;
    //The buttons in the secret memory area
    public static final int KEYPAD_BUTTON_REGISTER = 0xFEA1;

    //dupes for readability during development.
    public static final int ADDRESS_SOUND1_LOW = 0xFF13;
    public static final int ADDRESS_SOUND1_HIGH = 0xFF14;
    public static final int ADDRESS_SOUND2_LOW = 0xFF18;
    public static final int ADDRESS_SOUND2_HIGH = 0xFF19;
    public static final int ADDRESS_SOUND3_LOW = 0xFF1D;
    public static final int ADDRESS_SOUND3_HIGH = 0xFF1E;
    public static final int ADDRESS_SOUND4_LOW = 0xFF13;
    public static final int ADDRESS_SOUND4_HIGH = 0xFF14;
    public static final int ADDRESS_SOUND_CHANNEL_CONTROL = 0xFF24;
    public static final int ADDRESS_SOUND_OUTPUT_SELECT = 0xFF25;
    public static final int ADDRESS_SOUND_ON_OFF= 0xFF26;
    public static final int ADDRESS_SOUND3_WAVEPATTERN_START= 0xFF30;
    public static final int ADDRESS_SOUND3_WAVEPATTERN_END= 0xFF3F;

    public static final int ADDRESS_SPRITE_ATTRIBUTE_TABLE = 0xFE00;
    
    public static final int ADDRESS_SB = 0xFF01;
    public static final int ADDRESS_SC = 0xFF02;

}
