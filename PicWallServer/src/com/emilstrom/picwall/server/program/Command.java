package com.emilstrom.picwall.server.program;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Emil on 2014-08-23.
 */
public class Command {
	public static List<Command> commandList = new ArrayList<>();
	public static interface Execute {
		public void exec(List<String> args);
	}

	public int index;

	String comm;
	Execute execute;
	List<Command> nestingCommands = new ArrayList<>();

	public Command(String comm) {
		commandList.add(this);

		this.comm = comm;
		index = 0;
	}
	public Command(Command head, String comm) {
		this.comm = comm;
		index = head.index + 1;
	}

	public boolean equals(String comm) {
		return this.comm.equals(comm);
	}

	public Command getCommand(String comm) {
		for(Command c : nestingCommands) if (c.equals(comm)) return c;

		return null;
	}

	public Command addCommand(String comm) {
		Command c = new Command(this, comm);
		nestingCommands.add(c);
		return c;
	}

	public void setExecute(Execute e) {
		execute = e;
	}

	public boolean isExecutable() {
		return execute != null;
	}

	public void checkCommandTree(String[] commList) {
		if (equals(commList[index])) {
			if (isExecutable()) {
				List<String> args = new ArrayList<>();
				for(int i=index+1; i<commList.length; i++) args.add(commList[i]);

				execute.exec(args);
			} else {
				Command c = getCommand(commList[index+1]);
				if (c == null) {
					printTree();
				} else {
					c.checkCommandTree(commList);
				}
			}
		}
	}

	public void printTree() {
		Console.output(comm, Console.COLOR_BLUE);
		for(Command c : nestingCommands)
			Console.output(".." + c.comm, Console.COLOR_YELLOW);
	}
}