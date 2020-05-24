package com.pyide.main;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JMenuBar;
/**
 * Class for making the main window
 * @author Kevin Zhang
*/
public class WindowMaker {

	/**
	 * Makes a window
	 * @param title The title of the window.
	 * @param panel The jpanel of the window.
	 * @param menu The menu bar of the menu.
 	 */
	public WindowMaker(String title,JPanel panel,JMenuBar menu) {

		//this is the main window
		JFrame frame=new JFrame(title);

		frame.setJMenuBar(menu);//sets the menu bar
		frame.add(panel);//adds the panel
		frame.pack();//pack the frame
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);//ternimate the program when the window closed
		frame.setVisible(true);//make the window visible

	}

}
