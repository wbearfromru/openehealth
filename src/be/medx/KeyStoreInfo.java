package be.medx;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;

public class KeyStoreInfo
{
    private static final String JKS = "JKS";
    private static final String PKCS12 = "pkcs12";
    private String keystorePath;
    private String keystoreType;
    private char[] keystorePassword;
    private String alias;
    private char[] privateKeyPassword;
    
    public KeyStoreInfo(final String alias, final char[] privateKeyPwd) throws TechnicalConnectorException {
        this(null, null, null, alias, privateKeyPwd);
    }
    
    public KeyStoreInfo(final String pathKeystore, final char[] pwdKeystore, final String alias, final char[] privateKeyPwd) throws TechnicalConnectorException {
        this(pathKeystore, PKCS12, pwdKeystore, alias, privateKeyPwd);
        if (pathKeystore.toLowerCase().contains(".jks")) {
            this.keystoreType = JKS;
        }
    }
    
    public KeyStoreInfo(final String pathKeystore, final String keystoreType, final char[] pwdKeystore, final String alias, final char[] privateKeyPwd) throws TechnicalConnectorException {
        Validate.notEmpty(alias);
        Validate.notNull((Object)privateKeyPwd);
        if (StringUtils.isNotEmpty(pathKeystore)) {
            Validate.notEmpty(keystoreType);
            Validate.notNull((Object)pwdKeystore);
        }
        this.keystorePath = pathKeystore;
        this.keystoreType = keystoreType;
        this.keystorePassword = ArrayUtils.clone(pwdKeystore);
        this.alias = alias;
        this.privateKeyPassword = ArrayUtils.clone(privateKeyPwd);
    }
    
    public final String getKeystorePath() {
        return this.keystorePath;
    }
    
    public final String getKeystoreType() {
        return this.keystoreType;
    }
    
    public final char[] getKeystorePassword() {
        return ArrayUtils.clone(this.keystorePassword);
    }
    
    public final String getAlias() {
        return this.alias;
    }
    
    public final char[] getPrivateKeyPassword() {
        return ArrayUtils.clone(this.privateKeyPassword);
    }
    
    static {
    }
}
