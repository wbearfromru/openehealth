package be.medx.services;

import be.medx.mcn.Blob;

public class PostParameter {

	/**
	 * String which identifies the request, to be used in InputReference and as
	 * second part of the kmehr id.
	 */
	public String requestIdentifier;

	/**
	 * the Blob object to send with the business content.
	 */
	public Blob blob;

	/**
	 * boolean indicating its a test.
	 */
	public Boolean istest;

	/**
	 * the service name , used to retrieve parameters from config file.
	 */
	public String serviceName;

	/**
	 * boolean indicating if we must use xades or not. currently Xades is not
	 * supported and value true will cause an UnsupportedOperationException.
	 */
	public Boolean useXades;

	/**
	 * optional : the oaNumber to set in the to part of the header.
	 */
	public Integer oaNumber;

	/**
	 * the url to use for the
	 */
	public String addressingHeaderUrl;

	/**
     * 
     */
	public PostParameter(Blob blob, Boolean istest, String serviceName, Boolean useXades, java.lang.Integer oaNumber, String addressingHeaderUrl, String requestIdentifier) {
		this.blob = blob;
		this.istest = istest;
		this.serviceName = serviceName;
		this.useXades = useXades;
		this.oaNumber = oaNumber;
		this.addressingHeaderUrl = addressingHeaderUrl;
		this.requestIdentifier = requestIdentifier;
	}

	/**
	 * @param blob
	 *            the blob to set
	 */
	public void setBlob(Blob blob) {
		this.blob = blob;
	}

	/**
	 * @param istest
	 *            the istest to set
	 */
	public void setIstest(Boolean istest) {
		this.istest = istest;
	}

	/**
	 * @param serviceName
	 *            the serviceName to set
	 */
	public void setServiceName(String serviceName) {
		this.serviceName = serviceName;
	}

	/**
	 * @param useXades
	 *            the useXades to set
	 */
	public void setUseXades(Boolean useXades) {
		this.useXades = useXades;
	}

	/**
	 * @param oaNumber
	 *            the oaNumber to set
	 */
	public void setOaNumber(java.lang.Integer oaNumber) {
		this.oaNumber = oaNumber;
	}

	/**
	 * @param addressingHeaderUrl
	 *            the addressingHeaderUrl to set
	 */
	public void setAddressingHeaderUrl(String addressingHeaderUrl) {
		this.addressingHeaderUrl = addressingHeaderUrl;
	}
}
