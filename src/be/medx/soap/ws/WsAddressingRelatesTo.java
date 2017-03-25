package be.medx.soap.ws;

import java.net.URI;

public class WsAddressingRelatesTo
{
    private String relationshipType;
    private URI releatesTo;
    
    public String getRelationshipType() {
        return this.relationshipType;
    }
    
    public void setRelationshipType(final String relationshipType) {
        this.relationshipType = relationshipType;
    }
    
    public URI getReleatesTo() {
        return this.releatesTo;
    }
    
    public void setReleatesTo(final URI releatesTo) {
        this.releatesTo = releatesTo;
    }
}
