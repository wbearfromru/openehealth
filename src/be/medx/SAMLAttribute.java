package be.medx;

import org.apache.commons.lang.ArrayUtils;

public class SAMLAttribute
{
    private String name;
    private String namespace;
    private String[] value;
    
    public SAMLAttribute(final String name, final String namespace, final String... value) {
        this.name = name;
        this.namespace = namespace;
        this.value = value;
    }
    
    public String getName() {
        return this.name;
    }
    
    public String getNamespace() {
        return this.namespace;
    }
    
    @Deprecated
    public String getValue() {
        if (ArrayUtils.isNotEmpty((Object[])this.value)) {
            return this.value[0];
        }
        return null;
    }
    
    public String[] getValues() {
        return (String[])ArrayUtils.clone((Object[])this.value);
    }
    
    @Override
    public String toString() {
        return "SAMLAttribute [name=" + this.name + ", namespace=" + this.namespace + ", value=" + ArrayUtils.toString((Object)this.value) + "]";
    }
}
