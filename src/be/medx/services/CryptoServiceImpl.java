package be.medx.services;

import java.security.KeyStore;
import java.security.PrivateKey;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.ArrayUtils;

import be.fgov.ehealth.etee.crypto.utils.KeyManager;
import be.medx.crypto.AbstractExtendedCredential;
import be.medx.crypto.Credential;
import be.medx.crypto.KeyStoreCredential;
import be.medx.crypto.KeyStoreInfo;
import be.medx.crypto.KeyStoreManager;
import be.medx.exceptions.TechnicalConnectorException;

public class CryptoServiceImpl implements CryptoService {


	private Credential headerCredential;
    private final Map<String, KeyStore> keystores;
	private KeyStoreCredential holderOfKeyCredential;
	private Map<String, PrivateKey> holderOfKeyPrivateKeys;
	private KeyStoreCredential encryptionCredential;
	private Map<String, PrivateKey> encryptionPrivateKeys;
	private String certificatePath;

	public CryptoServiceImpl() {
        this.keystores = new HashMap<String, KeyStore>();
	}
	
	@Override
	public void loadCerificate(String certificatePath, String certificatePassword) throws TechnicalConnectorException {
		this.certificatePath = certificatePath;
		this.loadIdentificationKeys(certificatePassword, false);
		this.loadHolderOfKeyKeys(certificatePassword, false);
		this.loadEncryptionKeys(certificatePassword, false);
	}

	@Override
	public AbstractExtendedCredential getHOKCredential() {
		return this.holderOfKeyCredential;
	}
	
    @Override
	public Credential getHeaderCredential() {
		return this.headerCredential;
	}

	private void loadIdentificationKeys(final String pwd, final boolean eidonly) throws TechnicalConnectorException {
        final char[] password = (pwd == null) ? ArrayUtils.EMPTY_CHAR_ARRAY : pwd.toCharArray();
        if (this.keystores.containsKey("identification")) {
            this.headerCredential = new KeyStoreCredential(this.keystores.get("identification"), "authentication", pwd);
        }
        else             if (pwd == null) {
                return;
            }            else {
            try {
                final String pathKeystore = this.certificatePath;
                final char[] pwdKeystore = password;
                final String privateKeyAlias = "authentication";
                final char[] privateKeyPwd = password;
                final KeyStoreInfo ksInfo = new KeyStoreInfo(pathKeystore, pwdKeystore, privateKeyAlias, privateKeyPwd);
                final Credential headerCred = new KeyStoreCredential(ksInfo);
                this.headerCredential = headerCred;
            }
            catch (Exception e) {
                throw new TechnicalConnectorException();
            }
        }
    }
    
    private void loadHolderOfKeyKeys(final String pwd, final boolean eidonly) throws TechnicalConnectorException {
        final char[] password = (pwd == null) ? ArrayUtils.EMPTY_CHAR_ARRAY : pwd.toCharArray();
        if (this.keystores.containsKey("holderofkey")) {
            final KeyStore hokstore = this.keystores.get("holderofkey");
            this.holderOfKeyCredential = new KeyStoreCredential(hokstore, "authentication", pwd);
            this.holderOfKeyPrivateKeys = KeyManager.getDecryptionKeys(hokstore, password);
        }
        else if (pwd == null){
                return;
        } else {
            try {
                final String pathKeystore = this.certificatePath;
                final char[] pwdKeystore = password;
                final String privateKeyAlias = "authentication";
                final char[] privateKeyPwd = password;
                final KeyStoreInfo ksInfo = new KeyStoreInfo(pathKeystore, pwdKeystore, privateKeyAlias, privateKeyPwd);
                final KeyStoreManager encryptionKeystoreManager = new KeyStoreManager(ksInfo);
                final Map<String, PrivateKey> hokPrivateKeys = (Map<String, PrivateKey>)KeyManager.getDecryptionKeys(encryptionKeystoreManager.getKeyStore(), ksInfo.getPrivateKeyPassword());
                this.holderOfKeyCredential = new KeyStoreCredential(ksInfo);
                this.holderOfKeyPrivateKeys = hokPrivateKeys;
                //fetchEtk(EncryptionTokenType.HOLDER_OF_KEY, hokPrivateKeys);
            }
            catch (Exception e) {
                throw new TechnicalConnectorException();
            }
        }
    }
    
    private void loadEncryptionKeys(final String pwd, final boolean eidonly) throws TechnicalConnectorException {
        final char[] password = (pwd == null) ? ArrayUtils.EMPTY_CHAR_ARRAY : pwd.toCharArray();
        if (this.keystores.containsKey("encryption")) {
            final KeyStore hokstore = this.keystores.get("encryption");
            this.holderOfKeyCredential = new KeyStoreCredential(hokstore, "authentication", pwd);
            this.holderOfKeyPrivateKeys = KeyManager.getDecryptionKeys(hokstore, password);
        }
        else if (pwd == null) {
                return;
        } else {
            try {
                final String pathKeystore = this.certificatePath;
                final char[] pwdKeystore = password;
                final String privateKeyAlias = "authentication";
                final char[] privateKeyPwd = password;
                final KeyStoreInfo ksInfo = new KeyStoreInfo(pathKeystore, pwdKeystore, privateKeyAlias, privateKeyPwd);
                final KeyStoreManager encryptionKeystoreManager = new KeyStoreManager(ksInfo);
                final Map<String, PrivateKey> encryptionPrivateKeys = (Map<String, PrivateKey>)KeyManager.getDecryptionKeys(encryptionKeystoreManager.getKeyStore(), ksInfo.getPrivateKeyPassword());
                this.encryptionCredential = new KeyStoreCredential(ksInfo);
                this.encryptionPrivateKeys= encryptionPrivateKeys;
                //fetchEtk(EncryptionTokenType.ENCRYPTION, encryptionPrivateKeys);
            }
            catch (Exception e) {
                throw new TechnicalConnectorException();
            }
        }
    }
    

}
