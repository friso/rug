package nl.waredingen.moviesearch;

import java.io.IOException;

import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.mapreduce.Reducer;

public class TermToTimeslotCombiner extends Reducer<TermTimeWritable, LongWritable, TermTimeWritable, LongWritable> {
	private LongWritable valueTemplate = new LongWritable();
	
	protected void reduce(TermTimeWritable key, Iterable<LongWritable> values, Context context) throws IOException, InterruptedException {
		long count = 0;
		for (LongWritable value : values) {
			count += value.get();
		}
		valueTemplate.set(count);
		
		context.write(key, valueTemplate);
	}
}
