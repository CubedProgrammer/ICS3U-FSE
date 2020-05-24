package com.pyide.evt;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
/**
 * Class for my window action listening
 * @author Kevin Zhang
 */
public class WindowHandler implements ComponentListener {

    /**
     * The package explorer to resize
     */
    private JScrollPane explorer;
    /**
     * The editors to resize
     */
    private JTabbedPane editors;

    /**
     * Constructor for the window handler
     * @param explorer The package explorer to resize
     * @param editors The editors to resize
     */
    public WindowHandler(JScrollPane explorer,JTabbedPane editors) {

        this.explorer=explorer;
        this.editors=editors;

    }

    /**
     * This is what happens when you resize the window.
     * @param e The component event, used to get the component.
     */
    public void componentResized(ComponentEvent e) {

    	//gets the component and its size
        Component c=e.getComponent();
        int w=c.getWidth();
        int h=c.getHeight();

        //resets the size accordingly
        explorer.setSize(w/4,h);
        editors.setSize(w*3/4,h);
        //set the preferred size
        explorer.setPreferredSize(explorer.getSize());
        editors.setPreferredSize(editors.getSize());
        //sets the location
        explorer.setLocation(0,0);
        editors.setLocation(w/4+1,0);

    }

    public void componentMoved(ComponentEvent e) {

    }

    public void componentShown(ComponentEvent e) {

    }

    public void componentHidden(ComponentEvent e) {

    }

}