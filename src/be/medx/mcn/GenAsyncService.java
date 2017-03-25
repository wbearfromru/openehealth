package be.medx.mcn;

import be.cin.nip.async.generic.Confirm;
import be.cin.nip.async.generic.ConfirmResponse;
import be.cin.nip.async.generic.Get;
import be.cin.nip.async.generic.GetResponse;
import be.cin.nip.async.generic.Post;
import be.cin.nip.async.generic.PostResponse;
import be.medx.exceptions.TechnicalConnectorException;
import be.medx.saml.SAMLToken;
import be.medx.soap.ws.WsAddressingHeader;

public interface GenAsyncService {
	PostResponse postRequest(SAMLToken p0, Post p1, WsAddressingHeader p2) throws TechnicalConnectorException;

	GetResponse getRequest(SAMLToken p0, Get p1, WsAddressingHeader p2) throws TechnicalConnectorException;

	ConfirmResponse confirmRequest(SAMLToken p0, Confirm p1, WsAddressingHeader p2) throws TechnicalConnectorException;
}
