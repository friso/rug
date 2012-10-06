package nl.waredingen.moviesearch;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.apache.hadoop.io.WritableComparable;

public class TermTimeWritable implements WritableComparable<TermTimeWritable> {
	private String term;
	private long timeslot;
	
	@Override
	public void write(DataOutput out) throws IOException {
		out.writeUTF(term);
		out.writeLong(timeslot);
	}

	@Override
	public void readFields(DataInput in) throws IOException {
		term = in.readUTF();
		timeslot = in.readLong();
	}

	@Override
	public int compareTo(TermTimeWritable o) {
		int termDifference = term.compareTo(o.term);
		return termDifference == 0 ? (timeslot == o.timeslot ? 0 : timeslot < o.timeslot ? -1 : 1) : termDifference;
	}

	public String getTerm() {
		return term;
	}

	public void setTerm(String term) {
		this.term = term;
	}

	public long getTimeslot() {
		return timeslot;
	}

	public void setTimeslot(long timeslot) {
		this.timeslot = timeslot;
	}
}
