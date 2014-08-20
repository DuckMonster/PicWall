package com.emilstrom.picwall.server.program;

import com.emilstrom.net.server.Client;
import com.emilstrom.net.server.MessageBuffer;
import com.emilstrom.picwall.protocol.Protocol;
import com.emilstrom.picwall.server.program.image.Head;
import com.emilstrom.picwall.server.program.image.Node;
import sun.plugin2.message.Message;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Emil on 2014-08-08.
 */
public class User {
	public int clientID;
	List<Integer> threadList = new ArrayList<Integer>();
	public void addThreadToList(Head h) { threadList.add(h.headIndex); }
	public boolean isViewingThread(Head h) { return threadList.contains(h.headIndex); }

	public User(int id) {
		clientID = id;
	}

	public void sendHead(Head h) {
		h.sendToUser(this);
	}
	public void sendNode(Node n) {
		n.sendToUser(this);
	}

	public void sendMessage(MessageBuffer msg) {
		Program.server.sendMessage(clientID, msg);
	}

	public void sendClearCanvas() {
		MessageBuffer msg = new MessageBuffer();
		msg.addWord(Protocol.DATA_CLEAR);

		sendMessage(msg);
	}

	public void sendOpenThread(Head h) {
		MessageBuffer msg = new MessageBuffer();
		msg.addWord(Protocol.DATA_OPEN_THREAD);
		msg.addInt(h.headIndex);

		sendMessage(msg);
	}

	public void sendCenterThread(Head h) {
		MessageBuffer msg = new MessageBuffer();
		msg.addWord(Protocol.DATA_CENTER_THREAD);
		msg.addInt(h.headIndex);

		sendMessage(msg);
	}

	public void sendFilename(String fname) {
		MessageBuffer msg = new MessageBuffer();
		msg.addWord(Protocol.REQUEST_FILE_NAME);

		msg.addString(fname);

		sendMessage(msg);
	}

	public void display() {
		Client c = Program.server.getClient(clientID);
		if (c == null) Console.output("Client " + clientID + " doesn't exist!");
		else {
			Console.output("--- Client " + clientID + " ---", Console.COLOR_CYAN);
			Console.output("");
			Console.output("IP: " + c.getIP());
		}
	}
}