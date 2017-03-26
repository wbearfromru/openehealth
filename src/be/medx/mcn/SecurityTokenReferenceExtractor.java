package be.medx.mcn;

import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;

import org.apache.xml.security.exceptions.XMLSecurityException;
import org.apache.xml.security.keys.KeyInfo;

public class SecurityTokenReferenceExtractor implements Extractor {
	@Override
	public boolean canExtract(final KeyInfo keyinfo) {
		return keyinfo.length("http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd", "SecurityTokenReference") > 0;
	}

	@Override
	public List<X509Certificate> extract(final KeyInfo keyInfo) throws XMLSecurityException {
		final List<X509Certificate> result = new ArrayList<X509Certificate>();
		result.add(keyInfo.getX509Certificate());
		return result;
	}
}
