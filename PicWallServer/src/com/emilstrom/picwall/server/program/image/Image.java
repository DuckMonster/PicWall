package com.emilstrom.picwall.server.program.image;

import com.emilstrom.picwall.server.program.Console;

import java.io.File;

/**
 * Created by DuckMonster on 2014-08-22.
 */
public class Image {
	public String url;

	public Image(String url) {
		this.url = url;
		Console.output("Created image: " + url);
	}

	public boolean fileExists() {
		File f = new File("/var/www/imgbank/" + url);
		return f.isFile();
	}
}
