package be.medx.mcn;

public class CareProvider extends Party {
	private Nihii nihii;

	public CareProvider(final Nihii nihii) {
		this.nihii = nihii;
	}

	public Nihii getNihii() {
		return this.nihii;
	}

	public void setNihii(final Nihii nihii) {
		this.nihii = nihii;
	}
}
