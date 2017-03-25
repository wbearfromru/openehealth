package be.medx.soap.ws;

import java.util.ArrayList;
import java.util.List;
import java.net.URI;

public class WsAddressingHeader
{
    private String mustUnderstand;
    private URI messageID;
    private List<WsAddressingRelatesTo> relatesTo;
    private URI to;
    private URI action;
    private String from;
    private String replyTo;
    private String faultTo;
    
    public WsAddressingHeader(final URI action) {
        this.mustUnderstand = "1";
        this.relatesTo = new ArrayList<WsAddressingRelatesTo>();
        this.action = action;
    }
    
    public URI getMessageID() {
        return this.messageID;
    }
    
    public void setMessageID(final URI messageID) {
        this.messageID = messageID;
    }
    
    public URI getTo() {
        return this.to;
    }
    
    public void setTo(final URI to) {
        this.to = to;
    }
    
    public URI getAction() {
        return this.action;
    }
    
    public void setAction(final URI action) {
        this.action = action;
    }
    
    public String getFrom() {
        return this.from;
    }
    
    public void setFrom(final String from) {
        this.from = from;
    }
    
    public String getReplyTo() {
        return this.replyTo;
    }
    
    public void setReplyTo(final String replyTo) {
        this.replyTo = replyTo;
    }
    
    public String getFaultTo() {
        return this.faultTo;
    }
    
    public void setFaultTo(final String faultTo) {
        this.faultTo = faultTo;
    }
    
    public List<WsAddressingRelatesTo> getRelatesTo() {
        return this.relatesTo;
    }
    
    public String getMustUnderstand() {
        return this.mustUnderstand;
    }
    
    public void setMustUnderstand(final boolean mustUnderstand) {
        if (mustUnderstand) {
            this.mustUnderstand = "1";
        }
        else {
            this.mustUnderstand = "0";
        }
    }
}
