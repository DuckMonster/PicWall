package com.emilstrom.picwall.server;

import com.emilstrom.picwall.server.program.Program;

/**
 * Created by Emil on 2014-08-05.
 */
public class Component implements Runnable {
	public static boolean running = false;

	public Component() {
	}

	public void start() {
		if (running) return;

		running = true;
		new Thread(this).start();
	}

	public void stop() {
		running = false;
	}

	public void run() {
		Program p = new Program();

		while(running) {
			p.logic();

			try{ Thread.sleep(5); } catch (Exception e) {}
		}
	}

	public static void main(String[] args) {
		Component comp = new Component();
		comp.start();
	}
}