package fajitaboy.debugger;

@SuppressWarnings("serial")
public class InterruptedStepException extends Exception {
    private String msg;
    private int cycles;
    
    
    public InterruptedStepException(final String msg, final int cycles) {
        this.msg = msg;
        this.cycles = cycles;
    }

    public String toString() {
        return msg;
    }
    
    public int getCycles() {
        return cycles;
    }
}
