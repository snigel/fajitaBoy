/**
 * Represents the graphic ram, also known as VRAM.
 *
 * @author Adam Hulin, Johan Gustafsson
 */
public class Vram extends MemoryComponent {

    
    /**
     * @param start
     *            , address representing where vram begins in memory space
     * @param end
     *            , address representing where vram ends in memory space These
     *            two values are used for creating the right size of the vram
     *            array and for setting the offset value
     */
    Vram(final int start, final int end) {
        super(start, end);
    }
}
