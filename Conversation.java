//package userver;
import java.io.*;
import java.nio.*;
import java.nio.channels.*;
import java.util.*;
import java.net.*;
//import gui.*;

public class Conversation implements Runnable {

	private int _conversationID;
	private  int _PORT;
	private boolean _keepRunning;
	//private Vector<int> _memberIds;
	//private ServerSocketChannel _serverChannel;

	Conversation(int id, int port){
		_conversationID = id;
		_PORT = port;
		_keepRunning = true;
		//System.out.println("Conversation opened.");
	}

	public int getPort(){ return _PORT; }

	public void stopThread(){ _keepRunning = false; }

	@Override
	public void run(){
		// The ByteBuffer for all the clients in this conversation.
		ByteBuffer publicBus = ByteBuffer.allocate(10000);
		ChatDataProcessor processor = new ChatDataProcessor();
		ServerSocketChannel serverChannel;
		Selector selector;
		try{ // Open a ServerSocket, if error happens it should be handled.
			serverChannel = ServerSocketChannel.open();
			ServerSocket ss = serverChannel.socket();
			// Bind ServerSocket to the Address.
			ss.bind(new InetSocketAddress(_PORT));
			serverChannel.configureBlocking(false);
			selector = Selector.open();
			serverChannel.register(selector, SelectionKey.OP_ACCEPT);
		} catch(IOException ex){
			ex.printStackTrace();
			return;
		}
		// Flag used to control the read/write operations of clients.
		boolean newMessage = false;
		while(_keepRunning){
			try{ selector.select(); }
			catch(IOException ex){ ex.printStackTrace(); break; }
			// Calls all the ready channels in keys.
			Set<SelectionKey> readyKeys = selector.selectedKeys();
			Iterator<SelectionKey> iterator = readyKeys.iterator();
			int setSize = readyKeys.size();
			// Traverse all the keys (channels).
			while(iterator.hasNext() && _keepRunning){
				SelectionKey key = iterator.next();
				iterator.remove();
				try{
					if(key.isAcceptable()){ // ServerChannel accepts new connections.
						ServerSocketChannel server = (ServerSocketChannel) key.channel();
						SocketChannel client = server.accept();
						System.out.println("Accepted connection from :"+client);
						client.configureBlocking(false);
						// Make clients able to read and write.
						SelectionKey clientKey = client.register(selector, SelectionKey.OP_WRITE |
																	  SelectionKey.OP_READ);
					}
					if(key.isReadable() && !newMessage){ // Get one client's InputStream.
						SocketChannel client = (SocketChannel) key.channel();
						newMessage = true;
						if(client.read(publicBus) == 0){
							System.out.println("Some fuckin bad users who dont throw exceptions");
							key.cancel();
							try{ key.channel().close();
							} catch(IOException cex){ cex.printStackTrace(); }
							newMessage = false;
							publicBus.clear();
						} else { //If the channel read some data, flip the publicBus so the write operation could work.
							publicBus.flip();
							processor.parse(publicBus.duplicate());
						}
						// Breaks while(iterator.hasNext()) loop. A
						break; 
					}
					if(key.isWritable() && newMessage){ // Write to one client's outputStream.
						SocketChannel client = (SocketChannel) key.channel();
						client.write(processor.getByteBuffer());
						processor.getByteBuffer().flip();
						//client.write(publicBus);
						//publicBus.flip();
						setSize--;
						if(setSize == 0){
							newMessage = false;
							publicBus.clear();
						}
					}
				} catch(IOException ex){
					System.out.println("some jumpin dog.");
					setSize--;
					key.cancel();
					try{
						key.channel().close();
					} catch(IOException cex){ cex.printStackTrace(); }
				} 
			} // End of while(iterator.hasNext())
		} // End of while(_keepRunning)
		// After being stopped, close all channels.
		try{ selector.select(); }
		catch(IOException ex){ ex.printStackTrace(); }
		Set<SelectionKey> keys = selector.selectedKeys();
		Iterator<SelectionKey> it = keys.iterator();
		while(it.hasNext()){
			SelectionKey key = it.next();
			it.remove();
			key.cancel();
			try{
				key.channel().close();
			} catch(IOException cex){ cex.printStackTrace(); }
		}
	}
}