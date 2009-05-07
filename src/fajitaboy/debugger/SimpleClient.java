package fajitaboy.debugger;

import java.awt.Dimension;
import java.awt.event.KeyListener;

import javax.swing.JFrame;

import fajitaboy.applet.GamePanel;
import fajitaboy.gb.Cpu;
import fajitaboy.gb.Oscillator;
import fajitaboy.gb.memory.AddressBus;
import static fajitaboy.constants.LCDConstants.*;

public class SimpleClient {
    public static void main(String[] args) {
        if (args.length == 1) {
            String path = args[0];
            
            JFrame jfr = new JFrame("SimpleClient " + path);
            
            jfr.setVisible(true);
            jfr.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            
            int zoom = 2;
            GamePanel panelScreen = new GamePanel(zoom);
            panelScreen.setPreferredSize(new Dimension(zoom * LCD_W,
                    zoom * LCD_H));
            jfr.setContentPane(panelScreen);
            jfr.pack();
            
            AddressBus  a = new AddressBus(path);
            Cpu c = new Cpu(a);
            final Oscillator o = new Oscillator(c, a, panelScreen, true);
            
            KeyListener kc = new SimpleKeyInputController(a.getJoyPad());
            panelScreen.addKeyListener(kc);
            jfr.addKeyListener(kc);
            
            
            new Thread(new Runnable() {
               public void run() {
                   while (true) {
                       o.step();
                   }
               }
            }).start();
            
            jfr.getContentPane().requestFocusInWindow();
            
        } else {
            System.out.println("Too few a commandline arguments");
        }
    }
}
