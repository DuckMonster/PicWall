package com.emilstrom.picwall.protocol;

/**
 * Created by Emil on 2014-08-05.
 */
public class Protocol {
	public static final int REQUEST_IMAGE = 0,
			RECEIVE_HEAD = 1,
			RECEIVE_NODE = 2,
			RECEIVE_CLEAR = 10,

			RECEIVE_OPEN_THREAD = 50,
			RECEIVE_CENTER_THREAD = 51,

			REQUEST_FILE_NAME = 100,
			HEAD_UPLOADED = 101,
			NODE_UPLOADED = 102;
}