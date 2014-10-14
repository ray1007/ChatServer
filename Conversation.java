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
	private FileOutputStream _fout;
	private String _dataFolderPathPrefix = "C:/Users/nmlab/ChatServerData/conv"+_conversationID+"_";
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
		ByteBuffer publicBus = ByteBuffer.allocate(1024*25);
		//ChatDataProcessor processor = new ChatDataProcessor(1024*25);
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
		Vector<SocketChannel> clients = new Vector<SocketChannel>();
		/*
		try{
			File file = new File("newFile");
			if(!file.exists())
				file.createNewFile();
			_fout = new FileOutputStream(file);
		}catch(IOException ex){
			System.out.println("problem occured when contructing file");
		}
		*/
		while(_keepRunning){
			try{ selector.select(); }
			catch(IOException ex){ ex.printStackTrace(); break; }
			// Calls all the ready channels in keys.
			Set<SelectionKey> readyKeys = selector.selectedKeys();
			Iterator<SelectionKey> iterator = readyKeys.iterator();
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
						clients.addElement(client);
					}
					if(key.isReadable()){ // Get one client's InputStream.
						SocketChannel client = (SocketChannel) key.channel();
						
						if( client.read(publicBus)>0 ) { // First read in.
							publicBus.flip();
							int dataType = publicBus.get();
							int userId   = publicBus.getInt(1);
							
							// Preparation for each data type before entering the switch scope. 
							if(dataType == 0)
								publicBus.position(9);

							if(dataType == 2){ // Data type : File. 
							// Initialize File & FuileOutputStream.
								int length   = publicBus.getInt(5);
							// Get filename.
								String filename = "newFile";
								File file = new File(_dataFolderPathPrefix+filename);
								if(!file.exists())
									file.createNewFile();
								_fout = new FileOutputStream(file);
								publicBus.position(9);
							}

							switch(dataType){
								case 0:
									for(int i=0;i<clients.size();++i){
										client.write(publicBus);
										publicBus.flip();
									}
									break;
								case 2:
									_fout.getChannel().write(publicBus);
							}
							publicBus.clear();

							while(client.read(publicBus)>0){
								publicBus.flip();
								switch(dataType){
									case 0:
										for(int i=0;i<clients.size();++i){
											client.write(publicBus);
											publicBus.flip();
										}
										break;
									case 2:
										_fout.getChannel().write(publicBus);
								}
								publicBus.clear();
							}
							// Write to file if the message is a file.
							if(dataType == 2)
								_fout.close();
						}

						//System.out.println("OUT");
					}
				} catch(IOException ex){
					System.out.println("some jumpin dog.");
					key.cancel();
					clients.remove(key.channel());
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