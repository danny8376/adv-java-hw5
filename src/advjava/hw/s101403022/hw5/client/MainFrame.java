/**
 * Advance JAVA Assignment
 * 
 * Student Name : 蔡崴丞
 * Student No.  : 101403022
 * Class : Information Management - 2A
 * 
 * Filename : MainFrame.java
 * 
 * 
 */
package advjava.hw.s101403022.hw5.client;

import java.awt.*;
import java.awt.event.*;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;

import javax.swing.*;

public class MainFrame extends JFrame implements Runnable {
	private JLabel online;
	private JTextField name, text;
	private JTextArea msg;
	private JScrollPane msgScroll;
	
	private Thread main;
	private Socket socket;
	private BufferedWriter writer;
	
	private int step;
	
	public MainFrame() {
		super("ChatRoom - Client");
		
		
		addWindowListener(new WindowAdapter(){
			@Override
			public void windowClosing(WindowEvent event) {
				sendMessage("!exit");
			}
		});
		
		
		// GridBagLayout
		setLayout(new GridBagLayout());
		
		// ================     status part      ================
		GridBagConstraints c = new GridBagConstraints();
		
		JPanel topPanel = new JPanel();
		topPanel.setLayout(new GridBagLayout());
		GridBagConstraints c2 = new GridBagConstraints();
		
		c2.fill = GridBagConstraints.HORIZONTAL;
		c2.gridx = 0;
		c2.weightx = 0;
		JLabel broadcast = new JLabel("暱稱 : ");
		topPanel.add(broadcast, c2);
		
		c2.fill = GridBagConstraints.HORIZONTAL;
		c2.gridx = 1;
		c2.weightx = 0.5;
		name = new JTextField();
		name.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				if (name.getText().isEmpty()) online.setText("請輸入暱稱!");
				else sendMessage("!namecheck" + name.getText());
			}
		});
		topPanel.add(name, c2);
		
		c2.fill = GridBagConstraints.HORIZONTAL;
		c2.gridx = 2;
		c2.weightx = 0.5;
		online = new JLabel("");
		topPanel.add(online, c2);
		
		
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridx = 0;
		c.gridy = 0;
		c.gridwidth = 2;
		c.weightx = 1.0;
		add(topPanel, c);
		
		
		// ================       msg part       ================
		c.fill = GridBagConstraints.BOTH;
		c.gridx = 0;
		c.gridy = 1;
		c.weighty = 0;
		JLabel msgLabel = new JLabel("訊息:");
		add(msgLabel, c);
		
		c.fill = GridBagConstraints.BOTH;
		c.gridx = 0;
		c.gridy = 2;
		c.weighty = 1.0;
		msg = new JTextArea("請先輸入暱稱");
		msg.setBorder(BorderFactory.createLoweredBevelBorder());
		msg.setEditable(false);
		msg.setEnabled(false);
		msgScroll = new JScrollPane(msg);
        msgScroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		add(msgScroll, c);
		
		
		c.fill = GridBagConstraints.BOTH;
		c.gridx = 0;
		c.gridy = 3;
		c.gridwidth = 1;
		c.weightx = 0;
		c.weighty = 0;
		JLabel textLabel = new JLabel("傳送訊息:");
		add(textLabel, c);
		
		c.fill = GridBagConstraints.BOTH;
		c.gridx = 1;
		c.gridy = 3;
		c.weightx = 0.5;
		c.weighty = 0;
		text = new JTextField();
		text.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				if (!name.getText().isEmpty()) { 
					String msg = text.getText();
					appendMessage("你自己:" + msg);
					sendMessage("@" + msg);
					text.setText("");
				}
			}
		});
		text.setEnabled(false);
		add(text, c);
		
		// start client thread
		step = 0; // 0 : waiting for name , 1 : message!
		
		main = new Thread(this);
		main.start();
	}
	
	private void appendMessage(String msg) {
		this.msg.append(msg +  System.lineSeparator());
		
		JScrollBar vertical = this.msgScroll.getVerticalScrollBar();
		vertical.setValue(vertical.getMaximum());
	}
	
	private void sendMessage(String msg) {
		try {
			writer.write(msg);
			writer.newLine();
			writer.flush();
		} catch (IOException e) {
			disconnect();
		}
	}
	
	private void processMessage(String msg) {
		if (msg.length() < 2) return;
		// prevent processing message before nickname given 
		if (step == 0) {
			if (msg.startsWith("!namecheck")) {
				if ("pass".equals(msg.substring(10))) {
					this.online.setText("");
					this.name.setEditable(false);
					this.msg.setText("");
					this.msg.setEnabled(true);
					this.text.setEnabled(true);
					this.step = 1;
					sendMessage("!online");
				} else {
					online.setText("此暱稱已有人使用!");
				}
			}
		} else {
			String type = msg.substring(0, 1), cont = msg.substring(1);
			if ("!".equals(type)) { // command
				if (cont.startsWith("online")) {
					String online = cont.substring(6);
					this.online.setText("在線人數 : " + online);
				} else {
					// wrong command!
				}
			} else if ("@".equals(type)) { // message prefix : @
				appendMessage(cont);
			} else {
				// wrong type!
			}
		}
	}
	
	private void waitingMessage() {
		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			String line;
			while((line = reader.readLine()) != null) {
				processMessage(line);
			}
		} catch (IOException e) {
			disconnect();
		}
	}
	
	@Override
	public void run() {
		try {
			socket = new Socket("127.0.0.1", 45678);
			
			writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
			
			waitingMessage();
		} catch (IOException e) {
			JOptionPane.showMessageDialog(this, "Unable to connect to server!", this.getTitle(), JOptionPane.ERROR_MESSAGE);
			this.dispose();
		}
	}
	
	private void disconnect() {
		try {
			socket.close();
		} catch (IOException e) {
		}
		JOptionPane.showMessageDialog(this, "Unable to communicate with server!", this.getTitle(), JOptionPane.ERROR_MESSAGE);
		this.dispose();
	}
}
