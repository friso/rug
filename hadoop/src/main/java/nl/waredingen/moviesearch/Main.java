package nl.waredingen.moviesearch;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.fs.PathFilter;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.SequenceFileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.SequenceFileOutputFormat;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;

public class Main extends Configured implements Tool {

	private static final String OPTION_REDIS = "redis";
	private static final String OPTION_OLDLOGDIR = "oldlogdir";
	private static final String OPTION_LOGDIR = "logdir";
	private static final String OPTION_OUTPUT = "output";
	private static final String TERMCOUNTS_DIR_SUFFIX = "/termcounts/";
	private static final String SUGGESTIONS_SUFFIX = "/suggestions";

	@Override
	public int run(String[] args) throws Exception {
		CommandLine cmd = setupCommandLineParsing(args);
		FileSystem fs = FileSystem.get(getConf());
		
		FileStatus[] logInputs = getLogFileList(cmd, fs);
		if (logInputs.length == 0) {
			//nothing to do
			return 0;
		}
		
		if (!runLogProcessingJob(cmd, fs, logInputs)) {
			return 1;
		}
		
		if (!runTermCountProcessingJob(cmd, fs)) {
			return 1;
		}
		
		RedisUploader.uploadSuggestions(getConf(), new Path(cmd.getOptionValue(OPTION_OUTPUT) + SUGGESTIONS_SUFFIX + "/part*"), cmd.getOptionValue(OPTION_REDIS));
		
		return 0;
	}

	private FileStatus[] getLogFileList(CommandLine cmd, FileSystem fs) throws IOException {
		FileStatus[] logInputs = fs.listStatus(new Path(cmd.getOptionValue(OPTION_LOGDIR)), new PathFilter() {
			@Override
			public boolean accept(Path path) {
				return !path.getName().endsWith(".tmp");
			}
		});
		return logInputs;
	}

	private boolean runTermCountProcessingJob(CommandLine cmd, FileSystem fs) throws IOException, InterruptedException, ClassNotFoundException {
		FileStatus[] termcountInputs = fs.listStatus(new Path(cmd.getOptionValue(OPTION_OUTPUT) + TERMCOUNTS_DIR_SUFFIX));
		Path suggestionsOutput = new Path(cmd.getOptionValue(OPTION_OUTPUT) + SUGGESTIONS_SUFFIX);
		if (fs.exists(suggestionsOutput)) {
			fs.delete(suggestionsOutput, true);
		}
		
		Job statToSuggestionsJob = createStatToSuggestionsJob(termcountInputs, suggestionsOutput);
		
		return statToSuggestionsJob.waitForCompletion(true);
	}

	private boolean runLogProcessingJob(CommandLine cmd, FileSystem fs, FileStatus[] logInputs) throws IOException, InterruptedException, ClassNotFoundException {
		printProcessingMessage(logInputs);
		
		Path termToTimeslotOutput = new Path(
				cmd.getOptionValue(OPTION_OUTPUT) + TERMCOUNTS_DIR_SUFFIX + 
				new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss").format(new Date()));
		Job termToTimeslotJob = createTermToTimeslotJob(logInputs, termToTimeslotOutput);
		
		if (termToTimeslotJob.waitForCompletion(true)) {
			for (FileStatus status : logInputs) {
				fs.rename(status.getPath(), new Path(cmd.getOptionValue(OPTION_OLDLOGDIR) + "/" + status.getPath().getName()));
			}
			return true;
		} else {
			return false;
		}
	}

	private void printProcessingMessage(FileStatus[] logInputs) {
		System.err.println("Processing:");
		for (FileStatus status : logInputs) {
			System.err.println("\t" + status.getPath().toString());
		}
	}

	@SuppressWarnings("static-access")
	private CommandLine setupCommandLineParsing(String[] args) throws ParseException {
		Options options = new Options();
		options.addOption(OptionBuilder
				.withArgName(OPTION_LOGDIR)
				.hasArg()
				.withDescription("HDFS directory that Flume leaves the log files in.")
				.create(OPTION_LOGDIR));
		
		options.addOption(OptionBuilder
				.withArgName(OPTION_OUTPUT)
				.hasArg()
				.withDescription("HDFS output path; will be overwritten.")
				.create(OPTION_OUTPUT));
		
		options.addOption(OptionBuilder
				.withArgName("old logdir")
				.hasArg()
				.withDescription("Destination to move the log files to once processed.")
				.create(OPTION_OLDLOGDIR));
		
		options.addOption(OptionBuilder
				.withArgName("redis hostname")
				.hasArg()
				.withDescription("Host name of the redis instance.")
				.create(OPTION_REDIS));
		
		CommandLineParser parser = new PosixParser();
		CommandLine cmd = parser.parse(options, args);
		return cmd;
	}
	
	private Job createStatToSuggestionsJob(FileStatus[] termcountInputs, Path suggestionsOutput) throws IOException {
		Job job = new Job(getConf());
		
		job.setJarByClass(Main.class);
		job.setJobName("Term counts to suggestions job");
		
		job.setInputFormatClass(SequenceFileInputFormat.class);
		FileInputFormat.setInputPaths(job, fileStatusToPathArray(termcountInputs));
		
		job.setOutputFormatClass(SequenceFileOutputFormat.class);
		FileOutputFormat.setOutputPath(job, suggestionsOutput);
		
		job.setMapOutputKeyClass(Text.class);
		job.setMapOutputValueClass(TermScoreWritable.class);
		job.setMapperClass(TermStatToSuggestionsMapper.class);
		
		job.setOutputKeyClass(Text.class);
		job.setOutputValueClass(Text.class);
		job.setReducerClass(TermStatToSuggestionsReducer.class);
		
		return job;
	}

	private Job createTermToTimeslotJob(FileStatus[] inputs, Path output) throws IOException {
		Job job = new Job(getConf());
		
		job.setJarByClass(Main.class);
		job.setJobName("Term count per time slot job");
		
		job.setInputFormatClass(TextInputFormat.class);
		FileInputFormat.setInputPaths(job, fileStatusToPathArray(inputs));
		
		job.setOutputFormatClass(SequenceFileOutputFormat.class);
		FileOutputFormat.setOutputPath(job, output);
		
		job.setMapOutputKeyClass(TermTimeWritable.class);
		job.setMapOutputValueClass(LongWritable.class);
		job.setMapperClass(TermToTimeslotMapper.class);
		
		job.setOutputKeyClass(Text.class);
		job.setOutputValueClass(TermStatWritable.class);
		job.setReducerClass(TermToTimeslotReducer.class);
		job.setCombinerClass(TermToTimeslotCombiner.class);
		
		return job;
	}

	private Path[] fileStatusToPathArray(FileStatus[] inputs) {
		Path[] inputPaths = new Path[inputs.length];
		for (int c = 0; c < inputs.length; c++) {
			inputPaths[c] = inputs[c].getPath();
		}
		return inputPaths;
	}

	public static void main(String args[]) {
		try {
			System.exit(ToolRunner.run(new Main(), args));
		} catch (Exception e) {
			System.err.println("Error: " + e.getMessage());
			e.printStackTrace(System.err);
			System.exit(1);
		}
	}
}
