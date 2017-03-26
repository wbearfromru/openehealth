// 
// Decompiled by Procyon v0.5.29
// 

package be.medx.mcn;

import org.joda.time.DateTime;

import be.medx.exceptions.TechnicalConnectorException;

public interface CommonBuilder {
	public static final String PROJECT_NAME_KEY = "projectName";

	Routing createRouting(Patient p0, DateTime p1);

	CommonInput createCommonInput(PackageInfo p0, boolean p1, String p2) throws TechnicalConnectorException;

	Origin createOrigin(PackageInfo p0) throws TechnicalConnectorException;

	Routing createRoutingToMutuality(String p0, DateTime p1);
}
