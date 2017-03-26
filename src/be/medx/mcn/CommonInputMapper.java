// 
// Decompiled by Procyon v0.5.29
// 

package be.medx.mcn;

import java.util.ArrayList;
import java.util.List;

import org.dozer.DozerBeanMapper;

import be.cin.mycarenet.esb.common.v2.CareProviderType;
import be.cin.mycarenet.esb.common.v2.IdType;
import be.cin.mycarenet.esb.common.v2.LicenseType;
import be.cin.mycarenet.esb.common.v2.NihiiType;
import be.cin.mycarenet.esb.common.v2.OrigineType;
import be.cin.mycarenet.esb.common.v2.PackageType;
import be.cin.mycarenet.esb.common.v2.ValueRefString;

public final class CommonInputMapper {
	public static be.cin.mycarenet.esb.common.v2.CommonInput mapCommonInputType(final be.medx.mcn.CommonInput input) {
		final List<String> myMappingFiles = new ArrayList<String>();
		myMappingFiles.add("dozer/genasync-commoninput.xml");
		final DozerBeanMapper mapper = new DozerBeanMapper();
		mapper.setMappingFiles(myMappingFiles);
		final be.cin.mycarenet.esb.common.v2.CommonInput destObject = new be.cin.mycarenet.esb.common.v2.CommonInput();
		mapper.map(input, destObject);
		return destObject;
	}

	public static OrigineType mapOrigin(final Origin origin) {
		final OrigineType result = new OrigineType();
		final CareProvider careProvider = origin.getCareProvider();
		if (careProvider != null) {
			result.setCareProvider(mapCareProvider(careProvider));
		}
		final PackageInfo packageInfo = origin.getPackageInfo();
		if (packageInfo != null) {
			result.setPackage(mapPackage(packageInfo));
		}
		final String siteId = origin.getSiteId();
		if (siteId != null) {
			result.setSiteID(createValueRefString(siteId));
		}
		return result;
	}

	static PackageType mapPackage(final PackageInfo packageInfo) {
		final PackageType result = new PackageType();
		final LicenseType license = new LicenseType();
		final String password = packageInfo.getPassword();
		if (password != null) {
			license.setPassword(password);
		}
		final String userName = packageInfo.getUserName();
		if (userName != null) {
			license.setUsername(userName);
		}
		result.setLicense(license);
		final String packageName = packageInfo.getPackageName();
		if (packageName != null) {
			result.setName(createValueRefString(packageName));
		}
		return result;
	}

	static CareProviderType mapCareProvider(final CareProvider careProvider) {
		final CareProviderType result = new CareProviderType();
		final Nihii nihii = careProvider.getNihii();
		if (nihii != null) {
			result.setNihii(mapNihii(nihii));
		}
		final Identification organization = careProvider.getOrganization();
		if (organization != null) {
			result.setOrganization(mapIdType(organization));
		}
		final Identification physicalPerson = careProvider.getPhysicalPerson();
		if (physicalPerson != null) {
			result.setPhysicalPerson(mapIdType(physicalPerson));
		}
		return result;
	}

	static IdType mapIdType(final Identification organization) {
		final IdType result = new IdType();
		final String cbe = organization.getCbe();
		if (cbe != null) {
			result.setCbe(createValueRefString(cbe));
		}
		final String name = organization.getName();
		if (name != null) {
			result.setName(createValueRefString(name));
		}
		final Nihii nihii = organization.getNihii();
		if (nihii != null) {
			result.setNihii(mapNihii(nihii));
		}
		final String ssin = organization.getSsin();
		if (ssin != null) {
			result.setSsin(createValueRefString(ssin));
		}
		return result;
	}

	static NihiiType mapNihii(final Nihii nihii) {
		final NihiiType result = new NihiiType();
		final String quality = nihii.getQuality();
		if (quality != null) {
			result.setQuality(quality);
		}
		final String value = nihii.getValue();
		if (value != null) {
			result.setValue(createValueRefString(value));
		}
		return result;
	}

	static ValueRefString createValueRefString(final String value) {
		final ValueRefString result = new ValueRefString();
		result.setValue(value);
		return result;
	}
}
