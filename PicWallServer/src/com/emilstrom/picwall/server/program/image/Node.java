package com.emilstrom.picwall.server.program.image;

import com.emilstrom.net.server.*;
import com.emilstrom.picwall.server.program.*;
import com.emilstrom.picwall.protocol.*;

/**
 * Created by Emil on 2014-08-12.
 */
public class Node extends Image {
	public Head head;
	public Node(String url, Head h) {
		super(url);
		head = h;
	}

	public void sendToUser(User u) {
		MessageBuffer msg = new MessageBuffer();

		msg.addWord(Protocol.DATA_NODE);

		msg.addInt(head.headIndex);
		msg.addString(url);

		u.sendMessage(msg);
	}
}