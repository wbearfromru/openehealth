package be.medx.soap.handlers;

import org.apache.ws.security.SOAPConstants;
import org.apache.ws.security.util.WSSecurityUtil;
import org.apache.ws.security.WSEncryptionPart;

import java.util.Vector;

import javax.xml.crypto.dsig.Reference;

import org.apache.ws.security.components.crypto.Crypto;

import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.w3c.dom.Node;
import org.w3c.dom.Element;

import java.util.concurrent.TimeUnit;

import org.apache.ws.security.WSSConfig;
import org.w3c.dom.Document;
import org.apache.commons.lang.Validate;

import javax.xml.soap.SOAPMessage;

import org.apache.ws.security.message.WSSecTimestamp;
import org.apache.ws.security.message.WSSecSignature;
import org.apache.ws.security.message.WSSecHeader;

import javax.xml.soap.SOAPPart;

import org.slf4j.Logger;

import java.io.IOException;

import javax.xml.ws.ProtocolException;
import javax.xml.ws.handler.soap.SOAPMessageContext;

import org.apache.ws.security.WSSecurityException;

import be.medx.crypto.Credential;
import be.medx.exceptions.TechnicalConnectorException;
import be.medx.saml.SAMLToken;
import be.medx.soap.ws.WSSecurityCrypto;

public abstract class AbstractWsSecurityHandler extends AbstractSOAPHandler
{
    public WSSecHeaderGeneratorStep0 buildSignature() throws WSSecurityException {
        return new WSSecHeaderGeneratorImpl();
    }
    
    @Override
    public boolean handleOutbound(final SOAPMessageContext context) {
        try {
            this.getLogger().debug("adding WS-Security header");
            this.addWSSecurity(context);
            context.getMessage().saveChanges();
        }
        catch (Exception e) {
            throw new ProtocolException(e);
        }
        return true;
    }
    
    protected abstract void addWSSecurity(final SOAPMessageContext p0) throws IOException, WSSecurityException, TechnicalConnectorException;
    
    protected abstract Logger getLogger();
    
    protected enum SignedParts
    {
        BODY, 
        TIMESTAMP, 
        BST, 
        SAML_ASSERTION;
    }
    
    private class WSSecHeaderGeneratorImpl implements WSSecHeaderGeneratorStep0, WSSecHeaderGeneratorStep1, WSSecHeaderGeneratorStep2, WSSecHeaderGeneratorStep3, WSSecHeaderGeneratorStep4
    {
        private SOAPPart soapPart;
        private WSSecHeader wsSecHeader;
        private WSSecSignature sign;
        private WSSecTimestamp wsSecTimeStamp;
        private String assertionId;
        private Credential cred;
        
        @Override
        public WSSecHeaderGeneratorStep1 on(final SOAPMessage message) throws WSSecurityException {
            Validate.notNull((Object)message);
            this.soapPart = message.getSOAPPart();
            (this.wsSecHeader = new WSSecHeader()).insertSecurityHeader((Document)this.soapPart);
            final WSSConfig wssConfig = new WSSConfig();
            wssConfig.setWsiBSPCompliant(false);
            this.sign = new WSSecSignature(wssConfig);
            return this;
        }
        
        @Override
        public WSSecHeaderGeneratorStep2 withTimeStamp(final long ttl, final TimeUnit unit) {
            this.withTimeStamp(new Duration(ttl, unit));
            return this;
        }
        
        @Override
        public WSSecHeaderGeneratorStep2 withTimeStamp(final Duration duration) {
            (this.wsSecTimeStamp = new WSSecTimestamp()).setTimeToLive((int)duration.convert(TimeUnit.SECONDS));
            this.wsSecTimeStamp.build((Document)this.soapPart, this.wsSecHeader);
            return this;
        }
        
        @Override
        public WSSecHeaderGeneratorStep3 withBinarySecurityToken(final Credential cred) throws TechnicalConnectorException, WSSecurityException {
            this.cred = cred;
            return this;
        }
        
