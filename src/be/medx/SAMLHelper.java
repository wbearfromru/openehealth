package be.medx;

import org.w3c.dom.NodeList;
import org.joda.time.DateTime;
import org.w3c.dom.Element;

public final class SAMLHelper
{
    public static final String SAML_CONDITIONS = "Conditions";
    public static final String SAML_NOTONORAFTER = "NotOnOrAfter";
    public static final String SAML_SUCCESS = "Success";
    public static final String SAML_STATUSCODE = "StatusCode";
    public static final String SAML_STATUSMESSAGE = "StatusMessage";
    public static final String SAML_VALUE = "Value";
    public static final String SAML_ASSERTION = "Assertion";
    public static final String SAML_ATTRIBUTESTATEMENT = "AttributeStatement";
    public static final String SAML_ATTRIBUTE = "Attribute";
    
    public static String getStatusCode(final Element stsResponse) {
        return stsResponse.getElementsByTagNameNS("*", "StatusCode").item(0).getAttributes().getNamedItem("Value").getNodeValue();
    }
    
    public static String getStatusMessage(final Element stsResponse) {
        return stsResponse.getElementsByTagNameNS("*", "StatusMessage").item(0).getTextContent();
    }
    
    public static DateTime getNotOnOrAfterCondition(final Element stsResponse) {
        return DateUtils.parseDateTime(stsResponse.getElementsByTagNameNS("*", "Conditions").item(0).getAttributes().getNamedItem("NotOnOrAfter").getTextContent());
    }
    
    public static NodeList getAttributes(final Element stsResponse) {
        return ((Element)stsResponse.getElementsByTagNameNS("*", "AttributeStatement").item(0)).getElementsByTagNameNS("*", "Attribute");
    }
    
    public static Element getAssertion(final Element stsResponse) {
        return (Element)stsResponse.getElementsByTagNameNS("*", "Assertion").item(0);
    }
}
