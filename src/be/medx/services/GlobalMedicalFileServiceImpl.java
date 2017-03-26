package be.medx.services;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.UUID;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;

import be.cin.nip.async.generic.Post;
import be.cin.nip.async.generic.PostResponse;
import be.fgov.ehealth.messageservices.core.v1.RequestType;
import be.fgov.ehealth.messageservices.core.v1.RetrieveTransactionRequest;
import be.fgov.ehealth.messageservices.core.v1.SelectRetrieveTransaction;
import be.fgov.ehealth.messageservices.core.v1.TransactionType;
import be.fgov.ehealth.standards.kmehr.cd.v1.CDHCPARTY;
import be.fgov.ehealth.standards.kmehr.cd.v1.CDHCPARTYschemes;
import be.fgov.ehealth.standards.kmehr.cd.v1.CDHCPARTYvalues;
import be.fgov.ehealth.standards.kmehr.cd.v1.CDTRANSACTION;
import be.fgov.ehealth.standards.kmehr.cd.v1.CDTRANSACTIONschemes;
import be.fgov.ehealth.standards.kmehr.id.v1.IDHCPARTY;
import be.fgov.ehealth.standards.kmehr.id.v1.IDHCPARTYschemes;
import be.fgov.ehealth.standards.kmehr.id.v1.IDKMEHR;
import be.fgov.ehealth.standards.kmehr.id.v1.IDKMEHRschemes;
import be.fgov.ehealth.standards.kmehr.schema.v1.AuthorType;
import be.fgov.ehealth.standards.kmehr.schema.v1.HcpartyType;
import be.medx.exceptions.TechnicalConnectorException;
import be.medx.mcn.Blob;
import be.medx.mcn.BlobBuilder;
import be.medx.mcn.BlobBuilderImpl;
import be.medx.mcn.CommonBuilder;
import be.medx.mcn.CommonInputMapper;
import be.medx.mcn.GenAsyncService;
import be.medx.mcn.GenAsyncServiceImpl;
import be.medx.mcn.GenericCommonBuilderImpl;
import be.medx.mcn.PackageInfo;
import be.medx.mcn.RequestObjectBuilderImpl;
import be.medx.mcn.ResponseObjectBuilder;
import be.medx.mcn.ResponseObjectBuilderImpl;
import be.medx.mcn.SendRequestMapper;
import be.medx.saml.IdGeneratorFactory;
import be.medx.soap.ws.GenericWsSenderImpl;
import be.medx.soap.ws.WsAddressingHeader;
import be.medx.xml.MarshallerHelper;

public class GlobalMedicalFileServiceImpl implements GlobalMedicalFileService {

	private UserService userService;
	private SAMLTokenService samlTokenService;
	private CryptoService cryptoService;

	public GlobalMedicalFileServiceImpl(UserService userService, SAMLTokenService samlTokenService, CryptoService cryptoService) {
		this.samlTokenService = samlTokenService;
		this.userService = userService;
		this.cryptoService = cryptoService;
	}

	@Override
	public String requestGMDList() throws TechnicalConnectorException {
		DateTime requestDate = new DateTime();
		return PostAsync(requestDate, "GMD-CONSULT-HCP");
	}

	private String PostAsync(DateTime requestDate, String requestType) throws TechnicalConnectorException {

		RetrieveTransactionRequest request = new RetrieveTransactionRequest();
		RequestType rt = new RequestType();
		DateTime dt = new DateTime();
		String datestr = DateTimeFormat.forPattern("yyyyMMddHHmmss").print(new DateTime());

		IDKMEHR id = createKMEHRID(this.userService.getRIZIV() + "." + datestr);
		rt.setId(id);
		rt.setAuthor(createAuthorType());
		rt.setDate(new DateTime());
		rt.setTime(new DateTime());
		request.setRequest(rt);

		SelectRetrieveTransaction srt = new SelectRetrieveTransaction();

		TransactionType tt = createMyCarenetTransactionType(requestDate);
		tt.setAuthor(createAuthorType());
		srt.setTransaction(tt);
		request.setSelect(srt);

		MarshallerHelper<RetrieveTransactionRequest, RetrieveTransactionRequest> Marshaller = new MarshallerHelper<RetrieveTransactionRequest, RetrieveTransactionRequest>(RetrieveTransactionRequest.class, RetrieveTransactionRequest.class);
		String XML = Marshaller.toString(request);
		String requestIdentifier = datestr;
		return PostRequest(requestIdentifier, XML, requestType);
	}

	private String PostRequest(String requestIdentifier, String XML, String messageName) throws TechnicalConnectorException {
		byte[] content = XML.getBytes();
		BlobBuilder bbuilder = new BlobBuilderImpl("dmg");
		Blob blob = bbuilder.build(content, "deflate", "_" + UUID.randomUUID().toString(), "text/xml");
		blob.setMessageName(messageName);
		post(new PostParameter(blob, false, "dmg", false, null, "urn:be:cin:nip:async:generic:post:msg", requestIdentifier));
		return null; // FIXME should return a reference to the message sent
	}

