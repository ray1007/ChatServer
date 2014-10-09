
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
	//private _conversations;
	
	ChatServer(){
		// Initialize fixed-port socket.
	}

	public void start(){
		Thread loginTask = new MainLoginThread();
		loginTask.start();
		Thread newConversationTask = new MainNewConversationThread();
		newConversationTask.start();
	}

	public static void main(String[] argv){
		ChatServer chatServer = new ChatServer();
		chatServer.start();
		
		ByteBuffer publicBus = ByteBuffer.allocate(4096);
		ServerSocketChannel serverChannel;
		Selector selector;
		try{
			serverChannel = ServerSocketChannel.open();
			ServerSocket ss = serverChannel.socket();
			ss.bind(new InetSocketAddress(3333));
			serverChannel.configureBlocking(false);
			selector = Selector.open();
			serverChannel.register(selector, SelectionKey.OP_ACCEPT);
		} catch(IOException ex){
			ex.printStackTrace();
			return;
		}
		boolean newMessage = false;
		while(true){
			try{ selector.select(); }
			catch(IOException ex){ ex.printStackTrace(); break; }
			
			Set<SelectionKey> readyKeys = selector.selectedKeys();
			Iterator<SelectionKey> iterator = readyKeys.iterator();
			int setSize = readyKeys.size();

			while(iterator.hasNext()){
				//if(readyKeys.size() > 1)
				//	System.out.println("BB "+readyKeys.size());
				SelectionKey key = iterator.next();
				iterator.remove();
				
				try{
					
					if(key.isAcceptable()){ // serverChannel accepts new connections.
						ServerSocketChannel server = (ServerSocketChannel) key.channel();
						SocketChannel client = server.accept();
						System.out.println("Accepted connection from "+client);
						client.configureBlocking(false);
						SelectionKey clientKey = client.register(selector, SelectionKey.OP_WRITE |
																	  SelectionKey.OP_READ);
						clientKey.attach(publicBus.duplicate());
					}
					if(key.isReadable() && !newMessage){ // get one client's InputStream.
						SocketChannel client = (SocketChannel) key.channel();
						ByteBuffer buffer = (ByteBuffer) key.attachment();
						newMessage = true;
						publicBus.clear();
						
						client.read(buffer);
						buffer.flip();
						
						//client.read(publicBus);
						System.out.println("BB "+readyKeys.size());
						System.out.println("readable "+setSize);
						
						// breaks while(iterator.hasNext()) loop.
						break; 
					}
					if(key.isWritable() && newMessage){ // write to one client's outputStream.
						SocketChannel client = (SocketChannel) key.channel();
						ByteBuffer buffer = (ByteBuffer) key.attachment();
						
						client.write(buffer);
						buffer.flip();
						System.out.println("writeable "+setSize);
						setSize--;
						if(setSize == 0){
							newMessage = false;
							//publicBus.clear();
						}
					}
				} catch(IOException ex){
					System.out.println("some jumpin dog.");
					setSize--;
					key.cancel();
					publicBus.clear();
					try{
						key.channel().close();
					} catch(IOException cex){ cex.printStackTrace(); }
				}
			}
		}

		/*try(ServerSocket server = new ServerSocket(3333)){
			while(true){
				try(Socket connection = server.accept()){
					System.out.println("Port 3333 Accepted connection from "+ connection);
					//Reader in  = new InputStreamReader(connection.getInputStream(), "UTF-8");
					BufferedReader in = new BufferedReader(
												new InputStreamReader(connection.getInputStream(), "UTF-8")
											);
					Writer out = new OutputStreamWriter(connection.getOutputStream(), "UTF-8");
					StringBuilder sb = new StringBuilder();
					Date now = new Date();
					//int c;
					//while((c = in.read()) != -1){
					//	sb.append((char) c);
						//System.out.println((char)c);
					//}
					String fragment;
					while((fragment = in.readLine()) != null){
						System.out.println("Some enytry...");
						out.write(fragment+", "+now.toString());
						out.flush();
					}
					System.out.println("ee");
				} catch(IOException ex){}
			}
		} catch(IOException ex){
			System.err.println(ex);
		}
		*/
	}

	private static class MainLoginThread extends Thread {
		MainLoginThread(){}

		@Override
		public void run(){
			ExecutorService loginPool = Executors.newFixedThreadPool(50);

			try(ServerSocket _loginSSocket = new ServerSocket(LOGIN_PORT)){
				while(true){
					Socket connection = _loginSSocket.accept();
					Callable<Void> task = new LoginTask(connection);
					loginPool.submit(task);
				}
			} catch(IOException ex){
				_logger.log(Level.SEVERE, "Can't open _loginSSocket.", ex);
			}	
		}

		private static class LoginTask implements Callable<Void> {
			private Socket _connection;

			LoginTask(Socket connection){ _connection = connection; }

			@Override
			public Void call(){
				System.out.println("Login port accepted connection from "+_connection);
				try{
					// When client logs in, it passes login data in the form : 
					// [account_length][password_length][   account   ][   password  ]
					// [    1 byte    ][    1 byte     ][   n  byte   ][    n byte   ]
 					InputStream inStream = _connection.getInputStream();
					int accLen = inStream.read();
					int pasLen = inStream.read();
					
					// Get account & password string by InputStreamReader.
					Reader in  = new InputStreamReader(inStream, "UTF-8");
					StringBuilder sb = new StringBuilder();
					for(int i=0;i<accLen+pasLen;++i)
						sb.append((char) in.read());
					
					String accAndPas = sb.toString();
					String account = accAndPas.substring(0,accLen);
					String password = accAndPas.substring(accLen);
					
					//Authenfication the login data.
 					Writer out = new OutputStreamWriter(_connection.getOutputStream(), "UTF-8");
					if(account.equals("Patrick") && password.equals("TextBox")){
						// Member confirmed.
						out.write(1); // If client logined successfully, write back (byte)(int 1)
						out.flush();
						
						// Redirect to port 3333.
						int newPort = 3333;
						for(int i=0;i<4;++i)
							out.write((byte) (newPort >> (8*i)));
						
						out.write("Welcome! How was your head? I heard that you hit yourself last night.:) -- Archimedes.");
						out.flush();
						System.out.println("Login success. acc="+account+", pas="+password+"\r\n");
					} else{
						// Member not confirmed.
						out.write(0); // If client logined unsuccessfully, write back (byte)(int 0)
						out.flush();
						out.write("You are not a member yet. Sign up and enjoy! :) -- Archimedes.");
						out.flush();
					}
				} catch(IOException ex){
					_logger.log(Level.SEVERE, "Can't write to connection.", ex);
				} finally{
					try{
						_connection.close();
					} catch(IOException e){ /*ignore*/ }
				}
				return null;
			}	
		}
	}

	private static class MainNewConversationThread extends Thread {
		MainNewConversationThread(){}

		@Override
		public void run(){
			ExecutorService newConversationPool = Executors.newFixedThreadPool(50);

			try(ServerSocket newConversationSSocket = new ServerSocket(NEW_CONVERSATION_PORT)){
				while(true){
					Socket connection = newConversationSSocket.accept();
					Callable<Void> task = new NewConversationTask(connection);
					newConversationPool.submit(task);
				}
			} catch(IOException ex){
				_logger.log(Level.SEVERE, "Can't open _loginSSocket.", ex);
			}	
		}

		private static class NewConversationTask implements Callable<Void> {
			private Socket _connection;

			NewConversationTask(Socket connection){ this._connection = connection; }

			@Override
			public Void call(){
				try{
					Writer out = new OutputStreamWriter(_connection.getOutputStream());
					out.write("newConversationTask "+"\r\n");
					out.flush();
				} catch(IOException ex){
					_logger.log(Level.SEVERE, "Can't write to connection.", ex);
				} finally{
					try{
						_connection.close();
					} catch(IOException e){ /*ignore*/ }
				}
				return null;
			}	
		}
	}
}