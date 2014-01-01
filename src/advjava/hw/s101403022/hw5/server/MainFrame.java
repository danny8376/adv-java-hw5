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
package advjava.hw.s101403022.hw5.server;

import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

import javax.swing.*;

public class MainFrame extends JFrame implements Runnable {
	
	private JLabel online;
	private JTextField text;
	private JTextArea msg, users;
	private JScrollPane msgScroll, usersScroll;
	
	private Thread serverThread;
	private ServerSocket server;
	
	private ArrayList<ClientHandler> clients;
	
	public MainFrame() {
		super("ChatRoom - Server");
		
		
		// GridBagLayout
		setLayout(new GridBagLayout());
		
		// ================     status part      ================
		GridBagConstraints c = new GridBagConstraints();
		
		JPanel topPanel = new JPanel();
		topPanel.setLayout(new GridBagLayout());
		GridBagConstraints c2 = new GridBagConstraints();
		
		c2.fill = GridBagConstraints.HORIZONTAL;
		c2.gridx = 0;
		c2.weightx = 0.1;
		online = new JLabel("在線人數 : 0");
		topPanel.add(online, c2);
		
		c2.fill = GridBagConstraints.HORIZONTAL;
		c2.gridx = 1;
		c2.weightx = 0;
		JLabel broadcast = new JLabel("廣播訊息 : ");
		topPanel.add(broadcast, c2);
		
		c2.fill = GridBagConstraints.HORIZONTAL;
		c2.gridx = 2;
		c2.weightx = 1.0;
		text = new JTextField();
		text.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				dispatchMessage("<Broadcast> " + text.getText());
				text.setText("");
			}
		});
		topPanel.add(text, c2);
		
		
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
		c.gridwidth = 1;
		c.weighty = 0;
		JLabel msgLabel = new JLabel("訊息:");
		add(msgLabel, c);
		
		c.fill = GridBagConstraints.BOTH;
		c.gridx = 0;
		c.gridy = 2;
		c.weighty = 1.0;
		msg = new JTextArea();
		msg.setBorder(BorderFactory.createLoweredBevelBorder());
		msg.setEditable(false);
		msgScroll = new JScrollPane(msg);
        msgScroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		add(msgScroll, c);
		
		
		c.fill = GridBagConstraints.BOTH;
		c.gridx = 1;
		c.gridy = 1;
		c.gridwidth = 1;
		c.weightx = 0.2;
		c.weighty = 0;
		JLabel usersLabel = new JLabel("使用者列表:");
		add(usersLabel, c);
		
		c.fill = GridBagConstraints.BOTH;
		c.gridx = 1;
		c.gridy = 2;
		c.weighty = 1.0;
		users = new JTextArea();
		users.setBorder(BorderFactory.createLoweredBevelBorder());
		users.setEditable(false);
		usersScroll = new JScrollPane(users);
        usersScroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		add(usersScroll, c);
		
		// start server thread
		clients = new ArrayList<ClientHandler>();
		
		serverThread = new Thread(this);
		serverThread.start();
	}
	
	private void appendMessage(String msg) {
		this.msg.append(msg +  System.lineSeparator());
		
		JScrollBar vertical = this.msgScroll.getVerticalScrollBar();
		vertical.setValue(vertical.getMaximum());
	}
	
	public void dispatchMessage(String msg) {
		dispatchMessage(msg, null);
	}
	
	public void dispatchMessage(String msg, ClientHandler exclude) {
		appendMessage(msg);
		synchronized(clients) {
			for (ClientHandler client:clients) {
				if (client != exclude) client.sendMessage("@" + msg);
			}
		}
	}
	
	public void nameChanged(ClientHandler client) {
		dispatchMessage("<System> ID:" + client.getClientID() + " 設定名稱為 " + client.getName(), client);
		refreshClients();
	}
	
	public void sendOnline(ClientHandler client) {
		client.sendMessage("!online" + clients.size());
	}
	
	public void sendOnline() {
		synchronized(clients) {
			for (ClientHandler client:clients) sendOnline(client);
		}
	}
	
	public boolean checkName(String name) {
		boolean flag = true;
		synchronized(clients) {
			for (ClientHandler client:clients) {
				if (client.getName().equals(name)) {
					flag = false;
					break;
				}
			}
		}
		return flag;
	}
	
	public void refreshClients() {
		sendOnline();
		synchronized(clients) {
			online.setText("在線人數 : " + clients.size());
			users.setText("");
			for (ClientHandler client:clients) {
				if (client.getName().isEmpty()) users.append("ID:" + client.getClientID() + System.lineSeparator());
				else users.append(client.getName() + "(ID:" + client.getClientID() + ")" + System.lineSeparator());
			}
		}
	}
	
	private void addClient(ClientHandler client) {
		dispatchMessage("<System> ID:" + client.getClientID() + " 加入!", client);
		synchronized(clients) {
			clients.add(client);
		}
		refreshClients();
	}
	
	public void removeClient(ClientHandler client) {
		synchronized(clients) {
			clients.remove(client);
		}
		if (client.getName().isEmpty()) dispatchMessage("<System> ID:" + client.getClientID() + " 已離線", client);
		else dispatchMessage("<System> " + client.getName() + "(ID:" + client.getClientID() + ") 已離線", client);
		refreshClients();
	}
	
	
	@Override
	public void run() {
		try {
			server = new ServerSocket(45678);
			
			appendMessage("<System> Server is listening at port 45678");
			
			while(true) {
				Socket client = server.accept();
				
				ClientHandler handler = new ClientHandler(client, this);
				addClient(handler);
			}
		} catch (IOException e) {
			JOptionPane.showMessageDialog(this, "Unable to listen port!", this.getTitle(), JOptionPane.ERROR_MESSAGE);
			this.dispose();
		}
	}
}
