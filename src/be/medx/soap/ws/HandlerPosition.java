package be.medx.soap.ws;

public enum HandlerPosition
{
    BEFORE("BeforeSecurityChain"), 
    SECURITY("SecurityChain"), 
    AFTER("AfterSecurityChain");
    
    private String name;
    
    private HandlerPosition(final String name) {
        this.name = name;
    }
    
    public String getName() {
        return this.name;
    }
}
