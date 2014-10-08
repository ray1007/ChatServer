//package userver;
import java.io.*;
import java.util.*;
import java.net.*;
//import gui.*;

public class DaytimeServer{

	public final static int PORT = 10000;
	public static void main(String[] argv){
		try(ServerSocket server = new ServerSocket(PORT)){
			while(true){
				try(Socket connection = server.accept()){
					Writer out = new OutputStreamWriter(connection.getOutputStream());
					Date now = new Date();
					out.write(now.toString() + "\r\n");
					out.flush();
					connection.close();
				} catch(IOException ex){}
			}
		} catch(IOException ex){
			System.err.println(ex);
		}
	}
}