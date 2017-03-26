package be.medx.mcn;

import java.security.cert.X509Certificate;
import java.util.List;

import org.apache.xml.security.exceptions.XMLSecurityException;
import org.apache.xml.security.keys.KeyInfo;

public interface Extractor {
	boolean canExtract(KeyInfo p0);

	List<X509Certificate> extract(KeyInfo p0) throws XMLSecurityException;
}
