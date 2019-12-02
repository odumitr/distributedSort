package com.oana.distrib;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.StringTokenizer;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

public class ModifiedSort {

	public static final int NR_BLOCKS = 1; 
//	public static final int NR_BLOCKS = 2048 / 128 - 1; 
	public static final String DELIM = ",";
//	public static final int MAX_NR = 200000000;
	public static final int MAX_NR = 4;
	
	private static IntWritable[] generateKeys() {
		
		IntWritable[] keys = new IntWritable[NR_BLOCKS];
		for(int i=0; i < NR_BLOCKS; i++) {
			keys[i] = new IntWritable(i+1);
		}
		return keys;
	}
	
	private static IntWritable findKey(Text value, IntWritable[] outputKeys) {
		
		return outputKeys[0];
	}
	
	public static class TokenizerMapper extends Mapper<Object, Text, Text, IntWritable>{

	 IntWritable[] outputKeys = generateKeys();		 
	 Text value = new Text();
	
	 public void map(Object key, Text value, Context context) throws IOException, InterruptedException {
		 
	   StringTokenizer itr = new StringTokenizer(value.toString(), DELIM);	   
	   while (itr.hasMoreTokens()) {	     	     
	     value.set(itr.nextToken());
	     System.out.println(value.toString());
	     IntWritable outputKey = ModifiedSort.findKey(value, outputKeys);
	     value.set(String.valueOf(outputKey.get()));
	     IntWritable outputInt = new IntWritable(Integer.parseInt(value.toString()));
	     System.out.println("key is "+value.toString()+", value is "+outputInt.get());
	     context.write(value, outputInt);	     
	   }
	   System.out.println("+++++++++++++++++++++++++++++++++++++++++");
	   System.out.println("Finished map phase");
	 }
	}
	
	public static class IntSortReducer extends Reducer<Text, IntWritable,
													Text, IntWritable> {
	 
		 private ArrayList<IntWritable> result = new ArrayList<IntWritable>();
	
		 public void reduce(Text key, Iterable<IntWritable> values, Context context) 
				 								throws IOException, InterruptedException {
			  			 
			 for (IntWritable val : values) {
				 result.add(val);
			 }
			 Collections.sort(result);
			 
			 for(int i=0; i<result.size(); i++) {
				 context.write(key, result.get(i)); 
			 }			 
			 System.out.println("Finished reduce phase");
		 }
	}
	
	public static void main(String[] args) throws Exception {
	 
		 Configuration conf = new Configuration();
		 Job job = Job.getInstance(conf, "distrib sort");
		 job.setJarByClass(ModifiedSort.class);
		 job.setMapperClass(TokenizerMapper.class);
//		 job.setCombinerClass(IntSumReducer.class);
		 job.setReducerClass(IntSortReducer.class);
		 job.setOutputKeyClass(IntWritable.class);
		 job.setOutputValueClass(ArrayList.class);
		 job.getMapOutputValueClass();
		 FileInputFormat.addInputPath(job, new Path(args[0]));
		 FileOutputFormat.setOutputPath(job, new Path(args[1]));
		 System.exit(job.waitForCompletion(true) ? 0 : 1);
	}
}
