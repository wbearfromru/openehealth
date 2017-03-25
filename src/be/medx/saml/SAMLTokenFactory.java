package be.medx.saml;

import org.slf4j.LoggerFactory;
import org.w3c.dom.NodeList;
import org.w3c.dom.Element;
import org.slf4j.Logger;

import be.medx.crypto.Credential;

public final class SAMLTokenFactory
{
    private static final Logger LOG;
    private static final String XMLNS_SAML_1_0_ASS = "urn:oasis:names:tc:SAML:1.0:assertion";
    
    public static SAMLTokenFactory getInstance() {
        return SAMLTokenFactorySingleton.INSTANCE.getSAMLTokenFactory();
    }
    
    public SAMLToken createSamlToken(final Element assertion, final Credential credential) {
        final NodeList authenticationStatements = assertion.getElementsByTagNameNS("urn:oasis:names:tc:SAML:1.0:assertion", "AuthenticationStatement");
        for (int i = 0; i < authenticationStatements.getLength(); ++i) {
            final Element authenticationStatement = (Element)authenticationStatements.item(i);
            final NodeList confirmationMethodsNodeList = authenticationStatement.getElementsByTagNameNS("urn:oasis:names:tc:SAML:1.0:assertion", "ConfirmationMethod");
            for (int j = 0; j < confirmationMethodsNodeList.getLength(); ++j) {
                final Element confirmationMethodEl = (Element)confirmationMethodsNodeList.item(j);
                final String confirmationMethod = confirmationMethodEl.getTextContent();
                SAMLTokenFactory.LOG.debug("ConfirmationMethod " + confirmationMethod + " found.");
                if ("urn:oasis:names:tc:SAML:1.0:cm:holder-of-key".equals(confirmationMethod)) {
                    return new SAMLHolderOfKeyToken(assertion, credential);
                }
                if ("urn:oasis:names:tc:SAML:1.0:cm:sender-vouches".equals(confirmationMethod)) {
                    return new SAMLSenderVouchesCredential(assertion, credential);
                }
                SAMLTokenFactory.LOG.debug("Unsupported configurtionMethod [" + confirmationMethod + "]");
            }
        }
        SAMLTokenFactory.LOG.debug("Unable to determine confirmationMethod.");
        return new SAMLHolderOfKeyToken(assertion, credential);
    }
    
    static {
        LOG = LoggerFactory.getLogger((Class)SAMLTokenFactory.class);
    }
    
    private enum SAMLTokenFactorySingleton
    {
        INSTANCE;
        
        private SAMLTokenFactory instance;
        
        private SAMLTokenFactorySingleton() {
            this.instance = new SAMLTokenFactory();
        }
        
        public SAMLTokenFactory getSAMLTokenFactory() {
            return this.instance;
        }
    }
}
