// 
// Decompiled by Procyon v0.5.29
// 

package be.medx.utils;

import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPFactory;
import javax.xml.soap.SOAPFault;
import javax.xml.ws.soap.SOAPFaultException;

public final class SOAPFaultFactory {
	public static SOAPFaultException newSOAPFaultException(final String reasonText, final Throwable cause) {
		try {
			final SOAPFactory factory = SOAPFactory.newInstance();
			final SOAPFault soapFault = factory.createFault();
			soapFault.setFaultString(reasonText);
			final SOAPFaultException except = new SOAPFaultException(soapFault);
			except.initCause(cause);
			return except;
		} catch (SOAPException e) {
			throw new IllegalArgumentException(e);
		}
	}
}
