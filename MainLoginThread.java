package archimedesServer;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.logging.*;
import java.nio.*;
import java.nio.channels.*;
//import gui.*;

public class MainLoginThread extends Thread {
	private final static int LOGIN_PORT = 10000;

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
			//_logger.log(Level.SEVERE, "Can't open _loginSSocket.", ex);
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
 				System.out.println(account+", "+ password);
 				boolean auth = false;
 				if( (account.equals("Patrick") && password.equals("TextBox")) ||
 					(account.equals("Ray") && password.equals("localnm")) ||
 					(account.equals("wayu") && password.equals("pusheen")) 	){
 					auth = true;
 				}

				Writer out = new OutputStreamWriter(_connection.getOutputStream(), "UTF-8");
				if(auth){
					// Member confirmed.
					out.write(1); // If client logined successfully, write back (byte)(int 1)
					out.flush();
						
					// Redirect to port 3333.
					int newPort = 3333;
					for(int i=0;i<4;++i)
						out.write((byte) (newPort >> (8*i)));

					// Update some data
						
					out.write("Welcome! How was your head? I heard that you hit yourself last night.:) -- Archimedes.");
					out.flush();
					System.out.println("Login success. acc="+account+", pas="+password+"\r\n");
				} else{// Member not confirmed.
					out.write(0); // If client logined unsuccessfully, write back (byte)(int 0)
					out.flush();
					out.write("You are not a member yet. Sign up and enjoy! :) -- Archimedes.");
					out.flush();
				}
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