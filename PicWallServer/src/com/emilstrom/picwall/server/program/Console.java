package com.emilstrom.picwall.server.program;

import com.emilstrom.picwall.server.Component;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

/**
 * Created by Emil on 2014-08-05.
 */
public class Console implements Runnable {
	static final String ESC = "\033[";
	public static final int COLOR_BLACK = 30,
							COLOR_RED = 31,
							COLOR_GREEN = 32,
							COLOR_YELLOW = 33,
							COLOR_BLUE = 34,
							COLOR_MAGENTA = 35,
							COLOR_CYAN = 36,
							COLOR_WHITE = 37;

	public static void output(String msg) { output(msg, COLOR_WHITE); }
	public static void output(String msg, int color) {
		System.out.print(ESC + "0G");

		int padding = 120;

		String s = "| " + ESC + Integer.toString(color) + "m" + msg;
		for(int i=0; i<padding - msg.length(); i++) s += " ";

		Date date = new Date();
		DateFormat formatter= new SimpleDateFormat("E, MMM dd, HH:mm:ss");
		formatter.setTimeZone(TimeZone.getTimeZone("CET"));

		s += ESC + "33;2m";
		s += formatter.format(date);
		s += ESC + "m";

		System.out.println(s);

		System.out.print(ESC + "0G");
		System.out.print("# ");
	}

	public static void clear() {
		System.out.print(ESC + "2J");
		System.out.print(ESC + "f");
	}

	static Thread inputThread;

	public static void init(Program program) {
		inputThread = new Thread(new Console(program));
		inputThread.start();
	}

	Program program;
	public Console(Program p) {
		program = p;
	}

	public void run() {
		InputStreamReader reader = new InputStreamReader(System.in);
		BufferedReader in = new BufferedReader(reader);

		while(Component.running) {
			String str = "";

			System.out.print(ESC + "0G");
			System.out.print("# ");

			try{str = in.readLine();}
			catch(Exception e) {}

			runCommand(str, program);

			try{ Thread.sleep(5); } catch (Exception e) {}
		}
	}

	public static void runCommand(String str, Program p) {
		String[] coms = new String[10];
		for(int i=0; i<coms.length; i++) coms[i] = "";

		char[] ch = str.toCharArray();

		int s = 0;
		for(int i=0; i<ch.length; i++)
		{
			if (ch[i] != ' ')
				coms[s] += ch[i];
			else if (i > 0 && ch[i-1] != ' ')
				s++;
		}

		String[] args = new String[coms.length - 1];
		for(int i=0; i<args.length; i++)
			args[i] = coms[i + 1];

		p.runCommand(coms[0], args);
	}
}