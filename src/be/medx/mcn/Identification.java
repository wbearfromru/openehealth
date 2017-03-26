package be.medx.mcn;

public class Identification {
	private String name;
	private Nihii nihii;
	private String ssin;
	private String cbe;

	public Identification() {
	}

	public Identification(final String name) {
		this.name = name;
	}

	public Identification(final String name, final Nihii nihii, final String ssin, final String cbe) {
		this.name = name;
		this.nihii = nihii;
		this.ssin = ssin;
		this.cbe = cbe;
	}

	public void setName(final String name) {
		this.name = name;
	}

	public String getName() {
		return this.name;
	}

	public Nihii getNihii() {
		return this.nihii;
	}

	public void setNihii(final Nihii nihii) {
		this.nihii = nihii;
	}

	public String getSsin() {
		return this.ssin;
	}

	public void setSsin(final String ssin) {
		this.ssin = ssin;
	}

	public String getCbe() {
		return this.cbe;
	}

	public void setCbe(final String cbe) {
		this.cbe = cbe;
	}
}
