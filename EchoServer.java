//package userver;
import java.io.*;
import java.util.*;
import java.net.*;
import java.nio.*;
import java.nio.charset.*;
import java.nio.channels.*;
//import gui.*;

public class EchoServer{

	public final static int PORT = 10000;
	public static Charset charset = Charset.forName("UTF-8");
	public static CharsetDecoder decoder = charset.newDecoder();

	public static void main(String[] args){
		int port;
		int count = 0;

		try{
			port = Integer.parseInt(args[0]);
		} catch (RuntimeException ex){
			port = PORT;
		}
		System.out.println("Linstening for connection on port "+port);

		ServerSocketChannel serverChannel;
		Selector selector;
		try{
			serverChannel = ServerSocketChannel.open();
			ServerSocket ss = serverChannel.socket();
			InetSocketAddress address = new InetSocketAddress(port);
			ss.bind(address);
			serverChannel.configureBlocking(false);
			selector = Selector.open();
			serverChannel.register(selector, SelectionKey.OP_ACCEPT);
		} catch(IOException ex){
			ex.printStackTrace();
			return;
		}

		while(true){
			try{
				selector.select();
			} catch(IOException ex){
				ex.printStackTrace();
				break;
			}

			Set<SelectionKey> readyKeys = selector.selectedKeys();
			Iterator<SelectionKey> iterator = readyKeys.iterator();
			while(iterator.hasNext()){
				SelectionKey key = iterator.next();
				iterator.remove();
				try{
					if(key.isAcceptable()){
						ServerSocketChannel server = (ServerSocketChannel) key.channel();
						SocketChannel client = server.accept();
						System.out.println("Accepted connection from "+client);
						client.configureBlocking(false);
						SelectionKey clientKey = client.register(selector, SelectionKey.OP_WRITE | SelectionKey.OP_READ);
						ByteBuffer buffer = ByteBuffer.allocate(100);
						clientKey.attach(buffer);					
					}
					if(key.isReadable()){
						SocketChannel client = (SocketChannel) key.channel();
						ByteBuffer output = (ByteBuffer) key.attachment();
						client.read(output);
					}
					if(key.isWritable()){
						//ServerSocketChannel server = (ServerSocketChannel) key.channel();
						SocketChannel client = (SocketChannel) key.channel();
						ByteBuffer output = (ByteBuffer) key.attachment();
						output.flip();
						client.write(output);
						//output.clear();
						//String data="";
						//int o = buffer.position();
						//data = decoder.decode(output).toString();
						/*if(!output.hasRemaining()){
							count++;
							output.clear();
							output.put((byte) 'c');
							output.put((byte) 'd');
							output.put((byte) '\r');
							output.put((byte) '\n');
							//output.clear();
							//byte[] data1 = "Test my output".getBytes("UTF_8");
							//ByteBuffer buf = ByteBuffer.wrap(data1);
							//client.write(buf);
							//output.put((byte) 'c');
							//output.put((byte) 'd');
							output.flip();
							client.write(output);
							System.out.println(count+"\n");
						}*/
						
						//
						output.compact();
					}
				} catch(IOException ex){
					key.cancel();
					try{
						key.channel().close();
					} catch (IOException cex){}
				}
			}
		}
	}
}