package com.oana.distrib;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class KeyDistribution {

	public static void main(String []args) throws NumberFormatException, IOException {
		
		BufferedReader reader = new BufferedReader(new FileReader("/media/tibi/work1/algorithmsResearch/eclipseWorkspace/distributedSort/Hadoop/output/part-r-00000"));
		String line = null;
		int index = 0;
		int[] keyDistr = new int[58];
		while((line = reader.readLine()) != null) {		
			String[] parsedLine = line.split("\t");			
			keyDistr[Integer.parseInt(parsedLine[0])-1]++; 
			index++;
			if(index % 1000000 == 0) {
				System.out.println(index);
			}
		}			     
		reader.close();
		for(int i=0; i<keyDistr.length; i++) {
			System.out.println("key is "+i+", nrs of values "+keyDistr[i]);
		}
	}
}
