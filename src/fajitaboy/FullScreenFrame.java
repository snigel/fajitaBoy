package fajitaboy;

import static fajitaboy.constants.LCDConstants.*;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.DisplayMode;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Toolkit;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.event.WindowStateListener;

import javax.swing.JFrame;
import javax.swing.WindowConstants;

public class FullScreenFrame extends JFrame {
    
    public FullScreenFrame(LayeredGamePanel layeredGamePanel) {
    	
    	GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice screen = ge.getDefaultScreenDevice();
        DisplayMode display = screen.getDisplayMode();
        
        setContentPane(layeredGamePanel);
        layeredGamePanel.updateSize(display.getWidth(), display.getHeight());
        
        setResizable(false);
        setUndecorated(true);
        //setAlwaysOnTop(true);
        
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        
        setTitle("FajitaBoy");
        setSize(Toolkit.getDefaultToolkit().getScreenSize());
        screen.setFullScreenWindow(this);
        
        requestFocusInWindow();
    }
}
