package com.emilstrom.picwall.server.program.image;

import com.emilstrom.net.server.*;
import com.emilstrom.picwall.server.program.*;
import com.emilstrom.picwall.protocol.*;

/**
 * Created by Emil on 2014-08-12.
 */
public class Node {
	public Head head;
	public String imageUrl;
	public Node(Head h ,String url) {
		head = h;
		this.imageUrl = url;
	}

	public void sendToUser(User u) {
		MessageBuffer msg = new MessageBuffer();

		msg.addWord(Protocol.DATA_NODE);

		msg.addInt(head.headIndex);
		msg.addString(imageUrl);

		u.sendMessage(msg);

		Console.output("Sent Node to client " + u.clientID + ": " + imageUrl);
	}
}