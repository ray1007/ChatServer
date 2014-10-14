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

	ChatDataProcessor(int bufferSize){
		_container = ByteBuffer.allocate(bufferSize);
	}
}