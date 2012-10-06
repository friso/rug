package nl.waredingen.moviesearch;

import java.io.IOException;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

public class TermStatToSuggestionsMapper extends Mapper<Text, TermStatWritable, Text, TermScoreWritable> {
	private Text keyTemplate = new Text();
	private TermScoreWritable valueTemplate = new TermScoreWritable();
	
	protected void map(Text key, TermStatWritable value, Context context) throws IOException, InterruptedException {
		byte[] termBytes = key.getBytes();
		for (int c = 1; c < Math.min(15, termBytes.length); c++) {
			keyTemplate.set(termBytes, 0, c);
			valueTemplate.setTerm(key.toString());
			valueTemplate.setScore(scoreForTerm(value));
			
			context.write(keyTemplate, valueTemplate);
		}
	}

	private double scoreForTerm(TermStatWritable value) {
		long ago = System.currentTimeMillis() - value.getTimeslot();
		return (double) value.getCount() / ago;
	}
}
