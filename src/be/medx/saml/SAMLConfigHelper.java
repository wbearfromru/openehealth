// 
// Decompiled by Procyon v0.5.29
// 

package be.medx.saml;

import org.slf4j.LoggerFactory;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;

import be.medx.exceptions.TechnicalConnectorException;

public class SAMLConfigHelper
{
    private static final Logger LOG;
    private static final String PROP_DELIMITER = ",";
    private static final int SAMLATTRIBUTE_EXPECTED_PROP_SIZE = 3;
    private static final int SAMLDESIGNATOR_EXPECTED_PROP_SIZE = 2;
    
    public static List<SAMLAttributeDesignator> getSAMLAttributeDesignators(final String key) throws TechnicalConnectorException {
        final List<SAMLAttributeDesignator> designators = new ArrayList<SAMLAttributeDesignator>();
        final List<String> designatorAttributes = Arrays.asList(
        	"urn:be:fgov:identification-namespace,urn:be:fgov:ehealth:1.0:certificateholder:person:ssin",
        	"urn:be:fgov:identification-namespace,urn:be:fgov:person:ssin",
        	"urn:be:fgov:certified-namespace:ehealth,urn:be:fgov:person:ssin:doctor:boolean",
        	"urn:be:fgov:certified-namespace:ehealth,urn:be:fgov:person:ssin:ehealth:1.0:doctor:nihii11",
        	"urn:be:fgov:certified-namespace:ehealth,urn:be:fgov:person:ssin:ehealth:1.0:fpsph:doctor:boolean"
        );
        for (final String attribute : designatorAttributes) {
            final String[] values = StringUtils.split(attribute, PROP_DELIMITER);
            if (values.length != SAMLDESIGNATOR_EXPECTED_PROP_SIZE) {
                SAMLConfigHelper.LOG.error("Expecting samlattributedesignator with 2 parts.[" + attribute + "]");
                throw new TechnicalConnectorException();
            }
            designators.add(new SAMLAttributeDesignator(values[1], values[0]));
        }
        return designators;
    }
    
    public static List<SAMLAttribute> getSAMLAttributes(final String key) throws TechnicalConnectorException {
        final List<SAMLAttribute> attributes = new ArrayList<SAMLAttribute>();
        final List<String> samlAttributes = Arrays.asList(
        		"urn:be:fgov:identification-namespace,urn:be:fgov:ehealth:1.0:certificateholder:person:ssin,70101109152",
        		"urn:be:fgov:identification-namespace,urn:be:fgov:person:ssin,70101109152"
            );
        for (final String attribute : samlAttributes) {
            final String[] values = StringUtils.split(attribute, PROP_DELIMITER);
            if (values.length < SAMLATTRIBUTE_EXPECTED_PROP_SIZE) {
                throw new TechnicalConnectorException();
            }
            attributes.add(new SAMLAttribute(values[1], values[0], (String[])ArrayUtils.subarray((Object[])values, 2, values.length)));
        }
        return attributes;
    }
    
    static {
        LOG = LoggerFactory.getLogger(SAMLConfigHelper.class);
    }
}
