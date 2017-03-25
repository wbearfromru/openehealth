package be.medx;

import org.slf4j.LoggerFactory;
import javax.activation.DataSource;
import org.apache.commons.lang.ArrayUtils;
import org.w3c.dom.Element;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.util.Collection;
import java.util.Iterator;
import javax.xml.bind.Marshaller;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.bind.JAXBException;
import org.w3c.dom.Node;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.bind.JAXBElement;
import java.lang.annotation.Annotation;
import javax.xml.bind.annotation.XmlRootElement;
import be.medx.XOPFeature;
import org.xml.sax.SAXException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import javax.xml.ws.handler.Handler;
import java.util.List;
import javax.activation.DataHandler;
import java.util.Map;
import org.w3c.dom.Document;
import org.slf4j.Logger;

public final class GenericRequest
{
    private static final Logger LOG;
    private Document payload;
    private Map<String, DataHandler> handlers;
    private Map<String, Object> requestMap;
    private List<Handler> beforeSecurity;
    private List<Handler> afterSecurity;
    private List<Handler> securityHandler;
    private Map<Class, Object> activeFeatures;
    private List<Handler> featureHandlers;
    
    public GenericRequest() {
        this.handlers = new HashMap<String, DataHandler>();
        this.requestMap = new HashMap<String, Object>();
        this.beforeSecurity = new ArrayList<Handler>();
        this.afterSecurity = new ArrayList<Handler>();
        this.securityHandler = new ArrayList<Handler>();
        this.activeFeatures = new HashMap<Class, Object>();
        this.featureHandlers = new ArrayList<Handler>();
    }
    
    public GenericRequest setEndpoint(final String endpoint) {
        try {
            new URL(endpoint);
            this.requestMap.put("javax.xml.ws.service.endpoint.address", endpoint);
        }
        catch (MalformedURLException e) {
            throw new IllegalArgumentException(e.getMessage(), e);
        }
        return this;
    }
    
    public GenericRequest setPayload(final Document payload) {
        this.payload = payload;
        return this;
    }
    
    public GenericRequest setPayload(final Document payload, final GenericFeature... features) {
        this.payload = payload;
        this.process(features);
        return this;
    }
    
    public Document getPayload() {
        return this.payload;
    }
    
    public Map<String, DataHandler> getDataHandlerMap() {
        return this.handlers;
    }
    
    public GenericRequest setPayload(final String payload) throws TechnicalConnectorException {
        try {
            this.payload = ConnectorXmlUtils.toDocument(payload);
        }
        catch (TechnicalConnectorException e) {
            if (e.getCause() instanceof SAXException) {
                throw new IllegalArgumentException("Payload is not a well-formed xml document.", e);
            }
        }
        return this;
    }
    
    public GenericRequest setPayload(final Object payload) {
        this.setPayload(payload, null);
        return this;
    }
    
    public GenericRequest setPayload(final Object payload, final GenericFeature... features) {
        this.process(features);
        final XOPFeature mtomFeature = this.getFeature(XOPFeature.class);
        final Class<?> payloadClazz = payload.getClass();
        if (!payloadClazz.isAnnotationPresent(XmlRootElement.class)) {
            if (payload instanceof JAXBElement) {
                try {
                    final DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
                    dbf.setNamespaceAware(true);
                    final Document doc = dbf.newDocumentBuilder().newDocument();
                    final JAXBElement<?> jaxbElement = (JAXBElement<?>)payload;
                    final Marshaller marshaller = JaxbContextFactory.getJaxbContextForClass(jaxbElement.getDeclaredType()).createMarshaller();
                    marshaller.marshal(jaxbElement, doc);
                    this.payload = doc;
                    return this;
                }
                catch (JAXBException e) {
                    throw new IllegalArgumentException("PayLoadclass [" + payloadClazz + "] is not annotated with @XMLRootElement or is not a JAXBElement class.", e);
                }
                catch (ParserConfigurationException e2) {
                    throw new IllegalArgumentException("PayLoadclass [" + payloadClazz + "] is not annotated with @XMLRootElement or is not a JAXBElement class.", e2);
                }
            }
            throw new IllegalArgumentException("PayLoadclass [" + payloadClazz + "] is not annotated with @XMLRootElement or is not a JAXBElement class.");
        }
        final MarshallerHelper helper = getHelper(payloadClazz, mtomFeature);
        this.payload = helper.toDocument(payload);
        this.handlers = (Map<String, DataHandler>)helper.getDataHandlersMap();
        return this;
    }
    
