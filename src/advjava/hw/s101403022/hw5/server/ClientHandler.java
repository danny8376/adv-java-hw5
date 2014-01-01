/**
 * Advance JAVA Assignment
 * 
 * Student Name : 蔡崴丞
 * Student No.  : 101403022
 * Class : Information Management - 2A
 * 
 * Filename : ClientHandler.java
 * 
 * 
 */
package advjava.hw.s101403022.hw5.server;

import java.io.*;
import java.net.Socket;

public class ClientHandler implements Runnable {
	
	private static int clientCount = 0;
	
	private Socket socket;
	private MainFrame server;
	private Thread clientThread;
	private String name;
	private BufferedWriter writer;
	private int cid;
	private boolean disconnected;
	
	public ClientHandler(Socket _socket, MainFrame _server) {
		this.socket = _socket;
		this.server = _server;
		
		cid = ++clientCount;
		name = "";
		disconnected = false;
		
		try {
			writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
		} catch (IOException e) {
			disconnect();
		}
		
		clientThread = new Thread(this);
		clientThread.start();
	}
	
	public String getName() {
		return name;
	}
	
	public int getClientID() {
		return cid;
	}
	
	public void sendMessage(String msg) {
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
		String type = msg.substring(0, 1), cont = msg.substring(1);
		if ("!".equals(type)) { // command
			if (cont.startsWith("namecheck")) {
				String name = cont.substring(9);
				if (server.checkName(name)) {
					sendMessage("!namecheckpass");
					this.name = name;
					server.nameChanged(this);
				} else {
					sendMessage("!namecheckfailed");
				}
			} else if ("online".equals(cont)) {
				server.sendOnline(this);
			} else if ("exit".equals(cont)) {
				disconnect();
			} else {
				// wrong command!
			}
		} else if ("@".equals(type)) { // message prefix : @
			server.dispatchMessage(name + ":" + cont, this);
			sendMessage("!msg");
		} else {
			// wrong type!
		}
	}
	
	@Override
	public void run() {
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
	
	private void disconnect() {
		if (disconnected) return;
		disconnected = true;
		
		try {
			socket.close();
		} catch (IOException e) {
		}
		server.removeClient(this);
	}
}
