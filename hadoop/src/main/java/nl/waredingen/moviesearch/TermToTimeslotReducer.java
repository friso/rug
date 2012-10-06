package nl.waredingen.moviesearch;

import java.io.IOException;

import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;

public class TermToTimeslotReducer extends Reducer<TermTimeWritable, LongWritable, Text, TermStatWritable> {
	private Text keyTemplate = new Text();
	private TermStatWritable valueTemplate = new TermStatWritable();
	
	protected void reduce(TermTimeWritable key, Iterable<LongWritable> values, Context context) throws IOException, InterruptedException {
		keyTemplate.set(key.getTerm());
		
		long count = 0;
		for (LongWritable value : values) {
			count += value.get();
		}
		
		valueTemplate.setTimeslot(key.getTimeslot());
		valueTemplate.setCount(count);
		
		context.write(keyTemplate, valueTemplate);
	}
}
