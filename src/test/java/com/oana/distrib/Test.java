package com.oana.distrib;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;

public class Test {

	public static void main(String []args) {
		
		testKeyGenerate();
	}
	
	private static void testKeyGenerate() {
		
		IntWritable key = Sort.findKey(new Text("11033203"));
		IntWritable expectedKey = new IntWritable(4);
		if(key.compareTo(expectedKey) != 0) {
			System.out.println("not equal, expected "+expectedKey.get()
											+", obtained "+key.get());
			return;
		}		
		
		key = Sort.findKey(new Text("20495341"));
		expectedKey = new IntWritable(6);
		if(key.compareTo(expectedKey) != 0) {
			System.out.println("not equal, expected "+expectedKey.get()
											+", obtained "+key.get());
		}
		System.out.println("successful test");	
		
	}
}
