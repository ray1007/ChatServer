//package userver;
import java.io.*;
import java.nio.*;
import java.nio.channels.*;
import java.util.*;
import java.net.*;
import java.lang.reflect.Array;
//import gui.*;

public class ChatDataProcessor {
	private ByteBuffer _container;

	ChatDataProcessor(){}

	public ByteBuffer getByteBuffer(){ return _container; }

	public void parse(ByteBuffer rawInput){
		_container = rawInput;
		//Byte[] rawBytes = _container.array();
		//int typeId = (int)_container.get();
		int typeId = (int) _container.array()[0];
		
		//Byte[] usrId = Byte[4];
		switch(typeId){
			case 0: // Data type is "text".
				//just return.
				break;
			case 2: // Data type is "file".
				try{
					File file = new File("C:/Users/nmlab/newFile");
					if(!file.exists())
						file.createNewFile();
				
					OutputStream out;
				
					out = new BufferedOutputStream(
							new FileOutputStream(file)
							);
					out.write(_container.array(), 5, Array.getLength(_container.array())-5);
					out.flush();
					out.close();
				} catch(IOException ex){ ex.printStackTrace(); 
				} //catch(FileNotFoundException fnfex){ System.out.println("No such file!"); }

			default:
		}
	}
}