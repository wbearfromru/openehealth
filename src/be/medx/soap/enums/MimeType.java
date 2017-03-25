package be.medx.soap.enums;

public enum MimeType
{
    plaintext("plain/text"), 
    octectstream("application/octet-stream");
    
    private String value;
    
    private MimeType(final String value) {
        this.value = value;
    }
    
    public String getValue() {
        return this.value;
    }
}