        @Override
        public WSSecHeaderGeneratorStep3 withSAMLToken(final SAMLToken token) throws WSSecurityException, TechnicalConnectorException {
            this.cred = token;
            final Element assertionElement = token.getAssertion();
            final Element importedAssertionElement = (Element)this.soapPart.importNode(assertionElement, true);
            final Element securityHeaderElement = this.wsSecHeader.getSecurityHeader();
            securityHeaderElement.appendChild(importedAssertionElement);
            this.assertionId = assertionElement.getAttribute("AssertionID");
            return this;
        }
        
        @Override
        public void sign(final SignedParts... parts) throws WSSecurityException, TechnicalConnectorException {
            if (StringUtils.isNotEmpty(this.assertionId)) {
                this.sign.setSignatureAlgorithm("http://www.w3.org/2000/09/xmldsig#rsa-sha1");
                this.sign.setKeyIdentifierType(12);
                this.sign.setCustomTokenValueType("http://docs.oasis-open.org/wss/oasis-wss-saml-token-profile-1.0#SAMLAssertionID");
                this.sign.setCustomTokenId(this.assertionId);
            }
            else {
                this.sign.setKeyIdentifierType(1);
            }
            final Crypto crypto = (Crypto)new WSSecurityCrypto(this.cred.getPrivateKey(), this.cred.getCertificate());
            this.sign.prepare((Document)this.soapPart, crypto, this.wsSecHeader);
            if (StringUtils.isEmpty(this.assertionId)) {
                this.sign.appendBSTElementToHeader(this.wsSecHeader);
            }
            final List<Reference> referenceList = (List<Reference>)this.sign.addReferencesToSign((List)this.generateReferencesToSign(parts), this.wsSecHeader);
            this.sign.computeSignature((List)referenceList, false, (Element)null);
        }
        
        protected Vector<WSEncryptionPart> generateReferencesToSign(final SignedParts[] parts) {
            final Vector<WSEncryptionPart> signParts = new Vector<WSEncryptionPart>();
            for (final SignedParts part : parts) {
                switch (part) {
                    case TIMESTAMP: {
                        Validate.notNull((Object)this.wsSecTimeStamp);
                        signParts.add(new WSEncryptionPart(this.wsSecTimeStamp.getId()));
                        break;
                    }
                    case BODY: {
                        final SOAPConstants soapConstants = WSSecurityUtil.getSOAPConstants(this.soapPart.getDocumentElement());
                        signParts.add(new WSEncryptionPart(soapConstants.getBodyQName().getLocalPart(), soapConstants.getEnvelopeURI(), "Content"));
                        break;
                    }
                    case SAML_ASSERTION: {
                        Validate.notNull((Object)this.assertionId);
                        signParts.add(new WSEncryptionPart(this.assertionId));
                        break;
                    }
                    case BST: {
                        signParts.add(new WSEncryptionPart(this.sign.getBSTTokenId()));
                        break;
                    }
                }
            }
            return signParts;
        }
    }
    
    public interface WSSecHeaderGeneratorStep1 extends WSSecHeaderGeneratorStep2
    {
        WSSecHeaderGeneratorStep2 withTimeStamp(long p0, TimeUnit p1);
        
        WSSecHeaderGeneratorStep2 withTimeStamp(Duration p0);
    }
    
    public interface WSSecHeaderGeneratorStep2 extends WSSecHeaderGeneratorStep3
    {
        WSSecHeaderGeneratorStep3 withBinarySecurityToken(Credential p0) throws TechnicalConnectorException, WSSecurityException;
    }
    
    public interface WSSecHeaderGeneratorStep3 extends WSSecHeaderGeneratorStep4
    {
        WSSecHeaderGeneratorStep3 withSAMLToken(SAMLToken p0) throws WSSecurityException, TechnicalConnectorException;
    }
    
    public interface WSSecHeaderGeneratorStep4
    {
        void sign(SignedParts... p0) throws WSSecurityException, TechnicalConnectorException;
    }
    
    public interface WSSecHeaderGeneratorStep0 extends WSSecHeaderGeneratorStep2
    {
        WSSecHeaderGeneratorStep1 on(SOAPMessage p0) throws WSSecurityException;
    }
}
