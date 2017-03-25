package be.medx;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import javax.xml.soap.SOAPException;
import javax.activation.DataHandler;
import java.util.HashMap;
import javax.xml.soap.AttachmentPart;
import java.util.Map;
import javax.xml.bind.attachment.AttachmentUnmarshaller;

public class AttachmentUnmarshallerImpl extends AttachmentUnmarshaller
{
    private Map<String, AttachmentPart> attachments;
    private boolean xop;
    
    public AttachmentUnmarshallerImpl(final boolean xop) {
        this.attachments = new HashMap<String, AttachmentPart>();
        this.xop = xop;
    }
    
    public Map<String, AttachmentPart> getAttachmentPartMap() {
        return this.attachments;
    }
    
    @Override
    public DataHandler getAttachmentAsDataHandler(final String cid) {
        final AttachmentPart attachment = this.attachments.get(decode(cid));
        try {
            return attachment.getDataHandler();
        }
        catch (SOAPException e) {
            throw new IllegalStateException(e);
        }
    }
    
    @Override
    public byte[] getAttachmentAsByteArray(final String cid) {
        try {
            final DataHandler handler = this.getAttachmentAsDataHandler(cid);
            return ConnectorIOUtils.getBytes(handler.getInputStream());
        }
        catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }
    
    private static String decode(final String cid) {
        try {
            return URLDecoder.decode(cid, "UTF-8");
        }
        catch (UnsupportedEncodingException e) {
            throw new IllegalStateException(e);
        }
    }
    
    @Override
    public boolean isXOPPackage() {
        return this.xop;
    }
}
