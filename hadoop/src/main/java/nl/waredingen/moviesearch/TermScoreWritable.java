package nl.waredingen.moviesearch;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.apache.hadoop.io.Writable;

public class TermScoreWritable implements Writable {
	private String term;
	private double score;
	
	@Override
	public void write(DataOutput out) throws IOException {
		out.writeUTF(term);
		out.writeDouble(score);
	}

	@Override
	public void readFields(DataInput in) throws IOException {
		term = in.readUTF();
		score = in.readDouble();
	}

	public String getTerm() {
		return term;
	}

	public void setTerm(String term) {
		this.term = term;
	}

	public double getScore() {
		return score;
	}

	public void setScore(double score) {
		this.score = score;
	}
}
