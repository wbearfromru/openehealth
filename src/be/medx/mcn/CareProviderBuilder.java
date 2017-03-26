// 
// Decompiled by Procyon v0.5.29
// 

package be.medx.mcn;


public class CareProviderBuilder {
	private CareProvider careProvider;

	public CareProviderBuilder(final String quality, final String id) {
		this.careProvider = new CareProvider(new Nihii(quality, id));
	}

	public final CareProviderBuilder addPhysicalPersonIdentification(final Identification identification) {
		this.checkPhysicalPersonNotFilledOutYet();
		this.careProvider.setPhysicalPerson(identification);
		return this;
	}

	public final CareProviderBuilder addOrganisationIdentification(final Identification identification) {
		this.checkOrganisationNotFilledOutYet();
		this.careProvider.setOrganization(identification);
		return this;
	}

	public final CareProviderBuilder addPhysicalPersonWithSsin(final String name, final String ssin) {
		this.checkPhysicalPersonNotFilledOutYet();
		this.careProvider.setPhysicalPerson(new Identification(name, null, ssin, null));
		return this;
	}

	private void checkPhysicalPersonNotFilledOutYet() {
		if (this.careProvider.getPhysicalPerson() != null) {
			throw new IllegalStateException("error while building careprovider : addPhysicalPerson called while physical person already filled out");
		}
	}

	public final CareProviderBuilder addPhysicalPersonWithNihii(final String name, final String quality, final String value) {
		this.checkPhysicalPersonNotFilledOutYet();
		this.careProvider.setPhysicalPerson(new Identification(name, new Nihii(quality, value), null, null));
		return this;
	}

	public final CareProviderBuilder addOrganisationWithNihii(final String name, final String quality, final String value) {
		this.checkOrganisationNotFilledOutYet();
		this.careProvider.setOrganization(new Identification(name, new Nihii(quality, value), null, null));
		return this;
	}

	private void checkOrganisationNotFilledOutYet() {
		if (this.careProvider.getOrganization() != null) {
			throw new IllegalStateException("error while building careprovider : addOrganisation called while organisation already filled out");
		}
	}

	public final CareProviderBuilder addOrganisationWithCbe(final String name, final String cbeNumber) {
		this.checkOrganisationNotFilledOutYet();
		this.careProvider.setOrganization(new Identification(name, null, null, cbeNumber));
		return this;
	}

	public final CareProvider build() {
		return this.careProvider;
	}
}
