package com.oana.distrib;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
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
import org.apache.log4j.Logger;

public class Sort {	
	
	private Logger logger = Logger.getLogger(Sort.class);
	
//	public static final int NR_BLOCKS = 1; 
//	public static final int NR_BLOCKS = 2048 / 128 - 1; 
	public static final int NR_BLOCKS = 50;
	public static final String DELIM = ",";
	public static final int MAX_NR = 200000000;
//	public static final int MAX_NR = 4;
	
	
	static IntWritable findKey(Text value) {
		
		int chunk = (int) Math.ceil(MAX_NR / (double) NR_BLOCKS);
		int val = Integer.parseInt(value.toString());
		for(int i=1; i <= NR_BLOCKS; i++) {
			if(val <= chunk * i) {
				return new IntWritable(i);
			}
		}
		throw new RuntimeException("Key not found in findKey function for value "+value.toString());
	}
	
	
	public static class TokenizerMapper extends Mapper<Object, Text, IntWritable, IntWritable>{

		private Logger logger = Logger.getLogger(TokenizerMapper.class);				
			 
		Text value = new Text();
	
	 public void map(Object key, Text value, Context context) throws IOException, InterruptedException {
		 
//		 logger.info("Started mapping");		 
		 
	   StringTokenizer itr = new StringTokenizer(value.toString());	   
	   while (itr.hasMoreTokens()) {	     	     
	     value.set(itr.nextToken());
//	     logger.info("input nr to mapper "+value.toString());
//	     IntWritable outputKey = Sort.findKey(value, outputKeys);
	     IntWritable outputKey = Sort.findKey(value);
	     context.write(outputKey, new IntWritable(Integer.parseInt(value.toString())));	     
	   }
//	   logger.info("+++++++++++++++++++++++++++++++++++++++++");
//	   logger.info("Finished map phase");
	 }
	}
	
	public static class IntSortReducer extends Reducer<IntWritable, IntWritable,
													IntWritable, IntWritable> {

		private Logger logger = Logger.getLogger(IntSortReducer.class);				
		
		private ArrayList<Integer> result = new ArrayList<Integer>();
		
		public void reduce(IntWritable key, Iterable<IntWritable> values, Context context) 
			throws IOException, InterruptedException {
		
			logger.info("starting reducer");
			
			for (IntWritable val : values) {
//				logger.info("adding number to sorting "+val.get());
				result.add(new Integer(val.get()));													
			}						
			
			Collections.sort(result);
			
			logger.info("finished sorting");
			for(int i=0; i<result.size(); i++) {
//				logger.info("writing sorted nr key "+key.get()+", val "+result.get(i));
				context.write(key, new IntWritable(result.get(i)));
			}				
			logger.info("Finished reduce phase");
		}
	}
	
	public static void main(String[] args) throws Exception {
	 
		
		 long startAp = System.currentTimeMillis();		
		 Configuration conf = new Configuration();
		 Job job = Job.getInstance(conf, "distrib sort");
		 job.setJarByClass(Sort.class);
		 job.setMapperClass(TokenizerMapper.class);
//		 job.setCombinerClass(IntSumReducer.class);
		 job.setReducerClass(IntSortReducer.class);
		 int nrReduceTasks = NR_BLOCKS;
		 job.setNumReduceTasks(nrReduceTasks);
		 job.setOutputKeyClass(IntWritable.class);
		 job.setOutputValueClass(IntWritable.class);
		 FileInputFormat.addInputPath(job, new Path(args[0]));
		 FileOutputFormat.setOutputPath(job, new Path(args[1]));
		 System.exit(job.waitForCompletion(true) ? 0 : 1);
		 long stopAp = System.currentTimeMillis();
		 long delta = (stopAp - startAp) / 1000;
		 System.out.println("%%%%%%%%%%%%%%%%%%%%%%%%%%");
		 System.out.println("time is "+delta);
	}
}
