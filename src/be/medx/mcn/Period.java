package be.medx.mcn;

import java.io.Serializable;

import org.joda.time.DateTime;

public class Period implements Serializable {
	private static final long serialVersionUID = 7658819844468010644L;
	private DateTime begin;
	private DateTime end;

	public Period(final DateTime begin, final DateTime end) {
		this.begin = begin;
		this.end = end;
	}

	public DateTime getBegin() {
		return this.begin;
	}

	public void setBegin(final DateTime begin) {
		this.begin = begin;
	}

	public DateTime getEnd() {
		return this.end;
	}

	public void setEnd(final DateTime end) {
		this.end = end;
	}
}
