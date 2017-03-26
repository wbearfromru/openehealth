package be.medx.mcn;

public class Origin {
	private PackageInfo packageInfo;
	private String siteId;
	private CareProvider careProvider;
	private Party sender;

	public Origin(final PackageInfo packageInfo, final CareProvider careProvider) {
		this.packageInfo = packageInfo;
		this.careProvider = careProvider;
	}

	public Origin(final PackageInfo packageInfo, final Party sender) {
		this.packageInfo = packageInfo;
		this.sender = sender;
	}

	public Origin(final PackageInfo packageInfo, final String siteId, final Party sender) {
		this.packageInfo = packageInfo;
		this.siteId = siteId;
		this.sender = sender;
	}

	public Origin(final PackageInfo packageInfo, final String siteId, final CareProvider careProvider, final Party sender) {
		this.packageInfo = packageInfo;
		this.siteId = siteId;
		this.careProvider = careProvider;
		this.sender = sender;
	}

	public PackageInfo getPackageInfo() {
		return this.packageInfo;
	}

	public void setPackageInfo(final PackageInfo packageInfo) {
		this.packageInfo = packageInfo;
	}

	public String getSiteId() {
		return this.siteId;
	}

	public void setSiteId(final String siteId) {
		this.siteId = siteId;
	}

	public CareProvider getCareProvider() {
		return this.careProvider;
	}

	public void setCareProvider(final CareProvider careProvider) {
		this.careProvider = careProvider;
	}

	public Party getSender() {
		return this.sender;
	}

	public void setSender(final Party sender) {
		this.sender = sender;
	}
}
