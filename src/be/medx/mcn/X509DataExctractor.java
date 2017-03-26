package be.medx.mcn;

import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;

import org.apache.xml.security.exceptions.XMLSecurityException;
import org.apache.xml.security.keys.KeyInfo;
import org.apache.xml.security.keys.content.X509Data;

public class X509DataExctractor implements Extractor {
	@Override
	public boolean canExtract(final KeyInfo keyinfo) {
		return keyinfo.containsX509Data();
	}

	@Override
	public List<X509Certificate> extract(final KeyInfo keyInfo) throws XMLSecurityException {
		final List<X509Certificate> result = new ArrayList<X509Certificate>();
		for (int i = 0; i < keyInfo.lengthX509Data(); ++i) {
			final X509Data data = keyInfo.itemX509Data(i);
			for (int j = 0; j < data.lengthCertificate(); ++j) {
				result.add(data.itemCertificate(j).getX509Certificate());
			}
		}
		return result;
	}
}
