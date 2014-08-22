package com.emilstrom.picwall.server.program.image;

import com.emilstrom.net.server.MessageBuffer;
import com.emilstrom.picwall.protocol.Protocol;
import com.emilstrom.picwall.server.program.Console;
import com.emilstrom.picwall.server.program.Program;
import com.emilstrom.picwall.server.program.User;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Emil on 2014-08-08.
 */
public class Head extends Image {
	Program program;

	public int headIndex;
	public List<Node> nodeList;

	public Head(String url, int index, Program p) {
		super(url);
		program = p;
		headIndex = index;
		nodeList = new ArrayList<Node>();
	}

	public List<User> getViewingUsersList() {
		List<User> userList = new ArrayList<User>();

		for(User u : program.userList) {
			if (u != null && u.isViewingThread(this)) userList.add(u);
		}

		return userList;
	}

	public Node addNode(String url) {
		Node n = new Node(url, this);
		nodeList.add(n);

		//Update users of this new node!
		List<User> userList = getViewingUsersList();
		for(User u : userList) n.sendToUser(u);

		return n;
	}

	public void sendToUser(User u) {
		MessageBuffer msg = getThreadMessageBuffer();
		u.sendMessage(msg);
		u.addThreadToList(this);
	}

	public void sendToAllUsers() {
		for(User u : program.userList) {
			if (u != null) sendToUser(u);
		}
	}

	public void display() {
		Console.output("Thread " + headIndex);
		Console.output(url, Console.COLOR_GREEN);

		for(Node n : nodeList)
			Console.output("+  " + n.url, Console.COLOR_YELLOW);
	}

	public void remove() {
		program.headList.remove(this);
		Console.output("Thread " + headIndex + " removed", Console.COLOR_GREEN);
	}

	MessageBuffer getThreadMessageBuffer() {
		MessageBuffer msg = new MessageBuffer();
		msg.addWord(Protocol.DATA_HEAD);

		msg.addInt(headIndex);

		msg.addString(url);
		for(Node n : nodeList) msg.addString(n.url);

		return msg;
	}
}