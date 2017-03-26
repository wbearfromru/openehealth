// 
// Decompiled by Procyon v0.5.29
// 

package be.medx.mcn;

import java.util.List;
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

public interface RequestObjectBuilder {
	Query createQuery(Integer p0, Boolean p1);

	MsgQuery createMsgQuery(Integer p0, Boolean p1, String... p2);

	Post buildPostRequest(CommonInput p0, Blob p1, byte[] p2);

	Get buildGetRequest(OrigineType p0, MsgQuery p1, Query p2);

	Confirm buildConfirmRequest(OrigineType p0, List<MsgResponse> p1, List<TAckResponse> p2) throws TechnicalConnectorException, DataFormatException;

	Confirm buildConfirmRequestWithHashes(OrigineType p0, List<byte[]> p1, List<byte[]> p2);
}
