package com.emilstrom.picwall.server.program;

import com.emilstrom.net.server.Client;
import com.emilstrom.net.server.IServer;
import com.emilstrom.net.server.MessageBuffer;
import com.emilstrom.net.server.ServerEngine;
import com.emilstrom.picwall.protocol.Protocol;
import com.emilstrom.picwall.server.program.database.DatabaseHandler;
import com.emilstrom.picwall.server.program.image.Head;
import com.emilstrom.picwall.server.program.image.Node;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

/**
 * Created by Emil on 2014-08-05.
 */
public class Program implements IServer {
	public static int THREAD_COUNT = 0;
	public static final String IMAGE_DIRECTORY = "http://199.127.226.140/imgbank";
	public static final byte VERSION[] = {0, 1, 3, 0};
	public static ServerEngine server;
	static Random random = new Random();

	public DatabaseHandler databaseHandler;

	public User[] userList = new User[20];
	public User getUser(int i) { return userList[i]; }

	public List<Head> headList;

	public Program() {
		Console.clear();
		Console.output("--- SERVER STARTED! ---");
		Console.output("--- Version " + VERSION[0] + "." + VERSION[1] + "." + VERSION[2] + "." + VERSION[3] + " ---");
		ServerEngine.showMessages = false;

		databaseHandler = new DatabaseHandler(this);

		server = new ServerEngine(12345, this);
		headList = new ArrayList<Head>();

		Console.init(this);
        runStartupFile();
	}

    public void runStartupFile() {
        try {
            File f = new File("./startup.cfg");
            if (!f.isFile()) {
                Console.output(f.getAbsolutePath() + " isn't a file!", Console.COLOR_RED);
                return;
            }

            BufferedReader in = new BufferedReader(new FileReader(f));

            String s;
            while((s = in.readLine()) != null) {
                Console.runCommand(s, this);
            }
        } catch(Exception e) {
            Console.output("Couldn't read startup file!", Console.COLOR_RED);
        }
    }

	public void logic() {
		server.update();
	}

	public void createTemporaryHeads() {
		addHead(
				"1/021.jpg",
				"1/66f.jpg",
				"1/a23.jpg",
				"1/aca.jpg",
				"1/c08.jpg",
				"1/c19.jpg"
		);

		addHead(
				"2/022.jpg",
				"2/5cd.jpg",
				"2/b68.jpg",
				"2/c2e.jpg",
				"2/ef3.jpg"
		);

		addHead(
				"3/4d4.jpg",
				"3/71b.png",
				"3/102.jpg",
				"3/105.jpg",
				"3/288.jpg",
				"3/292.jpg"
		);

		addHead(
				"4/2ur78et.jpg",
				"4/4b6a1a3f73aeb.jpg",
				"4/Kim_dat.jpg",
				"4/Me-chinese-me-play-joke-me-put-pee-pee-in-your-coke.jpg",
				"4/kim-jong-il-kim-jong-ok-480x327.jpg",
				"4/kimtoilet.jpg",
				"4/wizardofkim.jpg"
		);
	}

	public Head getHead(int headIndex) {
		for(Head h : headList) if (h.headIndex == headIndex) return h;

		return null;
	}

	public Head addHead(String... url) {
		Head h = addHead(url[0]);

		for(int i=1; i<url.length; i++) {
			h.addNode(url[i]);
		}

		return h;
	}

	public Head addHead(String url) {
		THREAD_COUNT++;
		Head h = new Head(THREAD_COUNT, url, this);
		headList.add(h);

		return h;
	}

	public Node addNodeToHead(Head h, String url) {
		return h.addNode(url);
	}

	public void removeAllHeads() {
		List<Head> tempList = new ArrayList<Head>();
		for(Head h : headList) tempList.add(h);
		for(Head h : tempList) h.remove();
	}

	@Override
	public void clientConnected(int id) {
		Console.output("Client " + id + " connected!");
		userList[id] = new User(id);
	}

	@Override
	public void clientMessage(int id, MessageBuffer msg) {
		switch (msg.readWord()) {
			case Protocol.REQUEST_IMAGE:
				sendAllHeadsToUser(getUser(id));
				break;

			case Protocol.REQUEST_FILE_NAME:
				sendFilenameToUser(getUser(id));
				break;

			case Protocol.HEAD_UPLOADED:
				receiveHeadUploaded(getUser(id), msg);
				break;

			case Protocol.NODE_UPLOADED:
				receiveNodeUploaded(getUser(id), msg);
				break;
		}
	}

