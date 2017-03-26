package be.medx.mcn;

import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;

import org.apache.xml.security.exceptions.XMLSecurityException;
import org.apache.xml.security.keys.KeyInfo;

public class ForkedExtractor implements Extractor {
	private Extractor[] extractors;

	public ForkedExtractor(final Extractor... extractors) {
		this.extractors = extractors;
	}

	@Override
	public boolean canExtract(final KeyInfo keyinfo) {
		for (final Extractor extractor : this.extractors) {
			if (extractor.canExtract(keyinfo)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public List<X509Certificate> extract(final KeyInfo keyinfo) throws XMLSecurityException {
		for (final Extractor extractor : this.extractors) {
			if (extractor.canExtract(keyinfo)) {
				return extractor.extract(keyinfo);
			}
		}
		return new ArrayList<X509Certificate>();
	}
}
