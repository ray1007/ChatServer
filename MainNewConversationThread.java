package archimedesServer;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.logging.*;
import java.nio.*;
import java.nio.channels.*;
//import gui.*;

public class MainNewConversationThread extends Thread {
	private final static int NEW_CONVERSATION_PORT = 10001;
	private Vector<Conversation> _convs;

	MainNewConversationThread(Vector<Conversation> c){ _convs = c; }

	@Override
	public void run(){
		ExecutorService newConversationPool = Executors.newFixedThreadPool(50);

		try(ServerSocket newConversationSSocket = new ServerSocket(NEW_CONVERSATION_PORT)){
			while(true){
				Socket connection = newConversationSSocket.accept();
				Callable<Void> task = new NewConversationTask(connection, _convs);
				newConversationPool.submit(task);
			}
		} catch(IOException ex){
			//_logger.log(Level.SEVERE, "Can't open _loginSSocket.", ex);
		}	
	}

	private static class NewConversationTask implements Callable<Void> {
		private Socket _connection;
		private Vector<Conversation> _convs;

		NewConversationTask(Socket connection, Vector<Conversation> convs){
			_connection = connection; 
			_convs = convs;
		}

		@Override
		public Void call(){
			try{
				// Fetch input.
				//boolean public;
				//SocketChannel client = _connection.getChannel();

				// Construct a new Conversation.
				ServerSocket test = new ServerSocket(0);
				int newPort = test.getLocalPort();
				test.close();
				Conversation conversation = new Conversation(true, _convs.size(), newPort);
				Thread t = new Thread(conversation);
				t.start();
				_convs.addElement(conversation);
					
				//Write protocol of new conversation to members.

				//Writer out = new OutputStreamWriter(_connection.getOutputStream());
				System.out.println("New Conversation at port "+newPort);
				//out.write("new conversation at port "+newPort);
				//out.flush();
			} catch(IOException ex){
				//_logger.log(Level.SEVERE, "Can't write to connection.", ex);
			} finally{
				try{
					_connection.close();
				} catch(IOException e){ /*ignore*/ }
			}
			return null;
		}	
	}
}