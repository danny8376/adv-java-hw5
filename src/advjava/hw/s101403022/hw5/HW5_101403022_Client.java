/**
 * Advance JAVA Assignment
 * 
 * Student Name : 蔡崴丞
 * Student No.  : 101403022
 * Class : Information Management - 2A
 * 
 * Filename : HW5_101403022_Client.java
 * 
 * Ver.1.1
 *   Some bug fix
 */
package advjava.hw.s101403022.hw5;

import javax.swing.JFrame;
import advjava.hw.s101403022.hw5.client.MainFrame;

public class HW5_101403022_Client {
	public static void main(String args[]) {
		MainFrame frame = new MainFrame();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setSize(600, 480);
		frame.setVisible(true);
	}
}
