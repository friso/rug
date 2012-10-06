package nl.waredingen.moviesearch;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.apache.hadoop.io.Writable;

public class TermStatWritable implements Writable {
	private long timeslot;
	private long count;
	
	@Override
	public void write(DataOutput out) throws IOException {
		out.writeLong(timeslot);
		out.writeLong(count);
	}

	@Override
	public void readFields(DataInput in) throws IOException {
		timeslot = in.readLong();
		count = in.readLong();
	}

	public long getTimeslot() {
		return timeslot;
	}

	public void setTimeslot(long timeslot) {
		this.timeslot = timeslot;
	}

	public long getCount() {
		return count;
	}

	public void setCount(long count) {
		this.count = count;
	}
}
