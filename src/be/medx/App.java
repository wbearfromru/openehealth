package be.medx;

import org.apache.log4j.xml.DOMConfigurator;

import be.medx.exceptions.TechnicalConnectorException;
import be.medx.saml.SAMLToken;
import be.medx.services.CryptoService;
import be.medx.services.CryptoServiceImpl;
import be.medx.services.SAMLTokenService;
import be.medx.services.SAMLTokenServiceImpl;

public class App {

	private static final String SSIN_70101109152_20160502_140450_P12 = "D:/Projects/Windoc/branches/hotfix-9.13.15/libraries/ehealthconnector/src/EHealthInterface/Connector/bin/Debug/P12/SSIN=70101109152 20160502-140450.p12";

	private CryptoService cryptoService;
	private SAMLTokenService samlTokenService;

	public App() {
		this.cryptoService = new CryptoServiceImpl();
		this.samlTokenService = new SAMLTokenServiceImpl(this.cryptoService);
	}

	private SAMLToken initSession(String certificatePath, String certificatePassword) throws TechnicalConnectorException {
		this.cryptoService.loadCerificate(certificatePath, certificatePassword);
		return this.samlTokenService.getSAMLToken();
	}

	public static void main(String[] args) throws TechnicalConnectorException {
		new App().initSession(SSIN_70101109152_20160502_140450_P12, "boerestraat11a");
	}

	static {
		DOMConfigurator.configure("./src/log4j.conf");
	}

}
