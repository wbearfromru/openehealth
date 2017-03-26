package be.medx.mcn;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.zip.DataFormatException;

import be.cin.mycarenet.esb.common.v2.CommonInput;
import be.cin.mycarenet.esb.common.v2.OrigineType;
import be.cin.nip.async.generic.Confirm;
import be.cin.nip.async.generic.Get;
import be.cin.nip.async.generic.MsgQuery;
import be.cin.nip.async.generic.MsgResponse;
import be.cin.nip.async.generic.Post;
import be.cin.nip.async.generic.Query;
import be.cin.nip.async.generic.TAckResponse;
import be.cin.types.v1.Blob;
import be.medx.exceptions.TechnicalConnectorException;

public class RequestObjectBuilderImpl implements RequestObjectBuilder {
	@Override
	public final Post buildPostRequest(final CommonInput commonInput, final Blob blob, final byte[] xades) {
		final Post post = new Post();
		post.setCommonInput(commonInput);
		post.setDetail(blob);
		if (xades != null) {
			post.setXadesT(SendRequestMapper.mapB64fromByte(xades));
		}
		return post;
	}

	@Override
	public final Get buildGetRequest(final OrigineType origin, final MsgQuery msgQuery, final Query tackQuery) {
		final Get get = new Get();
		get.setMsgQuery(msgQuery);
		get.setOrigin(origin);
		get.setTAckQuery(tackQuery);
		return get;
	}

	@Override
	public final Confirm buildConfirmRequest(final OrigineType origin, final List<MsgResponse> msgResponses, final List<TAckResponse> tackResponses) throws TechnicalConnectorException, DataFormatException {
		final List<byte[]> msgHashValues = new ArrayList<byte[]>();
		final List<byte[]> tackContents = new ArrayList<byte[]>();
		if (msgResponses == null || msgResponses.isEmpty()) {
			msgHashValues.add(new byte[0]);
		} else {
			for (final MsgResponse msgResponse : msgResponses) {
				msgHashValues.add(msgResponse.getDetail().getHashValue());
			}
		}
		if (tackResponses == null || tackResponses.isEmpty()) {
			tackContents.add(new byte[0]);
		} else {
			for (final TAckResponse tackResponse : tackResponses) {
				tackContents.add(tackResponse.getTAck().getValue());
			}
		}
		return this.buildConfirmRequestWithHashes(origin, msgHashValues, tackContents);
	}

	@Override
	public Confirm buildConfirmRequestWithHashes(final OrigineType origin, final List<byte[]> msgHashValues, final List<byte[]> tackContents) {
		final Confirm confirm = new Confirm();
		confirm.setOrigin(origin);
		confirm.getMsgHashValues().addAll(msgHashValues);
		confirm.getTAckContents().addAll(tackContents);
		return confirm;
	}

	@Override
	public Query createQuery(final Integer max, final Boolean include) {
		final Query query = new Query();
		query.setInclude(include);
		query.setMax(max);
		return query;
	}

	@Override
	public MsgQuery createMsgQuery(final Integer max, final Boolean include, final String... messageNames) {
		final MsgQuery msgQuery = new MsgQuery();
		msgQuery.setInclude(include);
		msgQuery.setMax(max);
		for (final String messageName : messageNames) {
			msgQuery.getMessageNames().add(messageName);
		}
		return msgQuery;
	}

	public void initialize(final Map<String, Object> parameterMap) throws TechnicalConnectorException {
	}
}
