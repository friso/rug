package nl.waredingen.moviesearch;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;

public class TermStatToSuggestionsReducer extends Reducer<Text, TermScoreWritable, Text, Text> {
	private Text valueTemplate = new Text();
	
	protected void reduce(Text key, Iterable<TermScoreWritable> values, Context context) throws IOException, InterruptedException {
		final Map<String, Double> scores = new HashMap<String, Double>();
		for (TermScoreWritable value : values) {
			if (scores.containsKey(value.getTerm())) {
				scores.put(value.getTerm(), value.getScore() + scores.get(value.getTerm()));
			} else {
				scores.put(value.getTerm(), value.getScore());
			}
		}
		
		ArrayList<String> list = new ArrayList<String>(scores.size());
		list.addAll(scores.keySet());
		Collections.sort(list, new Comparator<String>() {
			@Override
			public int compare(String left, String right) {
				return Double.compare(scores.get(left), scores.get(right));
			}
		});
		
		StringBuilder result = new StringBuilder();
		int c;
		for (c = 0; c < Math.min(9, list.size() - 1); c++) {
			result.append(list.get(c));
			result.append('\n');
		}
		result.append(list.get(c));
		
		valueTemplate.set(result.toString());
		
		context.write(key, valueTemplate);
	}
}
