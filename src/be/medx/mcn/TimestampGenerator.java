// 
// Decompiled by Procyon v0.5.29
// 

package be.medx.mcn;

import be.medx.exceptions.TechnicalConnectorException;

public interface TimestampGenerator {
	byte[] generate(String p0, String p1, byte[] p2) throws TechnicalConnectorException;
}