    private <T extends GenericFeature> T getFeature(final Class<T> clazz) {
        for (final Class key : this.activeFeatures.keySet()) {
            if (clazz.isAssignableFrom(key)) {
                return (T)this.activeFeatures.get(key);
            }
        }
        return (T)this.activeFeatures.get(clazz);
    }
    
    private boolean hasFeature(final Class<?> clazz) {
        for (final Class key : this.activeFeatures.keySet()) {
            if (clazz.isAssignableFrom(key)) {
                return true;
            }
        }
        return this.activeFeatures.containsKey(clazz);
    }
    
    private void process(final GenericFeature... features) {
        for (final GenericFeature feature : features) {
            if (feature != null) {
                this.activeFeatures.put(feature.getClass(), feature);
                this.requestMap.put(feature.getID(), feature.isEnabled());
                this.featureHandlers.addAll(feature.getHandlers());
            }
        }
    }
    
    private static MarshallerHelper getHelper(final Class<?> payloadClazz, final XOPFeature feature) {
        if (feature == null) {
            return new MarshallerHelper(payloadClazz, payloadClazz, false, false);
        }
        return new MarshallerHelper(payloadClazz, payloadClazz, false, feature.isEnabled(), feature.getThreshold());
    }
    
    public GenericRequest setSoapAction(final String soapAction) throws TechnicalConnectorException {
        if (soapAction != null && !soapAction.isEmpty()) {
            this.requestMap.put("javax.xml.ws.soap.http.soapaction.use", Boolean.TRUE);
            this.requestMap.put("javax.xml.ws.soap.http.soapaction.uri", soapAction);
            this.securityHandler.add(new SoapActionHandler());
        }
        else {
            GenericRequest.LOG.warn("warning : setSoapAction called with parameter " + soapAction);
        }
        return this;
    }
    
    public Map<String, Object> getRequestMap() {
        return this.requestMap;
    }
    
    public GenericRequest setWSAddressing(final WsAddressingHeader header) throws TechnicalConnectorException {
        if (header != null) {
            this.requestMap.put("be.ehealth.technicalconnector.handler.WsAddressingHandlerV200508.use", Boolean.TRUE);
            this.requestMap.put("be.ehealth.technicalconnector.handler.WsAddressingHandlerV200508", header);
            this.securityHandler.add(new WsAddressingHandlerV200508());
            return this;
        }
        throw new TechnicalConnectorException();
    }

    public GenericRequest setCertificateSecured(final X509Certificate certificate, final PrivateKey privateKey) throws TechnicalConnectorException {
        final Credential cred = new KeyPairCredential(privateKey, certificate);
        this.setCredential(cred, TokenType.X509);
        return this;
    }

    public GenericRequest setSamlSecured(final Element assertion, final Credential hok) throws TechnicalConnectorException {
        /*final SAMLToken token = SAMLTokenFactory.getInstance().createSamlToken(assertion, hok);
        this.setCredential(token, TokenType.SAML);*/
        return this;
    }

    public GenericRequest setCredential(final Credential cred, final TokenType sec) throws TechnicalConnectorException {
        switch (sec) {
            case SAML: {
                break;
            }
            default: {
                this.processAsX509(cred);
                break;
            }
        }
        return this;
    }
    
    public GenericRequest setCredentialFromSession(final TokenType sec) throws TechnicalConnectorException {
        this.setCredential(null, sec);
        return this;
    }
    
    private GenericRequest processAsX509(Credential cred) throws TechnicalConnectorException {
        if (cred == null) {
            /*if (!Session.getInstance().hasValidSession()) {
                this.securityHandler.add(new CertificateCallback());
                return this;
            }
            cred = Session.getInstance().getSession().getSAMLToken();*/
        	throw new TechnicalConnectorException();
        }
        GenericRequest.LOG.debug("Using X509 Security");
        this.securityHandler.add(new CertificateCallback(cred.getCertificate(), cred.getPrivateKey()));
        return this;
    }
    
