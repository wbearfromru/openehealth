// 
// Decompiled by Procyon v0.5.29
// 

package be.medx.mcn;

import be.medx.exceptions.TechnicalConnectorException;

public interface BlobBuilder {
	public static final String PROJECT_NAME_KEY = "projectName";

	Blob build(byte[] p0) throws TechnicalConnectorException;

	Blob build(byte[] p0, String p1) throws TechnicalConnectorException;

	Blob build(byte[] p0, String p1, String p2, String p3) throws TechnicalConnectorException;

	Blob build(byte[] p0, String p1, String p2, String p3, String p4) throws TechnicalConnectorException;

	byte[] checkAndRetrieveContent(Blob p0) throws TechnicalConnectorException;
}
