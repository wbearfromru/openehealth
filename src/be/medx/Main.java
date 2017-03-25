package be.medx;

import java.math.BigInteger;
import java.security.*;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.security.interfaces.DSAPublicKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.xml.crypto.MarshalException;
import javax.xml.crypto.dsig.CanonicalizationMethod;
import javax.xml.crypto.dsig.Reference;
import javax.xml.crypto.dsig.SignatureMethod;
import javax.xml.crypto.dsig.SignedInfo;
import javax.xml.crypto.dsig.Transform;
import javax.xml.crypto.dsig.XMLSignature;
import javax.xml.crypto.dsig.XMLSignatureException;
import javax.xml.crypto.dsig.XMLSignatureFactory;
import javax.xml.crypto.dsig.dom.DOMSignContext;
import javax.xml.crypto.dsig.keyinfo.KeyInfo;
import javax.xml.crypto.dsig.keyinfo.KeyInfoFactory;
import javax.xml.crypto.dsig.spec.C14NMethodParameterSpec;
import javax.xml.crypto.dsig.spec.TransformParameterSpec;
import javax.xml.soap.SOAPException;
import javax.xml.transform.Source;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.bouncycastle.util.encoders.Base64;
import org.joda.time.DateTime;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import be.fgov.ehealth.etee.crypto.utils.KeyManager;

public class Main {

	private static final String SSIN_70101109152_20160502_140450_P12 = "D:/Projects/Windoc/branches/hotfix-9.13.15/libraries/ehealthconnector/src/EHealthInterface/Connector/bin/Debug/P12/SSIN=70101109152 20160502-140450.p12";
	private final static String STS_ENDPOINT = "https://services-acpt.ehealth.fgov.be/IAM/Saml11TokenService/Legacy/v1";
    private static XMLSignatureFactory xmlSignatureFactory;
    

	private Credential headerCredential;
    private final Map<String, KeyStore> keystores;
	private KeyStoreCredential holderOfKeyCredential;
	private Map<String, PrivateKey> holderOfKeyPrivateKeys;
	private KeyStoreCredential encryptionCredential;
	private Map<String, PrivateKey> encryptionPrivateKeys;
	
