package be.medx.mcn;

public class Party {
	private Identification physicalPerson;
	private Identification organization;

	public Identification getPhysicalPerson() {
		return this.physicalPerson;
	}

	public void setPhysicalPerson(final Identification physicalPerson) {
		this.physicalPerson = physicalPerson;
	}

	public Identification getOrganization() {
		return this.organization;
	}

	public void setOrganization(final Identification organization) {
		this.organization = organization;
	}
}
