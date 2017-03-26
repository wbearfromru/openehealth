package be.medx.mcn;

import java.io.Serializable;

import org.joda.time.DateTime;

public class Routing implements Serializable {
	private static final long serialVersionUID = -8364852115063355399L;
	private CareReceiverId careReceiver;
	private DateTime referenceDate;
	private Period period;

	public Routing() {
	}

	public Routing(final CareReceiverId careReceiver, final DateTime referenceDate) {
		this.careReceiver = careReceiver;
		this.referenceDate = referenceDate;
	}

	public Routing(final CareReceiverId careReceiver, final DateTime referenceDate, final Period period) {
		this.careReceiver = careReceiver;
		this.referenceDate = referenceDate;
		this.period = period;
	}

	public CareReceiverId getCareReceiver() {
		return this.careReceiver;
	}

	public void setCareReceiver(final CareReceiverId careReceiver) {
		this.careReceiver = careReceiver;
	}

	public DateTime getReferenceDate() {
		return this.referenceDate;
	}

	public void setReferenceDate(final DateTime referenceDate) {
		this.referenceDate = referenceDate;
	}

	public Period getPeriod() {
		return this.period;
	}

	public void setPeriod(final Period period) {
		this.period = period;
	}
}
