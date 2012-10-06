package nl.waredingen.moviesearch;

import java.io.IOException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.SequenceFile.Reader;
import org.apache.hadoop.io.Text;

import redis.clients.jedis.Jedis;

public class RedisUploader {
	private static final int ONE_HOUR = 3600;

	public static void uploadSuggestions(Configuration conf, Path source, String redisHost) throws IOException {
		FileSystem fs = FileSystem.get(conf);
		FileStatus[] inputs = fs.globStatus(source);
		
		Jedis jedis = new Jedis(redisHost);		
		jedis.connect();
		
		System.err.println("Redis uploader processing:");
		for (FileStatus status : inputs) {
			System.err.println("\t" + status.getPath());
			
			Reader reader = new Reader(fs, status.getPath(), conf);
			Text key = new Text();
			Text value = new Text();
			while (reader.next(key, value)) {
				jedis.setex(key.toString(), ONE_HOUR, value.toString());
			}
			reader.close();
		}
		
		jedis.disconnect();
		
		System.err.println("Done.");
	}
}
