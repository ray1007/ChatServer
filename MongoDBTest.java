//package userver;
import java.io.*;
import java.nio.*;
import java.nio.channels.*;
import java.util.*;
import java.net.*;
import com.mongodb.*;
//import gui.*;

public class MongoDBTest{

	//MongoDBTest(){
	//}

	public static void main(String[] args) throws UnknownHostException {
		MongoClient m = new MongoClient("localhost", 30000);
		DB db = m.getDB("test_db");

		db.getCollection("my_first").insert(new BasicDBObject("i", 1));
		for(String s : m.getDatabaseNames()){
			System.out.println(s);
		}
	}
}