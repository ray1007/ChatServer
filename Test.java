//package userver;
import java.io.*;
import java.nio.*;
import java.nio.channels.*;
import java.util.*;
import java.net.*;
//import gui.*;

public class Test{

	private int _a;
	private int _b;

	Test(int a, int b){
		_a = a;
		_b = b;
	}

	public void print(){ System.out.println("Data are "+_a+", "+_b); }

	public static void main(String[] args){
		Test c = new Test(5, 9);
		c.print();
	}

}