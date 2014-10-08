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
	private ServerSocketChannel _serverChannel;

	Conversation(int id){
		_conversationID = id;
		_serverChannel = ServerSocketChannel.open();
	}

	public void exec(int port){
		// open a socket 
		ServerSocket s = _serverChannel.socket();
		s.bind(new InetSocketAddress(port)); 
		_serverChannel.configureBlocking(false);
	}
	public int getPort(){ return _serverChannel.getLocalPort(); }

	public void exec()

}