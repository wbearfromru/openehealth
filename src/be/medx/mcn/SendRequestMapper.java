package be.medx.mcn;

import java.io.IOException;

import javax.activation.DataHandler;

import org.apache.commons.io.IOUtils;
import org.w3._2005._05.xmlmime.Base64Binary;

import be.fgov.ehealth.mycarenet.commons.core.v2.BlobType;
import be.fgov.ehealth.mycarenet.commons.core.v2.CareProviderType;
import be.fgov.ehealth.mycarenet.commons.core.v2.CareReceiverIdType;
import be.fgov.ehealth.mycarenet.commons.core.v2.CommonInputType;
import be.fgov.ehealth.mycarenet.commons.core.v2.IdType;
import be.fgov.ehealth.mycarenet.commons.core.v2.LicenseType;
import be.fgov.ehealth.mycarenet.commons.core.v2.NihiiType;
import be.fgov.ehealth.mycarenet.commons.core.v2.OriginType;
import be.fgov.ehealth.mycarenet.commons.core.v2.PackageType;
import be.fgov.ehealth.mycarenet.commons.core.v2.PartyType;
import be.fgov.ehealth.mycarenet.commons.core.v2.PeriodType;
import be.fgov.ehealth.mycarenet.commons.core.v2.RequestType;
import be.fgov.ehealth.mycarenet.commons.core.v2.RoutingType;
import be.fgov.ehealth.mycarenet.commons.core.v2.ValueRefString;
import be.medx.exceptions.TechnicalConnectorException;
import be.medx.soap.ws.ByteArrayDatasource;
import be.medx.xml.JaxbContextFactory;

public final class SendRequestMapper {
	public static CommonInputType mapCommonInput(final CommonInput commonInput) {
		final CommonInputType inputType = new CommonInputType();
		inputType.setOrigin(getOrigin(commonInput));
		inputType.setInputReference(commonInput.getInputReference());
		inputType.setRequest(getRequestType(commonInput.isTest()));
		return inputType;
	}

	public static RoutingType mapRouting(final Routing inRouting) {
		final RoutingType routing = new RoutingType();
		routing.setCareReceiver(getCareReceiver(inRouting.getCareReceiver()));
		routing.setPeriod(getPeriod(inRouting.getPeriod()));
		routing.setReferenceDate(inRouting.getReferenceDate());
		return routing;
	}

	public static BlobType mapBlobToBlobType(final Blob inBlob) {
		final BlobType blob = new BlobType();
		blob.setId(inBlob.getId());
		blob.setValue(inBlob.getContent());
		blob.setHashValue(inBlob.getHashValue());
		blob.setContentEncoding(inBlob.getContentEncoding());
		blob.setContentType(inBlob.getContentType());
		return blob;
	}

	public static Blob mapBlobTypeToBlob(final BlobType inBlob) {
		final Blob blob = new Blob();
		blob.setId(inBlob.getId());
		blob.setContent(inBlob.getValue());
		blob.setHashValue(inBlob.getHashValue());
		blob.setContentEncoding(inBlob.getContentEncoding());
		blob.setContentType(inBlob.getContentType());
		return blob;
	}

	private static OriginType getOrigin(final CommonInput commonInput) {
		final OriginType origin = new OriginType();
		origin.setCareProvider(getCareprovider(commonInput.getOrigin().getCareProvider()));
		origin.setPackage(getPackage(commonInput.getOrigin().getPackageInfo()));
		origin.setSender(getParty(commonInput.getOrigin().getSender()));
		return origin;
	}

	private static CareProviderType getCareprovider(final CareProvider inProvider) {
		final CareProviderType careProvider = new CareProviderType();
		careProvider.setNihii(getNihii(inProvider.getNihii()));
		careProvider.setOrganization(getIdType(inProvider.getOrganization()));
		careProvider.setPhysicalPerson(getIdType(inProvider.getPhysicalPerson()));
		return careProvider;
	}

	private static RequestType getRequestType(final boolean isTest) {
		final RequestType requestType = new RequestType();
		requestType.setIsTest(isTest);
		return requestType;
	}

	private static CareReceiverIdType getCareReceiver(final CareReceiverId inCareReceiver) {
		final CareReceiverIdType careReceiver = new CareReceiverIdType();
		careReceiver.setMutuality(inCareReceiver.getMutuality());
		careReceiver.setRegNrWithMut(inCareReceiver.getRegistrationNumberWithMutuality());
		careReceiver.setSsin(inCareReceiver.getSsinNumber());
		return careReceiver;
	}

