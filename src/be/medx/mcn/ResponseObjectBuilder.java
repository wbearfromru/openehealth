// 
// Decompiled by Procyon v0.5.29
// 

package be.medx.mcn;

import java.util.Map;

import be.cin.nip.async.generic.GetResponse;
import be.cin.nip.async.generic.PostResponse;
import be.medx.exceptions.TechnicalConnectorException;

public interface ResponseObjectBuilder {
	boolean handlePostResponse(PostResponse p0) throws TechnicalConnectorException;

	Map<Object, SignatureVerificationResult> handleGetResponse(GetResponse p0) throws TechnicalConnectorException;
}