  /*  private GenericRequest processAsSAML(Credential cred) throws TechnicalConnectorException {
        if (cred == null) {
            if (!Session.getInstance().hasValidSession()) {
                this.securityHandler.add(new SAMLHolderOfKeyHandler());
                return this;
            }
            cred = Session.getInstance().getSession().getSAMLToken();
        }
        if (cred instanceof SAMLHolderOfKeyToken) {
            GenericRequest.LOG.debug("Using HolderOfKey Credential");
            final SAMLToken samlToken = (SAMLToken)cred;
            this.securityHandler.add(new SAMLHolderOfKeyHandler(samlToken));
        }
        else {
            if (!(cred instanceof SAMLSenderVouchesCredential)) {
                throw new IllegalArgumentException("Unsupported credential of type [" + cred.getClass().getName() + "]");
            }
            GenericRequest.LOG.debug("Using SenderVouches Credential");
            final SAMLSenderVouchesCredential sv = (SAMLSenderVouchesCredential)cred;
            this.securityHandler.add(new SAMLSenderVouchesHandler(sv.getAssertion(), sv.getCertificate(), sv.getPrivateKey()));
        }
        return this;
    }*/
    
    public GenericRequest setDefaultHandlerChain() throws TechnicalConnectorException {
/*        this.beforeSecurity.addAll(new ConfigurableFactoryHelper("connector.defaulthandlerchain.beforesecurity", null).getImplementations());
        this.afterSecurity.addAll(new ConfigurableFactoryHelper("connector.defaulthandlerchain.aftersecurity", null).getImplementations());*/
        return this;
    }
    
    public GenericRequest setHandlerChain(final HandlerChain handlers) {
        this.beforeSecurity.addAll(handlers.getHandlers(HandlerPosition.BEFORE));
        this.afterSecurity.addAll(handlers.getHandlers(HandlerPosition.AFTER));
        this.afterSecurity.addAll(handlers.getHandlers(HandlerPosition.SECURITY));
        return this;
    }

    public Handler<?>[] getHandlerchain() {
        Handler<?>[] result = (Handler<?>[])new Handler[0];
        if (this.beforeSecurity != null && !this.beforeSecurity.isEmpty()) {
            result = (Handler<?>[])ArrayUtils.addAll((Object[])result, (Object[])this.beforeSecurity.toArray(new Handler[0]));
        }
        if (this.securityHandler != null) {
            result = (Handler<?>[])ArrayUtils.addAll((Object[])result, (Object[])this.securityHandler.toArray(new Handler[0]));
        }
        if (this.afterSecurity != null && !this.afterSecurity.isEmpty()) {
            result = (Handler<?>[])ArrayUtils.addAll((Object[])result, (Object[])this.afterSecurity.toArray(new Handler[0]));
        }
        if (this.featureHandlers != null && !this.featureHandlers.isEmpty()) {
            result = (Handler<?>[])ArrayUtils.addAll((Object[])result, (Object[])this.featureHandlers.toArray(new Handler[0]));
        }
        result = this.addingDefaultHandlers(result);
        return result;
    }
    
    private Handler<?>[] addingDefaultHandlers(Handler<?>[] result) {
        boolean timeoutHandler = false;
        boolean loggingHandler = false;
        boolean userAgentHandler = false;
        for (final Handler<?> handler : result) {
            if (!timeoutHandler && ConnectionTimeOutHandler.class.isInstance(handler)) {
                timeoutHandler = true;
            }
            else if (!loggingHandler && LoggingHandler.class.isInstance(handler)) {
                loggingHandler = true;
            }
            else if (!userAgentHandler && UserAgentHandler.class.isInstance(handler)) {
                userAgentHandler = true;
            }
            if (timeoutHandler && loggingHandler && userAgentHandler) {
                break;
            }
        }
        if (!timeoutHandler) {
            result = this.addHandler(result, new ConnectionTimeOutHandler());
        }
        if (!loggingHandler) {
            result = this.addHandler(result, new LoggingHandler());
        }
        if (!userAgentHandler) {
            result = this.addHandler(result, new UserAgentHandler());
        }
        return result;
    }
    
    private Handler<?>[] addHandler(Handler<?>[] result, final Handler<?> handler) {
        result = (Handler<?>[])ArrayUtils.add((Object[])result, (Object)handler);
        return result;
    }
    
    public GenericRequest addDataHandler(final String id, final DataHandler dataHandler) {
        this.handlers.put(id, dataHandler);
        return this;
    }
    
    public boolean isXopEnabled() {
        return this.hasFeature(XOPFeature.class);
    }
    
    public GenericRequest addDataHandler(final String id, final byte[] byteArray) {
        this.addDataHandler(id, new DataHandler(new ByteArrayDatasource(byteArray)));
        return this;
    }
    
    static {
        LOG = LoggerFactory.getLogger(GenericRequest.class);
    }
}