	private static PackageType getPackage(final PackageInfo info) {
		final PackageType type = new PackageType();
		type.setName(getValueRef(info.getPackageName(), null));
		type.setLicense(getLicense(info.getUserName(), info.getPassword()));
		return type;
	}

	private static NihiiType getNihii(final Nihii inNihii) {
		NihiiType nihii = null;
		if (inNihii != null) {
			nihii = new NihiiType();
			nihii.setValue(getValueRef(inNihii.getValue(), null));
			nihii.setQuality(inNihii.getQuality());
		}
		return nihii;
	}

	private static IdType getIdType(final Identification id) {
		IdType idType = null;
		if (id != null) {
			idType = new IdType();
			idType.setCbe(getValueRef(id.getCbe(), null));
			idType.setName(getValueRef(id.getName(), null));
			idType.setSsin(getValueRef(id.getSsin(), null));
			idType.setNihii(getNihii(id.getNihii()));
		}
		return idType;
	}

	private static ValueRefString getValueRef(final String value, final String reference) {
		ValueRefString valueRef = null;
		if (value != null) {
			valueRef = new ValueRefString();
			valueRef.setValue(value);
			valueRef.setValueRef(reference);
		}
		return valueRef;
	}

	private static LicenseType getLicense(final String userName, final String password) {
		final LicenseType license = new LicenseType();
		license.setUsername(userName);
		license.setPassword(password);
		return license;
	}

	private static PartyType getParty(final Party inParty) {
		final PartyType party = new PartyType();
		party.setOrganization(getIdType(inParty.getOrganization()));
		party.setPhysicalPerson(getIdType(inParty.getPhysicalPerson()));
		return party;
	}

	private static PeriodType getPeriod(final Period inPeriod) {
		PeriodType period = null;
		if (inPeriod != null) {
			period = new PeriodType();
			period.setStart(inPeriod.getBegin());
			period.setEnd(inPeriod.getEnd());
		}
		return period;
	}

	public static Blob mapToBlob(final be.cin.types.v1.Blob blob) throws TechnicalConnectorException {
		final Blob result = new Blob();
		result.setContent(convertToByteArray(blob.getValue()));
		result.setId(blob.getId());
		result.setContentEncoding(blob.getContentEncoding());
		result.setHashValue(blob.getHashValue());
		result.setContentType(blob.getContentType());
		return result;
	}

	public static be.cin.types.v1.Blob mapBlobToCinBlob(final Blob blob) {
		final be.cin.types.v1.Blob result = new be.cin.types.v1.Blob();
		final ByteArrayDatasource rawData = new ByteArrayDatasource(blob.getContent());
		final DataHandler dh = new DataHandler(rawData);
		result.setValue(dh);
		result.setMessageName(blob.getMessageName());
		result.setId(blob.getId());
		result.setContentEncoding(blob.getContentEncoding());
		result.setHashValue(blob.getHashValue());
		result.setContentType(blob.getContentType());
		return result;
	}

	public static Base64Binary mapB64fromByte(final byte[] param) {
		final Base64Binary result = new Base64Binary();
		result.setValue(param);
		result.setContentType("text/xml");
		return result;
	}

	private static byte[] convertToByteArray(final DataHandler value) throws TechnicalConnectorException {
		if (value == null) {
			return new byte[0];
		}
		try {
			return IOUtils.toByteArray(value.getInputStream());
		} catch (IOException e) {
			throw new TechnicalConnectorException();
		}
	}

	public void bootstrap() {
		JaxbContextFactory.initJaxbContext(new Class[] { BlobType.class });
		JaxbContextFactory.initJaxbContext(new Class[] { CareProviderType.class });
		JaxbContextFactory.initJaxbContext(new Class[] { CareReceiverIdType.class });
		JaxbContextFactory.initJaxbContext(new Class[] { CommonInputType.class });
		JaxbContextFactory.initJaxbContext(new Class[] { IdType.class });
		JaxbContextFactory.initJaxbContext(new Class[] { LicenseType.class });
		JaxbContextFactory.initJaxbContext(new Class[] { NihiiType.class });
		JaxbContextFactory.initJaxbContext(new Class[] { OriginType.class });
		JaxbContextFactory.initJaxbContext(new Class[] { PackageType.class });
		JaxbContextFactory.initJaxbContext(new Class[] { PartyType.class });
		JaxbContextFactory.initJaxbContext(new Class[] { PeriodType.class });
		JaxbContextFactory.initJaxbContext(new Class[] { RequestType.class });
		JaxbContextFactory.initJaxbContext(new Class[] { RoutingType.class });
		JaxbContextFactory.initJaxbContext(new Class[] { ValueRefString.class });
	}
}