	private PostResponse post(PostParameter parameterObject) throws TechnicalConnectorException {
		if (parameterObject.useXades) {
			// no xades needed for dmg async
			throw new java.lang.UnsupportedOperationException("does not support xades yet ");
		}

		PackageInfo packageInfo = new PackageInfo("phycompugroup", "ph1c4mp7gr9p", "Compugroup Medical Belgium bvba");
		be.cin.mycarenet.esb.common.v2.CommonInput ci = CommonInputMapper.mapCommonInputType(getCommonBuilder("genericasync").createCommonInput(packageInfo, parameterObject.istest, parameterObject.requestIdentifier));
		be.cin.types.v1.Blob det = SendRequestMapper.mapBlobToCinBlob(parameterObject.blob);

		Post post = new RequestObjectBuilderImpl().buildPostRequest(ci, det, null);

		GenAsyncService service = new GenAsyncServiceImpl(parameterObject.serviceName, "https://pilot.mycarenet.be/mycarenet-ws/async/generic/gmd", new GenericWsSenderImpl());
		WsAddressingHeader header = null;
		try {
			header = new WsAddressingHeader(new URI(parameterObject.addressingHeaderUrl));
		} catch (URISyntaxException e) {
			e.printStackTrace();
			throw new TechnicalConnectorException();
		}

		try {
			header.setTo(new URI(""));
		} catch (URISyntaxException e) {
			e.printStackTrace();
			throw new TechnicalConnectorException();
		}

		header.setFaultTo("http://www.w3.org/2005/08/addressing/anonymous");
		header.setReplyTo("http://www.w3.org/2005/08/addressing/anonymous");
		if (parameterObject.oaNumber != null) {
			try {
				header.setTo(new URI("urn:nip:destination:io:" + parameterObject.oaNumber));
			} catch (URISyntaxException e) {
				e.printStackTrace();
				throw new TechnicalConnectorException();
			}

		}
		try {
			header.setMessageID(new URI(IdGeneratorFactory.getIdGenerator("uuid").generateId()));
		} catch (URISyntaxException e) {
			e.printStackTrace();
			throw new TechnicalConnectorException();
		}

		PostResponse responsePost = service.postRequest(this.samlTokenService.getSAMLToken(), post, header);
		// storePostAndResponse(parameterObject.blob);
		ResponseObjectBuilder responseBuilder = new ResponseObjectBuilderImpl(this.cryptoService);
		Boolean hasWarnings = responseBuilder.handlePostResponse(responsePost);
		return responsePost;
	}

	private TransactionType createMyCarenetTransactionType(DateTime Adate) {
		TransactionType tt = new TransactionType();
		CDTRANSACTION gmdtran = CreateTransactionType();
		tt.getCds().add(gmdtran);
		tt.setBegindate(Adate);
		return tt;
	}

	private CDTRANSACTION CreateTransactionType() {
		be.fgov.ehealth.standards.kmehr.cd.v1.ObjectFactory cdfact = new be.fgov.ehealth.standards.kmehr.cd.v1.ObjectFactory();
		CDTRANSACTION gmdtran = cdfact.createCDTRANSACTION();
		gmdtran.setS(CDTRANSACTIONschemes.CD_TRANSACTION_MYCARENET);
		gmdtran.setSV("1.0");
		gmdtran.setValue("gmd");
		return gmdtran;
	}

	private AuthorType createAuthorType() {
		AuthorType author = new AuthorType();
		be.fgov.ehealth.standards.kmehr.schema.v1.HcpartyType hcparty = createHCParty();
		author.getHcparties().add(hcparty);
		return author;
	}

	private HcpartyType createHCParty() {
		HcpartyType hcparty = new HcpartyType();
		CDHCPARTY cd = new CDHCPARTY();
		cd.setS(CDHCPARTYschemes.CD_HCPARTY);
		cd.setSV("1.0");
		cd.setValue(CDHCPARTYvalues.PERSPHYSICIAN.value());
		hcparty.getCds().add(cd);
		hcparty.setFamilyname(this.userService.getLastName());
		hcparty.setFirstname(this.userService.getFirstName());
		IDHCPARTY idhcp = new IDHCPARTY();
		idhcp.setS(IDHCPARTYschemes.ID_HCPARTY);
		idhcp.setSV("1.0");
		idhcp.setValue(this.userService.getRIZIV());
		hcparty.getIds().add(idhcp);

		IDHCPARTY idhcp2 = new IDHCPARTY();
		idhcp2.setS(IDHCPARTYschemes.INSS);
		idhcp2.setSV("1.0");
		idhcp2.setValue(this.userService.getSSIN());
		hcparty.getIds().add(idhcp2);
		return hcparty;
	}

	private static IDKMEHR createKMEHRID(String Value) {
		IDKMEHR id = new IDKMEHR();
		id.setS(IDKMEHRschemes.ID_KMEHR);
		id.setSV("1.0");
		id.setValue(Value);
		return id;
	}

	public CommonBuilder getCommonBuilder(final String projectName) throws TechnicalConnectorException {
		return new GenericCommonBuilderImpl("default", this.userService.getRIZIV(), this.userService.getFirstName() + " " + this.userService.getLastName(), this.userService.getSSIN());
	}

}
