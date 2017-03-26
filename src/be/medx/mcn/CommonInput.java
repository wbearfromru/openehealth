package be.medx.mcn;

public class CommonInput {
	private Boolean isTest;
	private Origin origin;
	private String inputReference;

	public CommonInput(final Boolean isTest, final Origin origin, final String inputReference) {
		this.isTest = isTest;
		this.origin = origin;
		this.inputReference = inputReference;
	}

	public Boolean isTest() {
		return this.isTest;
	}

	public void setIsTest(final Boolean isTest) {
		this.isTest = isTest;
	}

	public Origin getOrigin() {
		return this.origin;
	}

	public String getInputReference() {
		return this.inputReference;
	}
}
