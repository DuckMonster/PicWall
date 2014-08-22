package com.emilstrom.picwall.server.program.database;

import com.emilstrom.picwall.server.program.Console;
import com.emilstrom.picwall.server.program.Program;
import com.emilstrom.picwall.server.program.image.Head;
import com.emilstrom.picwall.server.program.image.Node;

import java.io.*;

public class DatabaseHandler {
	Program program;
	String  rootdir =   "",
			dbdir =     "",
			currentdb = "";

	public DatabaseHandler(Program p) {
		program = p;
	}

	public void setDatabaseDir(String dir) {
		dbdir = dir;
		Console.output("Database dir set to " + rootdir + dbdir, Console.COLOR_GREEN);
	}

	public void setRootDir(String dir) {
		rootdir = dir;
		Console.output("Root dir set to " + rootdir, Console.COLOR_GREEN);
	}

	public void loadDatabase(String filename) {
		currentdb = filename;
		filename = rootdir + dbdir + filename;

		File f = new File(filename);
		if (!f.isFile()) {
			Console.output(filename + " isn't a file!", Console.COLOR_RED);
			return;
		}

		try {
			BufferedReader in = new BufferedReader(new FileReader(f));

			int headsLoaded=0, nodesLoaded=0;

			String head;
			while((head = in.readLine()) != null) {
				Head h = program.addHead(head);
				headsLoaded++;

				String node = in.readLine();
				while(node != null && !node.equals("")) {
					program.addNodeToHead(h, node);
					nodesLoaded++;

					node = in.readLine();
				}
			}

			in.close();

			Console.output(headsLoaded + " heads loaded!", Console.COLOR_GREEN);
			Console.output(nodesLoaded + " nodes loaded!", Console.COLOR_GREEN);
		} catch(Exception e) {
			Console.output("Couldn't read file at " + filename + "!", Console.COLOR_RED);
			Console.output(e.toString());
		}
	}

	public void saveDatabase() { saveDatabase(currentdb); }
	public void saveDatabase(String fileName) {
		String filePath = rootdir + dbdir + fileName;

		File f = new File(filePath);
		if (!f.isFile()) {
			if (createFile(fileName)) f = new File(filePath);
			else {
				Console.output("File didn't exist, and it couldn't be created!", Console.COLOR_RED);
				return;
			}
		}

		try {
			BufferedWriter w = new BufferedWriter(new FileWriter(f));

			for(Head h : program.headList) {
				w.write(h.url);
				w.newLine();

				for(Node n : h.nodeList) {
					w.write(n.url);
					w.newLine();
				}

				w.newLine();
			}

			w.flush();
			w.close();

			Console.output("Database saved at " + filePath, Console.COLOR_GREEN);
		} catch(Exception e) {
			Console.output("Couldn't read file at " + filePath + "!", Console.COLOR_RED);
			Console.output(e.toString());
		}
	}

	public boolean createFile(String filename) {
		filename = rootdir + dbdir + filename;

		File f = new File(filename);
		if (f.isFile()){
			Console.output(filename + " already exists!", Console.COLOR_RED);
			return false;
		}

		try {
			if (f.createNewFile()) {
				Console.output("Database created at " + filename, Console.COLOR_GREEN);
				return true;
			}
		} catch(Exception e) {
			Console.output("Couldn't create file at " + filename + "!", Console.COLOR_RED);
			return false;
		}

		return false;
	}

	public void print() {
		Console.output("Root:   " + rootdir);
		Console.output("Dir:    " + dbdir);
		Console.output("DB:     " + currentdb);
	}
}