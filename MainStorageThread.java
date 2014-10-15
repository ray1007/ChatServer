package archimedesServer;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.logging.*;
import java.nio.*;
import java.nio.channels.*;
import java.nio.file.*;
//import gui.*;

public class MainStorageThread extends Thread {
	private final static int STORAGE_PORT = 10002;
	
	MainStorageThread(){}

	@Override
	public void run(){
		ExecutorService storagePool = Executors.newFixedThreadPool(50);
		try(ServerSocket _storageSSocket = new ServerSocket(STORAGE_PORT)){
			while(true){
				Socket connection = _storageSSocket.accept();
				Callable<Void> task = new StorageTask(connection);
				storagePool.submit(task);
			}
		} catch(IOException ex){
			//_logger.log(Level.SEVERE, "Can't open _loginSSocket.", ex);
		}	
	}

	private static class StorageTask implements Callable<Void> {
		private Socket _connection;
		private final static String _dataFolderPathPrefix = "C:/Users/nmlab/ChatServerData";

		StorageTask(Socket connection){ _connection = connection; }

		@Override
		public Void call(){
			System.out.println("\nStorage port accepted connection from "+_connection);
			try{
				//ByteBuffer bus = ByteBuffer.allocate(1024*25);
				//SocketChannel client = _connection.getChannel();
				Reader in = new BufferedReader(
								new InputStreamReader(_connection.getInputStream(), "UTF-8")
								);
				StringBuilder sb = new StringBuilder();
				char c;
				while( (c = (char)in.read()) > 0)
					sb.append(c);
				
				Path targetPath = Paths.get(_dataFolderPathPrefix, "conv_"+ sb.toString());
				byte[] targetData = Files.readAllBytes(targetPath);
				OutputStream out = new BufferedOutputStream(
									_connection.getOutputStream()
									);
				out.write(targetData);
				out.flush();
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