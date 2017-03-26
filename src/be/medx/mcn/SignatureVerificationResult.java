package be.medx.mcn;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.ArrayUtils;
import org.bouncycastle.tsp.TimeStampToken;
import org.joda.time.DateTime;

import be.medx.exceptions.TechnicalConnectorException;
import be.medx.utils.TimestampUtil;

public class SignatureVerificationResult implements Serializable {
	private static final long serialVersionUID = 1L;
	private transient List<TimeStampToken> tsTokens;
	private List<Byte[]> serTsTokens;
	private X509Certificate signingCert;
	private List<X509Certificate> certChain;
	private DateTime signingTime;
	private List<DateTime> timestampGenTime;
	private Set<SignatureVerificationError> errors;

	public SignatureVerificationResult() {
		this.tsTokens = new ArrayList<TimeStampToken>();
		this.certChain = new ArrayList<X509Certificate>();
		this.timestampGenTime = new ArrayList<DateTime>();
		this.errors = new TreeSet<SignatureVerificationError>();
	}

	public boolean isValid() {
		return this.errors.isEmpty();
	}

	public Set<SignatureVerificationError> getErrors() {
		return this.errors;
	}

	public X509Certificate getSigningCert() {
		return this.signingCert;
	}

	public void setSigningCert(final X509Certificate signingCert) {
		this.signingCert = signingCert;
	}

	public DateTime getSigningTime() {
		return this.signingTime;
	}

	public void setSigningTime(final DateTime signingTime) {
		this.signingTime = signingTime;
	}

	public List<DateTime> getTimestampGenTimes() {
		return this.timestampGenTime;
	}

	public DateTime getVerifiedSigningTime(final int amount, final TimeUnit unit) {
		for (final DateTime genTime : this.timestampGenTime) {
			final DateTime start = genTime.minus(unit.toMillis(amount));
			final DateTime end = genTime.plus(unit.toMillis(amount));
			if (!this.signingTime.isBefore(start) && !this.signingTime.isAfter(end)) {
				return this.signingTime;
			}
		}
		return new DateTime();
	}

	public List<TimeStampToken> getTsTokens() {
		return this.tsTokens;
	}

	public List<X509Certificate> getCertChain() {
		return this.certChain;
	}

	private void writeObject(final ObjectOutputStream out) throws IOException {
		this.serTsTokens = new ArrayList<Byte[]>();
		for (final TimeStampToken tsToken : this.tsTokens) {
			this.serTsTokens.add(ArrayUtils.toObject(tsToken.getEncoded()));
		}
		out.defaultWriteObject();
		this.serTsTokens = null;
	}

	private void readObject(final ObjectInputStream in) throws IOException, ClassNotFoundException {
		in.defaultReadObject();
		this.tsTokens = new ArrayList<TimeStampToken>();
		if (this.serTsTokens != null) {
			for (final Byte[] serToken : this.serTsTokens) {
				try {
					this.tsTokens.add(TimestampUtil.getTimeStampToken(ArrayUtils.toPrimitive(serToken)));
				} catch (TechnicalConnectorException e) {
					throw new IOException(e);
				}
			}
		}
		this.serTsTokens = null;
	}
}
