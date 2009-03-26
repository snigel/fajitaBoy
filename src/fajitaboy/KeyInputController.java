package fajitaboy;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import fajitaboy.memory.IO.JoyPad;
/**
 * Handles the key input.
 */
public class KeyInputController implements KeyListener {

    /**
     * joypad object.
     */
    private JoyPad jp;

    /**
     * Oscillator reference, used to pause/resume.
     */
    private Oscillator osc;

    /**
     * Creates a new KeyInputController object that handles key input.
     * @param joypad the JoyPad object
     * @param oscillator the Oscillator object
     */
    public KeyInputController(final JoyPad joypad, final Oscillator oscillator) {
        jp = joypad;
        osc = oscillator;
    }

    /**
     * {@inheritDoc}
     */
    public final void keyPressed(final KeyEvent e) {
        switch(e.getKeyCode()) {
        case KeyEvent.VK_DOWN: jp.setDown(true); break;
        case KeyEvent.VK_UP: jp.setUp(true); break;
        case KeyEvent.VK_LEFT: jp.setLeft(true); break;
        case KeyEvent.VK_RIGHT: jp.setRight(true); break;
        case KeyEvent.VK_X: jp.setA(true); break;
        case KeyEvent.VK_Z: jp.setB(true); break;
        case KeyEvent.VK_ENTER: jp.setStart(true); break;
        case KeyEvent.VK_SHIFT: jp.setSelect(true); break;

        case KeyEvent.VK_P:
            if (osc.isRunning()) {
                osc.stop();
            } else {
                Thread emulatorThread = new Thread(osc);
                emulatorThread.start();
            }
            break;
        default: /* Nothing */ break;
        }
    }

    /**
     * {@inheritDoc}
     */
    public final void keyReleased(final KeyEvent e) {
        switch(e.getKeyCode()) {
        case KeyEvent.VK_DOWN: jp.setDown(false); break;
        case KeyEvent.VK_UP: jp.setUp(false); break;
        case KeyEvent.VK_LEFT: jp.setLeft(false); break;
        case KeyEvent.VK_RIGHT: jp.setRight(false); break;
        case KeyEvent.VK_X: jp.setA(false); break;
        case KeyEvent.VK_Z: jp.setB(false); break;
        case KeyEvent.VK_ENTER: jp.setStart(false); break;
        case KeyEvent.VK_SHIFT: jp.setSelect(false); break;

        default: /* Nothing */ break;
        }
    }

    /**
     * {@inheritDoc}
     */
    public final void keyTyped(final KeyEvent e) {

    }
}