	public static void main(String[] args) {   
		Main obj = new Main();
		
		String pass = "boerestraat11a";
		
		try {
			obj.loadIdentificationKeys(pass, false);
			obj.loadHolderOfKeyKeys(pass, false);
			obj.loadEncryptionKeys(pass, false);
	        obj.initSession();
		} catch (TechnicalConnectorException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public Main() {
        this.keystores = new HashMap<String, KeyStore>();
	}
	
    public Element getToken(final Credential headerCredentials, final Credential bodyCredentials, final List<SAMLAttribute> attributes, final List<SAMLAttributeDesignator> designators, final String subjectConfirmationMethod, final int validity) throws TechnicalConnectorException {
        return this.getToken(headerCredentials, bodyCredentials, attributes, designators, null, null, null, subjectConfirmationMethod, validity);
    }
	
	public Element getToken(final Credential headerCredentials, final Credential bodyCredentials, final List<SAMLAttribute> attributes, final List<SAMLAttributeDesignator> designators, final String authenticationMethod, final String nameQualifier, final String value, final String subjectConfirmationMethod, final int validity) throws TechnicalConnectorException {
        try {
            final SAMLNameIdentifier nameIdentifier = this.generateNameIdentifier(headerCredentials, nameQualifier, value);
            String requestTemplate = "";
            boolean sign = false;
            if (subjectConfirmationMethod.equalsIgnoreCase("urn:oasis:names:tc:SAML:1.0:cm:holder-of-key")) {
                sign = true;
                final String keyinfoType = "x509";
                if ("publickey".equalsIgnoreCase(keyinfoType)) {
                    requestTemplate = ConnectorIOUtils.convertStreamToString(ConnectorIOUtils.getResourceAsStream("/legacy/issue.samlv11.hok.publickey.template.xml"));
                }
                else {
                    requestTemplate = ConnectorIOUtils.convertStreamToString(ConnectorIOUtils.getResourceAsStream("/legacy/issue.samlv11.hok.template.xml"));
                }
            }
            else {
                if (!subjectConfirmationMethod.equalsIgnoreCase("urn:oasis:names:tc:SAML:1.0:cm:sender-vouches")) {
                    throw new UnsupportedOperationException("SubjectConfirmationMethod [" + subjectConfirmationMethod + "] not supported.");
                }
                if (StringUtils.isEmpty(authenticationMethod)) {
                    requestTemplate = ConnectorIOUtils.convertStreamToString(ConnectorIOUtils.getResourceAsStream("/legacy/issue.samlv11.sv.template.xml"));
                }
                else {
                    requestTemplate = ConnectorIOUtils.convertStreamToString(ConnectorIOUtils.getResourceAsStream("/legacy/issue.samlv11.sv.authmethod.template.xml"));
                }
            }
            final Document samlRequest = this.generateToken(requestTemplate, sign, headerCredentials, bodyCredentials, nameIdentifier, authenticationMethod, attributes, designators, validity);
            final GenericRequest request = getX509SecuredRequest(headerCredentials.getCertificate(), headerCredentials.getPrivateKey(), STS_ENDPOINT);
            request.setSoapAction("urn:be:fgov:ehealth:sts:protocol:v1:RequestSecureToken");
            request.setPayload(samlRequest);
            final Source response = new GenericWsSenderImpl().send(request).asSource();
            final Element stsResponse = SAMLConverter.convert(response);
            final String status = SAMLHelper.getStatusCode(stsResponse);
            if (!status.contains("Success")) {
                throw new TechnicalConnectorException();
            }
            if (stsResponse.getElementsByTagName("Assertion").getLength() < 0) {
                throw new TechnicalConnectorException();
            }
            return SAMLHelper.getAssertion(stsResponse);
        }
        catch (SOAPException e) {
            throw new TechnicalConnectorException();
        }
    }
	
    private SAMLNameIdentifier generateNameIdentifier(final X509Certificate authnCertificate) throws TechnicalConnectorException {
        final String cn = authnCertificate.getSubjectX500Principal().getName("RFC1779");
        final String ca = authnCertificate.getIssuerX500Principal().getName("RFC1779");
        return new SAMLNameIdentifier(StringEscapeUtils.escapeXml(cn), "urn:oasis:names:tc:SAML:1.1:nameid-format:X509SubjectName", StringEscapeUtils.escapeXml(ca), StringEscapeUtils.escapeXml(cn));
    }
    
    private Document generateToken(final String requestTemplate, final boolean sign, final Credential headerCred, final Credential hokCred, final SAMLNameIdentifier nameIdentifier, final String authmethod, final List<SAMLAttribute> attributes, final List<SAMLAttributeDesignator> designators, final int validity) throws TechnicalConnectorException {
        try {
            String request = ConnectorXmlUtils.flatten(requestTemplate);
            request = this.processDefaultFields(request, validity, nameIdentifier);
            request = this.processHolderOfKeyCredentials(hokCred, request);
            request = StringUtils.replace(request, "${authenticationMethod}", authmethod);
            final Element payload = ConnectorXmlUtils.toElement(request.getBytes());
            final Document doc = payload.getOwnerDocument();
            this.addDesignators(designators, doc);
            this.processAttributes(attributes, doc);
            final boolean alwaysSign = false;
            if (sign) {
                if (headerCred.getCertificate().equals(hokCred.getCertificate())) {
                    if (!alwaysSign) {
                        return doc;
                    }
                }
                try {
                    this.signRequest(doc.getDocumentElement(), hokCred.getPrivateKey(), hokCred.getCertificate());
                }
                catch (Exception e) {
                    throw new TechnicalConnectorException();
                }
            }
            return doc;
        }
        catch (CertificateEncodingException e2) {
            throw new TechnicalConnectorException();
        }
    }
    
    private void processAttributes(final List<SAMLAttribute> attributes, final Document doc) {
        final Element attributeStatement = (Element)doc.getElementsByTagNameNS("urn:oasis:names:tc:SAML:1.0:assertion", "AttributeStatement").item(0);
        for (final SAMLAttribute attr : attributes) {
            final Element attrEl = doc.createElementNS("urn:oasis:names:tc:SAML:1.0:assertion", "saml:Attribute");
            attrEl.setAttribute("AttributeName", attr.getName());
            attrEl.setAttribute("AttributeNamespace", attr.getNamespace());
            this.processAttributeValues(attrEl, attr.getValues());
            attributeStatement.appendChild(attrEl);
        }
    }
    
    
    protected SAMLNameIdentifier generateNameIdentifier(final Credential headerCredentials, final String nameQualifier, final String value) throws TechnicalConnectorException {
        SAMLNameIdentifier nameId = null;
        if (StringUtils.isEmpty(nameQualifier) && StringUtils.isEmpty(value)) {
            nameId = this.generateNameIdentifier(headerCredentials.getCertificate());
        }
        else {
            nameId = new SAMLNameIdentifier(nameQualifier, "urn:oasis:names:tc:SAML:1.1:nameid-format:unspecified", nameQualifier, value);
        }
        return nameId;
    }
    
    private static GenericRequest getX509SecuredRequest(final X509Certificate certificate, final PrivateKey privateKey, final String endpoint) throws TechnicalConnectorException {
        final GenericRequest request = getUnSecuredRequest(endpoint);
        request.setCertificateSecured(certificate, privateKey);
        return request;
    }
    
    private static GenericRequest getUnSecuredRequest(final String endpoint) throws TechnicalConnectorException {
        final GenericRequest request = new GenericRequest();
        request.setDefaultHandlerChain();
        request.setEndpoint(endpoint);
        return request;
    }
    
    protected String processDefaultFields(final String requestTemplate, final int validity, final SAMLNameIdentifier nameIdentifier) throws TechnicalConnectorException {
        final DateTime now = new DateTime();
        final String uuid = IdGeneratorFactory.getIdGenerator("uuid").generateId();
        final String notBefore = DateUtils.printDateTime(now);
        final String notAfter = DateUtils.printDateTime(now.plusHours(validity));
        String result = StringUtils.replace(requestTemplate, "${uuid}", uuid);
        result = StringUtils.replace(result, "${NotBefore}", notBefore);
        result = StringUtils.replace(result, "${NotOnOrAfter}", notAfter);
        result = StringUtils.replace(result, "${issuer}", nameIdentifier.getAssertionIssuer());
        result = StringUtils.replace(result, "${nameid.format}", nameIdentifier.getFormat());
        result = StringUtils.replace(result, "${nameid.qualifier}", nameIdentifier.getNameQualifier());
        result = StringUtils.replace(result, "${nameid.value}", nameIdentifier.getValue());
        return result;
    }
    
    private String processHolderOfKeyCredentials(final Credential hokCred, String request) throws TechnicalConnectorException, CertificateEncodingException {
        if (hokCred != null && hokCred.getCertificate() != null) {
            request = StringUtils.replace(request, "${holder.of.key}", new String(Base64.encode(hokCred.getCertificate().getEncoded())));
            final PublicKey publicKey = hokCred.getCertificate().getPublicKey();
            if (publicKey instanceof RSAPublicKey) {
                final RSAPublicKey rsaPublicKey = (RSAPublicKey)publicKey;
                request = StringUtils.replace(request, "${publickey.rsa.modulus}", new String(Base64.encode(convertTo(rsaPublicKey.getModulus()))));
                request = StringUtils.replace(request, "${publickey.rsa.exponent}", new String(Base64.encode(convertTo(rsaPublicKey.getPublicExponent()))));
                request = StringUtils.replace(request, "<ds:DSAKeyValue><ds:G>${publickey.dsa.g}<ds:G><ds:P>${publickey.dsa.p}</ds:P><ds:Q>${publickey.dsa.q}</ds:Q></ds:DSAKeyValue>", "");
            }
            else if (publicKey instanceof DSAPublicKey) {
                final DSAPublicKey dsaPublicKey = (DSAPublicKey)publicKey;
                request = StringUtils.replace(request, "${publickey.dsa.g}", new String(Base64.encode(convertTo(dsaPublicKey.getParams().getG()))));
                request = StringUtils.replace(request, "${publickey.dsa.p}", new String(Base64.encode(convertTo(dsaPublicKey.getParams().getP()))));
                request = StringUtils.replace(request, "${publickey.dsa.q}", new String(Base64.encode(convertTo(dsaPublicKey.getParams().getQ()))));
                request = StringUtils.replace(request, "<ds:RSAKeyValue><ds:Modulus>${publickey.rsa.modulus}</ds:Modulus><ds:Exponent>${publickey.rsa.exponent}</ds:Exponent></ds:RSAKeyValue>", "");
            }
            else {
                //STSServiceImpl.LOG.info("Unsupported public key: [" + publicKey.getClass().getName() + "+]");
            }
        }
        return request;
    }
    
    private void processAttributeValues(final Element attrEl, final String[] attributeValues) {
        for (final String attributeValue : attributeValues) {
            final Element attrVal = attrEl.getOwnerDocument().createElementNS("urn:oasis:names:tc:SAML:1.0:assertion", "saml:AttributeValue");
            attrVal.setTextContent(attributeValue);
            attrEl.appendChild(attrVal);
        }
    }
    
    private void signRequest(final Element requestElement, final PrivateKey privateKey, final Object keyInfoValue) throws NoSuchAlgorithmException, InvalidAlgorithmParameterException, MarshalException, XMLSignatureException, KeyException {
        final DOMSignContext domSignContext = new DOMSignContext(privateKey, requestElement, requestElement.getFirstChild());
        final String requestId = requestElement.getAttribute("RequestID");
        requestElement.setIdAttribute("RequestID", true);
        final List<Transform> transforms = new LinkedList<Transform>();
        transforms.add(this.xmlSignatureFactory.newTransform("http://www.w3.org/2000/09/xmldsig#enveloped-signature", (TransformParameterSpec)null));
        transforms.add(this.xmlSignatureFactory.newTransform("http://www.w3.org/2001/10/xml-exc-c14n#", (TransformParameterSpec)null));
        final Reference reference = this.xmlSignatureFactory.newReference("#" + requestId, this.xmlSignatureFactory.newDigestMethod("http://www.w3.org/2000/09/xmldsig#sha1", null), transforms, null, null);
        final CanonicalizationMethod canonicalizationMethod = this.xmlSignatureFactory.newCanonicalizationMethod("http://www.w3.org/2001/10/xml-exc-c14n#", (C14NMethodParameterSpec)null);
        final SignatureMethod signatureMethod = this.xmlSignatureFactory.newSignatureMethod("http://www.w3.org/2000/09/xmldsig#rsa-sha1", null);
        final SignedInfo signedInfo = this.xmlSignatureFactory.newSignedInfo(canonicalizationMethod, signatureMethod, Collections.singletonList(reference));
        final KeyInfoFactory keyInfoFactory = this.xmlSignatureFactory.getKeyInfoFactory();
        KeyInfo keyInfo = null;
        if (keyInfoValue instanceof PublicKey) {
            keyInfo = keyInfoFactory.newKeyInfo(Collections.singletonList(keyInfoFactory.newKeyValue((PublicKey)keyInfoValue)));
        }
        else {
            if (!(keyInfoValue instanceof X509Certificate)) {
                throw new IllegalArgumentException("Unsupported keyinfo type [" + keyInfoValue.getClass() + "]");
            }
            keyInfo = keyInfoFactory.newKeyInfo(Collections.singletonList(keyInfoFactory.newX509Data(Collections.singletonList(keyInfoValue))));
        }
        final XMLSignature xmlSignature = this.xmlSignatureFactory.newXMLSignature(signedInfo, keyInfo);
        xmlSignature.sign(domSignContext);
    }
    
    private static byte[] convertTo(final BigInteger bigInteger) {
        byte[] array = bigInteger.toByteArray();
        if (array[0] == 0) {
            final byte[] tmp = new byte[array.length - 1];
            System.arraycopy(array, 1, tmp, 0, tmp.length);
            array = tmp;
        }
        return array;
    }    
    
    private void addDesignators(final List<SAMLAttributeDesignator> designators, final Document doc) {
        final Element attributeQuery = (Element)doc.getElementsByTagNameNS("urn:oasis:names:tc:SAML:1.0:protocol", "AttributeQuery").item(0);
        for (final SAMLAttributeDesignator attr : designators) {
            final Element attrEl = doc.createElementNS("urn:oasis:names:tc:SAML:1.0:assertion", "saml:AttributeDesignator");
            attrEl.setAttribute("AttributeName", attr.getName());
            attrEl.setAttribute("AttributeNamespace", attr.getNamespace());
            attributeQuery.appendChild(attrEl);
        }
    }
    
    private void loadIdentificationKeys(final String pwd, final boolean eidonly) throws TechnicalConnectorException {
        final char[] password = (pwd == null) ? ArrayUtils.EMPTY_CHAR_ARRAY : pwd.toCharArray();
        if (this.keystores.containsKey("identification")) {
            this.headerCredential = new KeyStoreCredential(this.keystores.get("identification"), "authentication", pwd);
        }
        else             if (pwd == null) {
                return;
            }            else {
            try {
                final String pathKeystore = SSIN_70101109152_20160502_140450_P12;
                final char[] pwdKeystore = password;
                final String privateKeyAlias = "authentication";
                final char[] privateKeyPwd = password;
                final KeyStoreInfo ksInfo = new KeyStoreInfo(pathKeystore, pwdKeystore, privateKeyAlias, privateKeyPwd);
                final Credential headerCred = new KeyStoreCredential(ksInfo);
                this.headerCredential = headerCred;
            }
            catch (Exception e) {
                throw new TechnicalConnectorException();
            }
        }
    }
    
    private void loadHolderOfKeyKeys(final String pwd, final boolean eidonly) throws TechnicalConnectorException {
        final char[] password = (pwd == null) ? ArrayUtils.EMPTY_CHAR_ARRAY : pwd.toCharArray();
        if (this.keystores.containsKey("holderofkey")) {
            final KeyStore hokstore = this.keystores.get("holderofkey");
            this.holderOfKeyCredential = new KeyStoreCredential(hokstore, "authentication", pwd);
            this.holderOfKeyPrivateKeys = KeyManager.getDecryptionKeys(hokstore, password);
        }
        else if (pwd == null){
                return;
        } else {
            try {
                final String pathKeystore = SSIN_70101109152_20160502_140450_P12;
                final char[] pwdKeystore = password;
                final String privateKeyAlias = "authentication";
                final char[] privateKeyPwd = password;
                final KeyStoreInfo ksInfo = new KeyStoreInfo(pathKeystore, pwdKeystore, privateKeyAlias, privateKeyPwd);
                final KeyStoreManager encryptionKeystoreManager = new KeyStoreManager(ksInfo);
                final Map<String, PrivateKey> hokPrivateKeys = (Map<String, PrivateKey>)KeyManager.getDecryptionKeys(encryptionKeystoreManager.getKeyStore(), ksInfo.getPrivateKeyPassword());
                this.holderOfKeyCredential = new KeyStoreCredential(ksInfo);
                this.holderOfKeyPrivateKeys = hokPrivateKeys;
                //fetchEtk(EncryptionTokenType.HOLDER_OF_KEY, hokPrivateKeys);
            }
            catch (Exception e) {
                throw new TechnicalConnectorException();
            }
        }
    }
    
    private void loadEncryptionKeys(final String pwd, final boolean eidonly) throws TechnicalConnectorException {
        final char[] password = (pwd == null) ? ArrayUtils.EMPTY_CHAR_ARRAY : pwd.toCharArray();
        if (this.keystores.containsKey("encryption")) {
            final KeyStore hokstore = this.keystores.get("encryption");
            this.holderOfKeyCredential = new KeyStoreCredential(hokstore, "authentication", pwd);
            this.holderOfKeyPrivateKeys = KeyManager.getDecryptionKeys(hokstore, password);
        }
        else if (pwd == null) {
                return;
        } else {
            try {
                final String pathKeystore = SSIN_70101109152_20160502_140450_P12;
                final char[] pwdKeystore = password;
                final String privateKeyAlias = "authentication";
                final char[] privateKeyPwd = password;
                final KeyStoreInfo ksInfo = new KeyStoreInfo(pathKeystore, pwdKeystore, privateKeyAlias, privateKeyPwd);
                final KeyStoreManager encryptionKeystoreManager = new KeyStoreManager(ksInfo);
                final Map<String, PrivateKey> encryptionPrivateKeys = (Map<String, PrivateKey>)KeyManager.getDecryptionKeys(encryptionKeystoreManager.getKeyStore(), ksInfo.getPrivateKeyPassword());
                this.encryptionCredential = new KeyStoreCredential(ksInfo);
                this.encryptionPrivateKeys= encryptionPrivateKeys;
                //fetchEtk(EncryptionTokenType.ENCRYPTION, encryptionPrivateKeys);
            }
            catch (Exception e) {
                throw new TechnicalConnectorException();
            }
        }
    }
    
    private SAMLToken initSession() throws TechnicalConnectorException {
        final int validity = 24;
        final List<SAMLAttributeDesignator> designators = SAMLConfigHelper.getSAMLAttributeDesignators("sessionmanager.samlattributedesignator");
        final List<SAMLAttribute> attributes = SAMLConfigHelper.getSAMLAttributes("sessionmanager.samlattribute");
        final Element assertion = this.getToken(headerCredential, this.holderOfKeyCredential, attributes, designators, "urn:oasis:names:tc:SAML:1.0:cm:holder-of-key", 24);
        return SAMLTokenFactory.getInstance().createSamlToken(assertion, this.holderOfKeyCredential);
    }
    
    
    static {
        try {
            final String providerName = System.getProperty("jsr105Provider", "org.jcp.xml.dsig.internal.dom.XMLDSigRI");
            final Provider provider = (Provider)Class.forName(providerName).newInstance();
            xmlSignatureFactory = XMLSignatureFactory.getInstance("DOM", provider);
        }
        catch (IllegalAccessException e) {
            throw new RuntimeException(e.getClass().getSimpleName() + ": " + e.getMessage(), e);
        }
        catch (java.lang.InstantiationException e2) {
            throw new RuntimeException(e2.getClass().getSimpleName() + ": " + e2.getMessage(), e2);
        }
        catch (ClassNotFoundException e3) {
            throw new RuntimeException(e3.getClass().getSimpleName() + ": " + e3.getMessage(), e3);
        }
    }
    
}