	@Override
	public void clientDisconnected(int id) {
		Console.output("Client " + id + " disconnected!");
		userList[id] = null;
	}


	//
	public void sendHeadsToClient(User u, int n) {
		for(int i=0; i<n; i++)
			u.sendHead(
					headList.get(random.nextInt(headList.size()))
			);
	}

	public void sendAllHeadsToUser(User u) {
		for(Head h : headList) h.sendToUser(u);
	}

	public void sendFilenameToUser(User u) {
		DateFormat formatter= new SimpleDateFormat("YYMMdd-HHmmss");
		String fileName = formatter.format(new Date());

		u.sendFilename(fileName);

		Console.output("Client " + u.clientID + " is uploading an image to " + fileName);
	}

	public void receiveHeadUploaded(User u, MessageBuffer msg) {
		Console.output("Client " + u.clientID + " created a thread!");

		Head h = addHead("uploads/" + msg.readString());
		h.sendToAllUsers();

		databaseHandler.saveDatabase();
	}
	public void receiveNodeUploaded(User u, MessageBuffer msg) {
		Node n = addNodeToHead(getHead(msg.readInt()), "uploads/" + msg.readString());
		Console.output("Client " + u.clientID + " replied to thread " + n.head.headIndex + "!");

		databaseHandler.saveDatabase();
	}


	//CONSOLE COMMANDS
	public void runCommand(String command, String[] params) {
		try {
			switch (command) {
				case "disp":
				case "show":
				case "print":
					switch (params[0]) {
						case "user": {
							if (params[1] != "") {
								User u = getUser(Integer.parseInt(params[1]));
								if (u != null) u.display();
								else Console.output("That user doesn't exist!", Console.COLOR_RED);
							} else
								displayClientList();
						}
						break;

						case "thread":
							if (params[1] != "") {
								Head h = getHead(Integer.parseInt(params[1]));
								if (h != null) h.display();
								else Console.output("That thread doesn't exist!", Console.COLOR_RED);
							} else
								displayThreadList();
							break;

						default:
							Console.output("Display what?", Console.COLOR_RED);
							break;
					}
					break;

				case "send":
					switch(params[0]) {
						case "url": {
							sendHeadsToClient(getUser(Integer.parseInt(params[2])), Integer.parseInt(params[1]));
							break;
						}

						case "clear":
							getUser(Integer.parseInt(params[1])).sendClearCanvas();
							break;

						default:
							Console.output("Send what?", Console.COLOR_RED);
							break;
					}
					break;

				case "clear":
					Console.clear();
					break;

				case "remove":
					switch(params[0]) {
						case "thread": {
							if (params[1].equals("all")) {
								removeAllHeads();
							} else {
								Head h = getHead(Integer.parseInt(params[1]));
								if (h != null) h.remove();
								else Console.output("That thread doesnt exist!", Console.COLOR_RED);
							}
							break;
						}

						default:
							Console.output("Delete what?", Console.COLOR_RED);
							break;
					}
					break;

				case "database":
					switch(params[0]) {
						case "root":
							databaseHandler.setRootDir(params[1]);
							break;

						case "dir":
							databaseHandler.setDatabaseDir(params[1]);
							break;

						case "load":
							removeAllHeads();
							databaseHandler.loadDatabase(params[1]);
							break;

						case "save":
							if (params[1].equals(""))
								databaseHandler.saveDatabase();
							else
								databaseHandler.saveDatabase(params[1]);
							break;

						case "create":
							databaseHandler.createFile(params[1]);
							break;

						case "print":
							databaseHandler.print();
							break;
					}
					break;

				default:
					Console.output("Don't think that's a command...", Console.COLOR_RED);
					break;
			}
		} catch(Exception e) {
			Console.output("You did something horribly, HORRIBLY, wrong...", Console.COLOR_RED);
			Console.output(e.toString());
		}
	}

	public void displayClientList() {
		int n = 0;
		for(User c : userList) if (c != null) n++;

		Console.output("Number of clients: " + n);

		for(User c : userList)
			if (c != null) {
				Client cl = server.getClient(c.clientID);
				Console.output("ID " + cl.id + " - " + cl.getIP());
			}
	}

	public void displayThreadList() {
		for(Head h : headList) {
			h.display();

			Console.output("");
		}
	}
}
