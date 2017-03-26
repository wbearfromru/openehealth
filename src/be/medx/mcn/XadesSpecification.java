// 
// Decompiled by Procyon v0.5.29
// 

package be.medx.mcn;

import java.util.Map;

import org.apache.xml.security.signature.XMLSignature;
import org.w3c.dom.Element;

import be.medx.crypto.Credential;
import be.medx.exceptions.TechnicalConnectorException;
import be.medx.services.CryptoService;

public interface XadesSpecification {
	void addOptionalBeforeSignatureParts(SignedPropertiesBuilder p0, XMLSignature p1, Credential p2, String p3, Map<String, Object> p4) throws TechnicalConnectorException;

	void addOptionalAfterSignatureParts(CryptoService cryptoService, UnsignedPropertiesBuilder p0, XMLSignature p1, String p2, Map<String, Object> p3) throws TechnicalConnectorException;

	void verify(SignatureVerificationResult p0, Element p1);
}
