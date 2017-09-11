package edu.upenn.cis455.index;
import java.io.IOException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.KeyValueTextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

/*
 * This job detects location in a particular text
 * Not using this class in final model as it takes too long
 * 
 * 
 */
public class Location {
	
	/*
	 * Input to the Map function:
	 * DocID	Content
	 * 
	 * Map Emits
	 * Content.word;#;DocID	docWordCount
	 */
	public static class TokenizerMapper extends
			Mapper<Text, Text, Text, Text> {
		private Text mapKey = new Text();
		private Text mapValue = new Text();
		private NLP nlp = new NLP();
		
		public void map(Text key, Text value, Context context)
				throws IOException, InterruptedException {

			if (key == null || key.toString() == null || key.toString().trim().isEmpty()) {
				System.out.println("Empty string found");
				return;
			}
			
			String docID = key.toString().trim(); 
			String content = value.toString().trim();
			mapKey.set(docID);
			mapValue.set(NLP.findLocation(content).trim());
			context.write(mapKey, mapValue);
			
		}
	}
	
	/*
	 * Input to Reduce function:
	 * word;#;DocID	{docWordCount;#;Excerpt, docWordCount;#;Excerpt, ..., docWordCount;#;Excerpt} <word frequency>
	 * 
	 * Reduce emits:
	 * word;#;DocID	tf(term-frequency);#;Excerpt
	 */
	public static class IntSumReducer extends
			Reducer<Text, Text, Text, Text> {
		
		private Text result = new Text();
		
		public void reduce(Text key, Iterable<Text> values,
				Context context) throws IOException, InterruptedException {

			if (key == null || key.toString() == null || key.toString().trim().isEmpty()) {
				System.out.println("Empty string found");
				return;
			}
			
			String reduceValue = "";
			for(Text val : values){
				reduceValue = reduceValue + val.toString();
			}
			result.set(reduceValue);
			context.write(key, result);
		}
	}

	public static void main(String[] args) throws Exception {
		Configuration conf = new Configuration();
		conf.set("mapreduce.input.keyvaluelinerecordreader.key.value.separator", "\t");
		conf.set("mapreduce.task.timeout", "360000000");
		Job job = Job.getInstance(conf, "Term Frequency");
		job.setJarByClass(Location.class);
		job.setMapperClass(TokenizerMapper.class);
		job.setReducerClass(IntSumReducer.class);
		job.setOutputKeyClass(Text.class);
		job.setOutputValueClass(Text.class);
		job.setInputFormatClass(KeyValueTextInputFormat.class);
		FileInputFormat.addInputPath(job, new Path(args[0]));
		FileOutputFormat.setOutputPath(job, new Path(args[1]));
		System.exit(job.waitForCompletion(true) ? 0 : 1);
	}
}
