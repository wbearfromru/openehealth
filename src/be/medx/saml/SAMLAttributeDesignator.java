package be.medx.saml;

public class SAMLAttributeDesignator
{
    private String name;
    private String namespace;
    
    public SAMLAttributeDesignator(final String name, final String namespace) {
        this.name = name;
        this.namespace = namespace;
    }
    
    public String getName() {
        return this.name;
    }
    
    public String getNamespace() {
        return this.namespace;
    }
    
    @Override
    public String toString() {
        return "SAMLAttributeDesignator [name=" + this.name + ", namespace=" + this.namespace + "]";
    }
}
