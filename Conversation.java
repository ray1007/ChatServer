package archimedesServer;

import java.io.*;
import java.nio.*;
import java.nio.channels.*;
import java.nio.charset.Charset;
import java.util.*;
import java.net.*;
//import gui.*;

public class Conversation implements Runnable {

	private int _conversationID;
	private int _PORT;
	private boolean _keepRunning;
	private boolean _isPublic;
	private FileOutputStream _fout;
	private String _dataFolderPathPrefix = "C:/Users/nmlab/ChatServerData/conv"+_conversationID+"_";
	private int _dataCount;
	//private Vector<String> _record;
	//private Vector<int> _memberIds;
	//private ServerSocketChannel _serverChannel;

	Conversation(boolean isPublic, int id, int port){
		_isPublic = isPublic;
		_conversationID = id;
		_PORT = port;
		_keepRunning = true;
		_dataCount = 0;
		System.out.println("Conversation "+id+" is opened.");
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
						
						/*
						// Add time stamp.
						Date date = new Date();
						byte[] timestamp = date.toString().getBytes(Charset.forName("UTF-8"));
						publicBus.put(timestamp);
						publicBus.flip();
						for(int i=0;i<clients.size();++i){
							clients.get(i).write(publicBus);
							publicBus.flip();
						}
						publicBus.clear();
						*/
						int count;
						if( (count=client.read(publicBus))>0 ) { // First read in.
							System.out.println("First read in "+count+" bytes.");
							publicBus.flip();
							int dataType = publicBus.get();
							int userId   = publicBus.getInt(1);
							int length   = 0;
							byte[] filenameBytes = {};

							// Preparation for each data type before entering the switch scope. 
							if(dataType == 0)
								publicBus.position(9);

							if(dataType == 2){ // Data type : File. 
								// Get filename.
								length = publicBus.getInt(5);
								System.out.println("filename length "+length);
								filenameBytes = new byte[length];
								for(int i=0;i<length;++i){
									filenameBytes[i] = publicBus.array()[i+9];
								}
								String filename = new String(filenameBytes, "UTF-8");
								System.out.println(filename);
							
								// Initialize File & FuileOutputStream.
								_dataCount++;
								File file = new File(_dataFolderPathPrefix+_dataCount);
								if(!file.exists())
									file.createNewFile();
								_fout = new FileOutputStream(file);
								publicBus.position(9+length);
							}

							switch(dataType){
								case 0:
									for(int i=0;i<clients.size();++i){
										publicBus.position(9);
										clients.get(i).write(publicBus);
										publicBus.flip();
									}
									break;
								case 2:
									_fout.getChannel().write(publicBus);
							}
							publicBus.clear();

							while( (count = client.read(publicBus))>0){
								System.out.println("Read in "+count+" bytes.");
								publicBus.flip();
								switch(dataType){
									case 0:
										for(int i=0;i<clients.size();++i){
											clients.get(i).write(publicBus);
											publicBus.flip();
										}
										break;
									case 2:
										_fout.getChannel().write(publicBus);
								}
								publicBus.clear();
							}

							// Write to file if the message is a file.
							// Also print out a message of file.
							if(dataType == 2){
								_fout.close();
								// Write out file message.
								publicBus.put((byte)2);
								publicBus.putInt(userId);
								publicBus.putInt(length);
								publicBus.put(filenameBytes);
								String serverFileCode = _conversationID+"_"+_dataCount;
								publicBus.put(serverFileCode.getBytes(Charset.forName("UTF-8")));
								
								publicBus.flip();
								for(int i=0;i<clients.size();++i){
									clients.get(i).write(publicBus);
									publicBus.flip();
								}
								publicBus.clear();
							}
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