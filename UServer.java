//package userver;
import java.io.*;
import java.util.*;
import java.net.*;
import gui.*;
/**
 * uSocket chatroom server class
 * @author StarryDawn
 */
public class DaytimeServer{

	public final statiuc int PORT = 10000;

	public static void main(String[] argv){
		try(ServerSocket server = new ServerSocket(PORT)){
			while(true){
				try(Socket connection = server.accept()){
					Writer out = new OutputStreamWriter(connection.getOutputStream());
					Date now = new Date();
					out.write(now.toString() + "\r\n");
					out.flush();
					connection.close();
				}catch(IOexception ex){}
			}
		} catch(IOexception ex){
			System.err.println(ex);
		}
	}
}
