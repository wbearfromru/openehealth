package be.medx.mcn;

public class Nihii {
	private String quality;
	private String value;

	public Nihii(final String quality, final String value) {
		this.quality = quality;
		this.value = value;
	}

	public String getQuality() {
		return this.quality;
	}

	public void setQuality(final String quality) {
		this.quality = quality;
	}

	public String getValue() {
		return this.value;
	}

	public void setValue(final String value) {
		this.value = value;
	}
}
