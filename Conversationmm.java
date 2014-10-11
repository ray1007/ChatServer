//package userver;
import java.io.*;
import java.nio.*;
import java.nio.channels.*;
import java.util.*;
import java.net.*;
//import gui.*;

public class ConversationNIO{

	private final static int _conversationID;
	private Vector<int> _memberIds;
	private Vector<ServerSocket> _servers;

	Conversation(int id){
		this._conversationID = id;
		this.serverChannel = ServerSocketChannel.open();
	}

	public void addServer(int port){
		this._servers.add(new ServerSocket(port));
		
	}
	public void exec()

}