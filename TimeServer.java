//package userver;
import java.io.*;
import java.util.*;
import java.net.*;
//import gui.*;

public class TimeServer{

	public final static int PORT = 10000;

	public static void main(String[] argv){
		long diferenceBetweenEpochs = 2208988800L;

		try(ServerSocket server = new ServerSocket(PORT)){
			while(true){
				try(Socket connection = server.accept()){
					Writer out = new OutputStreamWriter(connection.getOutputStream());
					Date now = new Date();
					long msSince1970 = now.getTime();
					long secondSince1970 = msSince1970/1000;
					long secondSince1900 = secondSince1970 + diferenceBetweenEpochs;
					byte[] time = new byte[4];
					time[0] = (byte)( secondSince1900 & 0x00000000FF000000L >> 24);
					time[0] = (byte)( secondSince1900 & 0x0000000000FF0000L >> 16);
					time[0] = (byte)( secondSince1900 & 0x000000000000FF00L >> 8);
					time[0] = (byte)( secondSince1900 & 0x00000000000000FFL);
					out.write(time + "\r\n");
					out.flush();
					connection.close();
				} catch(IOException ex){}
			}
		} catch(IOException ex){
			System.err.println(ex);
		}
	}
}