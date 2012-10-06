package nl.waredingen.moviesearch;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.util.StringUtils;

public class TermToTimeslotMapper extends Mapper<LongWritable, Text, TermTimeWritable, LongWritable> {
	private final static long FIFTEEN_MINUTES = 1000L * 60 * 15;
	
	private TermTimeWritable keyTemplate = new TermTimeWritable();
	private LongWritable valueTemplate = new LongWritable(1L);
	
	//This is OK, because Mappers are single threaded
	private final DateFormat format;
	
	public TermToTimeslotMapper() {
		format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSS'Z'");
		format.setTimeZone(TimeZone.getTimeZone("UTC"));
	}
	
	protected void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {
		try {
			/*
			 * log line sample:
			 * 127.0.0.1	2012-07-01T06:00:29.500Z	GET /search?q=Grumpier+Old+Men HTTP/1.1	200	Grumpier Old Men
			 */
			String[] parts = StringUtils.split(value.toString(), '\001', '\t');
			if (!"-".equals(parts[4])) {
				Date logDate = format.parse(parts[1]);
				long timeslot = logDate.getTime() - (logDate.getTime() % FIFTEEN_MINUTES);
				
				keyTemplate.setTerm(parts[4].toLowerCase());
				keyTemplate.setTimeslot(timeslot);
								
				context.write(keyTemplate, valueTemplate);
			}
		} catch (Exception e) {
			System.err.println("Mapper error: " + e.getMessage());
			e.printStackTrace(System.err);
		}
	}
}
