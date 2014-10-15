package archimedesServer;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.logging.*;
import java.nio.*;
import java.nio.channels.*;
//import gui.*;

public class ChatServer{

	public final static int LOGIN_PORT = 10000;
	public final static int NEW_CONVERSATION_PORT = 10001;
	private final static Logger _logger = Logger.getLogger("Errors");
	private static Vector<Conversation> _conversations;
	//private Thread _loginTask;
	//private Thread _newConversationTask;

	ChatServer(){
		// Initialize fixed-port socket.
		_conversations = new Vector<Conversation>();
	}

	public void start(){
		Thread loginTask = new MainLoginThread();
		loginTask.start();
		Thread newConversationTask = new MainNewConversationThread(_conversations);
		newConversationTask.start();
		Thread storageTask = new MainStorageThread();
		storageTask.start();
	}

	public static void main(String[] argv){
		ChatServer chatServer = new ChatServer();
		chatServer.start();

		Conversation lobby = new Conversation(true, 0, 3333);
		Thread t = new Thread(lobby);
		t.start();
		//System.out.println(c.getPort());
		_conversations.addElement(lobby);
		char ch;
		try{
			while( (ch = (char) System.in.read()) != 's')
				System.out.println(ch);
		} catch(IOException ex){
			ex.printStackTrace();
		}
		System.out.println("stop");
		lobby.stopThread();
		int p = _conversations.size();
		System.out.println("lol"+p);
	}
}