package edu.upenn.cis455.index;
import java.io.IOException;
import java.util.StringTokenizer;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.KeyValueTextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

import com.google.common.collect.Iterables;


/*
 * This job converts the counts the occurrence of anchor tags (classified by different FEATURE eg. Title)
 * in a text of a particular docID. The result of this job with later be joined to main index
 * 
 * 
 */
public class AnchorTag {
	
	private static String FEATURE = "KEYWORD";
	/*
	 * Input to the Map function:
	 * DocID	Anchor Content
	 * 
	 * Map Emits
	 * word;#;DocID	docWordCount
	 */
	public static class TokenizerMapper extends
			Mapper<Text, Text, Text, Text> {

		private Text mapKey = new Text();
		private Text mapValue = new Text();
		private final static String FIELD_DELIMITER = ";#;";
		
		public void map(Text key, Text value, Context context)
				throws IOException, InterruptedException {

			if (key == null || key.toString() == null || key.toString().trim().isEmpty()) {
				System.out.println("Empty string found");
				return;
			}
			
			String docID = key.toString().trim();
			String content = value.toString().trim();
			content = NLP.removeSpace(content);
			content = NLP.process(content);

			StringTokenizer itr = new StringTokenizer(content);
			
			while (itr.hasMoreTokens()) {
				String word = itr.nextToken().trim();
				StringBuffer keyOut = new StringBuffer();
				keyOut.append(NLP.stem(word));
				keyOut.append(FIELD_DELIMITER);
				keyOut.append(docID);
				mapKey.set(keyOut.toString());
				mapValue.set("1");
				context.write(mapKey, mapValue);
			}
		}
	}
	
	/*
	 * Input to Reduce function:
	 * word;#;DocID		{"1", "1", ...} <word frequency>
	 * 
	 * Reduce emits:
	 * word;#;DocID		FEATURE@word-count
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

			int count = Iterables.size(values);
			StringBuffer resultBuffer = new StringBuffer();
			resultBuffer.append(FEATURE);
			resultBuffer.append("@");
			resultBuffer.append(Integer.toString(count));
			result.set(resultBuffer.toString());
			context.write(key, result);
			
		}
	}

	public static void main(String[] args) throws Exception {
		Configuration conf = new Configuration();
		conf.set("mapreduce.input.keyvaluelinerecordreader.key.value.separator", "\t");
		Job job = Job.getInstance(conf, "Anchor Tag");
		job.setJarByClass(AnchorTag.class);
		job.setMapperClass(TokenizerMapper.class);
		job.setReducerClass(IntSumReducer.class);
		job.setOutputKeyClass(Text.class);
		job.setOutputValueClass(Text.class);
		job.setInputFormatClass(KeyValueTextInputFormat.class);
		FileInputFormat.addInputPath(job, new Path(args[0]));
		FileOutputFormat.setOutputPath(job, new Path(args[1]));
		// FEATURE = args[2].trim();
		System.exit(job.waitForCompletion(true) ? 0 : 1);
	}
	
	
}
