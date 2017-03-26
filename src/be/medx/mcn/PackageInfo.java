package be.medx.mcn;

public class PackageInfo {
	private String userName;
	private String password;
	private String packageName;

	public PackageInfo() {
	}

	public PackageInfo(final String userName, final String password, final String name) {
		this.userName = userName;
		this.password = password;
		this.packageName = name;
	}

	public void setUserName(final String userName) {
		this.userName = userName;
	}

	public String getUserName() {
		return this.userName;
	}

	public void setPassword(final String password) {
		this.password = password;
	}

	public String getPassword() {
		return this.password;
	}

	public void setPackageName(final String name) {
		this.packageName = name;
	}

	public String getPackageName() {
		return this.packageName;
	}
}
