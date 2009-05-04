package fajitaboy.debugger;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import fajitaboy.gb.memory.IO.JoyPad;

/**
 * Handles the key input.
 */
public class SimpleKeyInputController implements KeyListener {

    /**
     * joypad object.
     */
    private JoyPad jp;

    /**
     * Creates a new KeyInputController object that handles key input.
     * 
     * @param joypad
     *            the JoyPad object
     */
    public SimpleKeyInputController(final JoyPad joypad) {
        jp = joypad;
    }

    /**
     * {@inheritDoc}
     */
    public final void keyPressed(final KeyEvent e) {
        switch (e.getKeyCode()) {
        case KeyEvent.VK_DOWN:
            jp.setDown(true);
            break;
        case KeyEvent.VK_UP:
            jp.setUp(true);
            break;
        case KeyEvent.VK_LEFT:
            jp.setLeft(true);
            break;
        case KeyEvent.VK_RIGHT:
            jp.setRight(true);
            break;
        case KeyEvent.VK_X:
            jp.setA(true);
            break;
        case KeyEvent.VK_Z:
            jp.setB(true);
            break;
        case KeyEvent.VK_ENTER:
            jp.setStart(true);
            break;
        case KeyEvent.VK_SHIFT:
            jp.setSelect(true);
            break;

        default: /* Nothing */
            break;
        }
    }

    /**
     * {@inheritDoc}
     */
    public final void keyReleased(final KeyEvent e) {
        switch (e.getKeyCode()) {
        case KeyEvent.VK_DOWN:
            jp.setDown(false);
            break;
        case KeyEvent.VK_UP:
            jp.setUp(false);
            break;
        case KeyEvent.VK_LEFT:
            jp.setLeft(false);
            break;
        case KeyEvent.VK_RIGHT:
            jp.setRight(false);
            break;
        case KeyEvent.VK_X:
            jp.setA(false);
            break;
        case KeyEvent.VK_Z:
            jp.setB(false);
            break;
        case KeyEvent.VK_ENTER:
            jp.setStart(false);
            break;
        case KeyEvent.VK_SHIFT:
            jp.setSelect(false);
            break;

        default: /* Nothing */
            break;
        }
    }

    /**
     * {@inheritDoc}
     */
    public final void keyTyped(final KeyEvent e) {

    }
}
