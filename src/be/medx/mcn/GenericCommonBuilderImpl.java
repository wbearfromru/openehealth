package be.medx.mcn;

import org.joda.time.DateTime;

import be.medx.exceptions.TechnicalConnectorException;

public class GenericCommonBuilderImpl implements CommonBuilder {
	private String projectName;
	private String careProviderId;
	private String careProviderFullName;
	private String careProviderSSIN;

	public GenericCommonBuilderImpl(String projectName, String careProviderId, String careProviderFullName, String careProviderSSIN) throws TechnicalConnectorException {
		this.projectName = projectName;
		this.careProviderId = careProviderId;
		this.careProviderFullName = careProviderFullName;
		this.careProviderSSIN = careProviderSSIN;
	}

	protected CareProvider createCareProviderForOrigin() throws NumberFormatException, TechnicalConnectorException {
		final CareProviderBuilder builder = new CareProviderBuilder("doctor", careProviderId);
		final String physicalPersonRootKey = "mycarenet." + this.projectName + ".careprovider.physicalperson";
		final String organizationRootKey = "mycarenet." + this.projectName + ".careprovider.organization";
		builder.addPhysicalPersonIdentification(this.createPerson(physicalPersonRootKey));
		builder.addOrganisationIdentification(this.createOrganization(organizationRootKey));
		return builder.build();
	}

	@Override
	public CommonInput createCommonInput(final PackageInfo packageInfo, final boolean isTest, final String inputReference) throws TechnicalConnectorException {
		final Origin origin = this.createOrigin(packageInfo);
		return new CommonInput(isTest, origin, inputReference);
	}

	@Override
	public Origin createOrigin(final PackageInfo packageInfo) throws TechnicalConnectorException {
		final Origin origin = new Origin(packageInfo, this.createCareProviderForOrigin());
		origin.setSender(this.createSenderForOrigin());
		origin.setSiteId("");
		return origin;
	}

	public String getProjectName() {
		return this.projectName;
	}

	public void setProjectName(final String projectName) {
		this.projectName = projectName;
	}

	Party createSenderForOrigin() throws TechnicalConnectorException {
		final String retrievedProjectName = this.getProjectName();
		final Party party = new Party();
		final String senderRootKey = "mycarenet." + retrievedProjectName + ".sender";
		party.setPhysicalPerson(this.createPerson(senderRootKey + ".physicalperson"));
		party.setOrganization(this.createOrganization(senderRootKey + ".organization"));
		return party;
	}

	@Override
	public Routing createRouting(final Patient patientInfo, final DateTime refDate) {
		final Routing routing = new Routing();
		routing.setPeriod(null);
		routing.setCareReceiver(this.createCareReceiver(patientInfo));
		routing.setReferenceDate(refDate);
		return routing;
	}

	@Override
	public Routing createRoutingToMutuality(final String mutuality, final DateTime refDate) {
		final Routing routing = new Routing();
		routing.setPeriod(null);
		routing.setCareReceiver(this.createCareReceiverForMutuality(mutuality));
		routing.setReferenceDate(refDate);
		return routing;
	}

	protected final CareReceiverId createCareReceiver(final Patient patientInfo) {
		final CareReceiverId careReceiver = new CareReceiverId(patientInfo.getInss(), patientInfo.getRegNrWithMut(), patientInfo.getMutuality());
		return careReceiver;
	}

	protected final CareReceiverId createCareReceiverForMutuality(final String mutuality) {
		final CareReceiverId careReceiver = new CareReceiverId(null, null, mutuality);
		return careReceiver;
	}

	protected Identification createOrganization(final String key) throws TechnicalConnectorException {
		final Identification identification = this.getIdentification(key);
		final boolean containsOrganization = identification != null;
		if (containsOrganization && identification.getCbe() == null && identification.getNihii() == null) {
			throw new TechnicalConnectorException();
		}
		return identification;
	}

	protected Identification createPerson(final String key) throws TechnicalConnectorException {
		final Identification identification = this.getIdentification(key);
		final boolean containsPhysicalPerson = identification != null;
		if (containsPhysicalPerson && identification.getSsin() == null) {
			throw new TechnicalConnectorException();
		}
		return identification;
	}

	private Identification getIdentification(final String key) {
		final Identification identification = new Identification();
		identification.setName(this.careProviderFullName);
		identification.setSsin(this.careProviderSSIN);
		final Nihii nihiiObject = new Nihii("doctor", this.careProviderId);
		identification.setNihii(nihiiObject);
		return identification;
	}

}
