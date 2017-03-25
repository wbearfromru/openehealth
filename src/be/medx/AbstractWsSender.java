// 
// Decompiled by Procyon v0.5.29
// 

package be.medx;

import java.io.InputStream;
import javax.xml.soap.AttachmentPart;
import java.util.Iterator;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPPart;
import javax.activation.DataHandler;
import java.io.IOException;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.text.MessageFormat;
import javax.xml.soap.SOAPMessage;
import java.net.URL;
import javax.xml.soap.SOAPConnection;
import java.net.MalformedURLException;
import javax.xml.soap.SOAPException;
import javax.xml.ws.handler.Handler;
import javax.xml.ws.handler.soap.SOAPMessageContext;
import java.util.Map;
import javax.xml.soap.SOAPConnectionFactory;
import javax.xml.soap.MessageFactory;

public abstract class AbstractWsSender
{
    public static final String MESSAGECONTEXT_ENDPOINT_ADDRESS = "javax.xml.ws.service.endpoint.address";
    public static final String PROP_RETRY_STRATEGY = "be.ehealth.technicalconnector.ws.genericsender.invokestrategy";
    //private static final Logger LOG;
    private static MessageFactory mf;
    private static SOAPConnectionFactory scf;
    //private static ConfigurableFactoryHelper<InvokeStrategy> invokeStrategyHelper;
    
    public GenericResponse send(final GenericRequest genericRequest) throws TechnicalConnectorException {
        final SOAPMessageContext request = this.createSOAPMessageCtx(genericRequest);
        request.putAll(genericRequest.getRequestMap());
        final InvokeStrategy strategy = new NoRetryInvokeStrategy();
        return strategy.invoke(request, genericRequest.getHandlerchain());
    }
    
    protected static GenericResponse call(final SOAPMessageContext request, final Handler<?>[] chain) throws TechnicalConnectorException {
        request.put("javax.xml.ws.handler.message.outbound", true);
        executeHandlers(chain, request);
        SOAPConnection conn = null;
        SOAPMessageContext reply = null;
        try {
            final URL endpoint = generateEndpoint(request);
            final SOAPMessage msgToSend = request.getMessage();
            conn = AbstractWsSender.scf.createConnection();
            reply = createSOAPMessageCtx(conn.call(msgToSend, endpoint));
        }
        catch (UnsupportedOperationException e) {
            throw translate(e);
        }
        catch (SOAPException e2) {
            throw translate(e2);
        }
        catch (MalformedURLException e3) {
            throw translate(e3);
        }
        finally {
            ConnectorIOUtils.closeQuietly(conn);
        }
        reply.put("javax.xml.ws.handler.message.outbound", false);
        executeHandlers(chain, reply);
        return new GenericResponse(reply.getMessage());
    }
    
    private static SOAPMessageContext createSOAPMessageCtx(final SOAPMessage msg) {
        return new SOAPMessageContextImpl(msg);
    }
    
    private static TechnicalConnectorException translate(final Exception e) {
    	e.printStackTrace();
        return new TechnicalConnectorException();
    }
    
    private static void executeHandlers(final Handler[] handlers, final SOAPMessageContext request) throws TechnicalConnectorException {
        for (final Handler handler : handlers) {
            if (!handler.handleMessage(request)) {
                throw new TechnicalConnectorException();
            }
        }
    }
    
    private static URL generateEndpoint(final SOAPMessageContext request) throws MalformedURLException {
        final String requestedTarget = (String)request.get("javax.xml.ws.service.endpoint.address");
        //final String target = EndpointDistributor.getInstance().getActiveEndpoint(requestedTarget);
        final String target = requestedTarget;
        request.put("javax.xml.ws.service.endpoint.address", target);
        final URL targetURL = new URL(target);
        final StringBuffer context = new StringBuffer();
        context.append(targetURL.getProtocol());
        context.append("://");
        context.append(targetURL.getHost());
        if (targetURL.getPort() != -1) {
            context.append(":");
            context.append(targetURL.getPort());
        }
        final URL endpoint = new URL(new URL(context.toString()), targetURL.getFile(), new URLStreamHandler() {
            @Override
            protected URLConnection openConnection(final URL url) throws IOException {
                final URL target = new URL(url.toString());
                final URLConnection connection = target.openConnection();
                connection.setConnectTimeout(Integer.parseInt((String)request.get("connector.soaphandler.connection.connection.timeout")));
                connection.setReadTimeout(Integer.parseInt((String)request.get("connector.soaphandler.connection.request.timeout")));
                return connection;
            }
        });
        return endpoint;
    }
    
    protected SOAPMessageContext createSOAPMessageCtx(final GenericRequest genericRequest) throws TechnicalConnectorException {
        try {
            final SOAPMessage soapMessage = AbstractWsSender.mf.createMessage();
            final SOAPPart soapPart = soapMessage.getSOAPPart();
            if (genericRequest.isXopEnabled()) {
                soapMessage.getMimeHeaders().addHeader("Content-Type", "application/xop+xml");
                soapPart.addMimeHeader("Content-ID", "<root.message@ehealth.fgov.be>");
                soapPart.addMimeHeader("Content-Transfer-Encoding", "8bit");
            }
            final SOAPEnvelope soapEnvelope = soapPart.getEnvelope();
            final SOAPBody soapBody = soapEnvelope.getBody();
            soapBody.addDocument(genericRequest.getPayload());
            final Map<String, DataHandler> handlers = genericRequest.getDataHandlerMap();
            for (final Map.Entry<String, DataHandler> handlerEntry : handlers.entrySet()) {
                final DataHandler handler = handlerEntry.getValue();
                final AttachmentPart part = soapMessage.createAttachmentPart(handler);
                part.setContentType(handler.getContentType());
                if (genericRequest.isXopEnabled()) {
                    part.addMimeHeader("Content-Transfer-Encoding", "binary");
                    part.setContentId("<" + handlerEntry.getKey() + ">");
                }
                else {
                    part.setContentId(handlerEntry.getKey());
                }
                soapMessage.addAttachmentPart(part);
            }
            return createSOAPMessageCtx(soapMessage);
        }
        catch (SOAPException e) {
            throw translate(e);
        }
    }
    
    static {
        final InputStream is = null;
        try {
            AbstractWsSender.mf = MessageFactory.newInstance();
            AbstractWsSender.scf = SOAPConnectionFactory.newInstance();
        }
        catch (UnsupportedOperationException e) {
            throw new IllegalArgumentException(e);
        }
        catch (SOAPException e2) {
            throw new IllegalArgumentException(e2);
        }
        finally {
            ConnectorIOUtils.closeQuietly(is);
        }
    }
}
