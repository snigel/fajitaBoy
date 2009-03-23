package fajitaboy;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import fajitaboy.IO.JoyPad;

public class KeyInputController implements KeyListener {

    JoyPad jp;
    
    public KeyInputController(JoyPad joypad) {
        jp = joypad;
    }
    
    public void keyPressed(KeyEvent e) {
        switch(e.getKeyCode()) {
        case KeyEvent.VK_DOWN: jp.setDown(true); break;
        case KeyEvent.VK_UP: jp.setUp(true); break;
        case KeyEvent.VK_LEFT: jp.setLeft(true); break;
        case KeyEvent.VK_RIGHT: jp.setRight(true); break;
        case KeyEvent.VK_X: jp.setA(true); break;
        case KeyEvent.VK_Z: jp.setB(true); break;
        case KeyEvent.VK_ENTER: jp.setStart(true); break;
        case KeyEvent.VK_SHIFT: jp.setSelect(true); break;
        }
    }

    public void keyReleased(KeyEvent e) {
        switch(e.getKeyCode()) {
        case KeyEvent.VK_DOWN: jp.setDown(false); break;
        case KeyEvent.VK_UP: jp.setUp(false); break;
        case KeyEvent.VK_LEFT: jp.setLeft(false); break;
        case KeyEvent.VK_RIGHT: jp.setRight(false); break;
        case KeyEvent.VK_X: jp.setA(false); break;
        case KeyEvent.VK_Z: jp.setB(false); break;
        case KeyEvent.VK_ENTER: jp.setStart(false); break;
        case KeyEvent.VK_SHIFT: jp.setSelect(false); break;
        }
    }

    public void keyTyped(KeyEvent e) {

    }
}
