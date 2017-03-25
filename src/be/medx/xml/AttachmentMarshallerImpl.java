package be.medx.xml;

import java.util.UUID;
import java.util.HashMap;
import javax.activation.DataHandler;
import java.util.Map;
import javax.xml.bind.attachment.AttachmentMarshaller;

public class AttachmentMarshallerImpl extends AttachmentMarshaller
{
    private Map<String, DataHandler> attachments;
    private boolean xop;
    private int threshold;
    
    public AttachmentMarshallerImpl(final boolean xop) {
        this(xop, 10);
    }
    
    public AttachmentMarshallerImpl(final boolean xop, final int threshold) {
        this.attachments = new HashMap<String, DataHandler>();
        this.xop = xop;
        this.threshold = threshold;
    }
    
    public Map<String, DataHandler> getDataHandlerMap() {
        return this.attachments;
    }
    
    @Override
    public String addMtomAttachment(final DataHandler data, final String elementNamespace, final String elementLocalName) {
        if (this.xop) {
            return this.addDataHandler(data);
        }
        return null;
    }
    
    @Override
    public String addMtomAttachment(final byte[] data, final int offset, final int length, final String mimeType, final String elementNamespace, final String elementLocalName) {
        if (!this.xop) {
            return null;
        }
        if (length < this.threshold) {
            return null;
        }
        final byte[] subarray = new byte[length];
        System.arraycopy(data, offset, subarray, 0, length);
        return this.addDataHandler(new DataHandler(subarray, mimeType));
    }
    
    @Override
    public String addSwaRefAttachment(final DataHandler data) {
        return this.addDataHandler(data);
    }
    
    @Override
    public boolean isXOPPackage() {
        return this.xop;
    }
    
    private String addDataHandler(final DataHandler handler) {
        final String cid = UUID.randomUUID() + "@ehealth.fgov.be";
        this.attachments.put(cid, handler);
        return "cid:" + cid;
    }
}
