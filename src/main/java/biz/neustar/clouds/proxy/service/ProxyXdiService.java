package biz.neustar.clouds.proxy.service;

import biz.neustar.clouds.proxy.*;
import biz.neustar.clouds.proxy.model.*;
import biz.neustar.clouds.proxy.service.*;
import biz.neustar.clouds.proxy.exception.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.*;

import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicInteger;

import org.littleshoot.proxy.*;
import org.littleshoot.proxy.impl.*;

import org.apache.commons.beanutils.*;

import io.netty.handler.codec.http.*;


import xdi2.client.http.XDIHttpClient;
import xdi2.client.util.XDIClientUtil;
import xdi2.client.exceptions.Xdi2ClientException;
import xdi2.client.exceptions.Xdi2ClientException;
import xdi2.client.exceptions.Xdi2DiscoveryException;
import xdi2.core.Graph;
import xdi2.core.Literal;
import xdi2.core.Statement;
import xdi2.core.constants.XDIConstants;
import xdi2.core.constants.XDIDictionaryConstants;
import xdi2.core.constants.XDILinkContractConstants;
import xdi2.core.features.linkcontracts.instance.GenericLinkContract;
import xdi2.core.features.linkcontracts.instance.PublicLinkContract;
import xdi2.core.features.linkcontracts.instance.RootLinkContract;
import xdi2.core.features.nodetypes.XdiAttribute;
import xdi2.core.features.nodetypes.XdiCommonRoot;

import xdi2.core.impl.memory.MemoryGraphFactory;
import xdi2.core.syntax.CloudName;
import xdi2.core.syntax.CloudNumber;
import xdi2.core.syntax.XDIAddress;
import xdi2.core.syntax.XDIArc;
import xdi2.core.util.XDIAddressUtil;
import xdi2.discovery.XDIDiscoveryClient;
import xdi2.discovery.XDIDiscoveryResult;
import xdi2.messaging.Message;
import xdi2.messaging.MessageEnvelope;
import xdi2.messaging.MessageResult;

import xdi2.core.syntax.XDIStatement;

public class ProxyXdiService
{
	private static final Logger logger = LoggerFactory.getLogger(ProxyXdiService.class);
	private static XDIDiscoveryClient discoveryClient;

	private static XDIDiscoveryClient getXDIDiscoveryClient()
	{
		if( discoveryClient != null )
		{
			return discoveryClient;
		}
		if( ProxyConfig.XDI_ENV_PROD.equals(ProxyApplication.getConfig().getXdiEnv()) == true )
		{
			discoveryClient = XDIDiscoveryClient.DEFAULT_DISCOVERY_CLIENT;
		}
		else if( ProxyConfig.XDI_ENV_OTE.equals(ProxyApplication.getConfig().getXdiEnv()) == true )
		{
			discoveryClient = XDIDiscoveryClient.NEUSTAR_OTE_DISCOVERY_CLIENT;
		}
		else
		{
			logger.error("Unknown XDI env - " + ProxyApplication.getConfig().getXdiEnv());
			discoveryClient = XDIDiscoveryClient.NEUSTAR_OTE_DISCOVERY_CLIENT;
		}
		return discoveryClient;
	}

	public static GuardianData verifyGuardian( GuardianInfo info )
	{
		CloudName cloudName = CloudName.create(info.getCloudName());
		XDIDiscoveryResult result = null;
		try
		{
			result = getXDIDiscoveryClient().discoverFromRegistry(cloudName.getXDIAddress(), null);
		}
		catch( Xdi2DiscoveryException ex )
		{
			String error = "Guardian discovery failure";
			logger.error(error + " - " + info.getCloudName(), ex);
			throw new GuardianAuthenticationFailureException(error);
		}
		catch( Xdi2ClientException ex )
		{
			String error = "Guardian discovery client failure";
			logger.error(error + " - " + info.getCloudName(), ex);
			throw new GuardianAuthenticationFailureException(error);
		}
		CloudNumber cloudNumber = result.getCloudNumber();
		if( cloudNumber == null )
		{
			throw new DependentAuthenticationFailureException("Cannot find guardian cloud number");
		}
		URL url = result.getXdiEndpointUrl();
		try
		{
			XDIClientUtil.authenticateSecretToken(cloudNumber, url, info.getSecretToken());
			logger.info("Authenticated successflly for " + info.getCloudName());
		}
		catch( Xdi2ClientException ex )
		{
			String error = "Guardian authentication failure";
			logger.error(error + " - " + info.getCloudName(), ex);
			throw new GuardianAuthenticationFailureException(error);
		}
		GuardianData data = new GuardianData(info);
		data.setCloudNumber(cloudNumber);
		data.setCloudUrl(url);
		return data;
	}

	public static DependentData verifyDependent( DependentInfo info )
	{
		CloudName cloudName = CloudName.create(info.getCloudName());
		XDIDiscoveryResult result = null;
		try
		{
			result = getXDIDiscoveryClient().discoverFromRegistry(cloudName.getXDIAddress(), null);
		}
		catch( Xdi2DiscoveryException ex )
		{
			String error = "Dependent discovery failure";
			logger.error(error + " - " + info.getCloudName(), ex);
			throw new DependentAuthenticationFailureException(error);
		}
		catch( Xdi2ClientException ex )
		{
			String error = "Dependent discovery client failure";
			logger.error(error + " - " + info.getCloudName(), ex);
			throw new DependentAuthenticationFailureException(error);
		}
		CloudNumber cloudNumber = result.getCloudNumber();
		if( cloudNumber == null )
		{
			throw new DependentAuthenticationFailureException("Cannot find dependent cloud number");
		}
		URL url = result.getXdiEndpointUrl();
		try
		{
			XDIClientUtil.authenticateSecretToken(cloudNumber, url, info.getSecretToken());
			logger.info("Authenticated successflly for " + info.getCloudName());
		}
		catch( Xdi2ClientException ex )
		{
			String error = "Dependent authentication failure";
			logger.error(error + " - " + info.getCloudName(), ex);
			throw new DependentAuthenticationFailureException(error);
		}
		DependentData data = new DependentData(info);
		data.setCloudNumber(cloudNumber);
		data.setCloudUrl(url);
		return data;
	}

	public static List<DependentData> getDependent( GuardianData guardian )
	{
		List<DependentData> rtn = null;

		XDIAddress isGuardian = XDIAddress.create("$is#guardian");

		try
		{
			MessageEnvelope getDependentMessageEnvelope = new MessageEnvelope();
			Message getDependentMessage = getDependentMessageEnvelope.createMessage(guardian.getCloudNumber().getXDIAddress());
			getDependentMessage.setToPeerRootXDIArc(guardian.getCloudNumber().getPeerRootXDIArc());
			getDependentMessage.setLinkContract(RootLinkContract.class);
			getDependentMessage.setSecretToken(guardian.getSecretToken());
			getDependentMessage.createGetOperation(XDIStatement.fromRelationComponents(guardian.getCloudNumber().getXDIAddress(), isGuardian, XDIConstants.XDI_ADD_VARIABLE));
			MessageResult getDependentResult = new XDIHttpClient(guardian.getCloudUrl()).send(getDependentMessageEnvelope, null);

			logger.info(getDependentResult.getGraph().toString("XDI DISPLAY", null));

			for( Statement stmt : getDependentResult.getGraph().getRootContextNode().getAllStatements() )
			{
				if( isGuardian.equals(stmt.getPredicate()) == false )
				{
					logger.debug("getDependent() - Ignore " + stmt);
					continue;
				}
				logger.debug("getDependent() - Process " + stmt);
				DependentData data = getDependentData(stmt);
				if( data != null )
				{
					if( rtn == null )
					{
						rtn = new ArrayList<DependentData>();
					}
					rtn.add(data);
				}
			}
		}
		catch( Exception e )
		{
			logger.error("getDependent() failed - " + e.getMessage(), e);
		}
		return rtn;
	}

	public static DependentData getDependentData( Statement stmt )
	{
		try
		{
			CloudNumber cloudNumber = CloudNumber.create(stmt.getObject().toString());
			XDIDiscoveryResult result = getXDIDiscoveryClient().discoverFromRegistry(cloudNumber.getXDIAddress(), null);
			URL cloudUrl = result.getXdiEndpointUrl();
	
			Graph tempGraph = MemoryGraphFactory.getInstance().openGraph();
			MessageEnvelope getMessageEnvelope = new MessageEnvelope();
			Message getMessage = getMessageEnvelope.createMessage(cloudNumber.getXDIAddress());
			getMessage.setToPeerRootXDIArc(cloudNumber.getPeerRootXDIArc());
                        getMessage.setLinkContract(PublicLinkContract.class);
                        getMessage.createGetOperation(XDIStatement.fromRelationComponents(cloudNumber.getXDIAddress(), XDIDictionaryConstants.XDI_ADD_IS_REF, XDIConstants.XDI_ADD_VARIABLE));
			MessageResult getMessageResult = new XDIHttpClient(cloudUrl).send(getMessageEnvelope, null);
			for( Statement stmt1 : getMessageResult.getGraph().getRootContextNode().getAllStatements())
			{
				if( XDIDictionaryConstants.XDI_ADD_IS_REF.equals(stmt1.getPredicate()) == false )
				{
					logger.debug("getDependentData() - Ignore " + stmt1);
					continue;
				}
				logger.debug("getDependentData() - Process " + stmt1);
				String cloudName = stmt1.getObject().toString();
				DependentData rtn = new DependentData();
				rtn.setCloudName(cloudName);
				rtn.setCloudNumber(cloudNumber);
				rtn.setCloudUrl(cloudUrl);
				return rtn;
			}
		}
		catch( Exception e )
		{
			logger.error("getDependentData() failed - " + e.getMessage(), e);
		}
		logger.error("getDependentData() - Cannot find dependent cloud " + stmt);
		return null;
	}

	public static void setDependentProxyServer( DependentData data )
	{
		logger.info("setDependentProxyServer - " + data);
		try
		{
			String server = "";
			if( data.getPort() != null )
			{
				server = data.getHostname() + ":" + data.getPort();
			}
			CloudNumber cloudNumber = data.getCloudNumber();
			Graph tempGraph = MemoryGraphFactory.getInstance().openGraph();
			XdiAttribute proxyServerMessage = XdiCommonRoot.findCommonRoot(tempGraph)
				.getXdiEntity(cloudNumber.getXDIAddress(), true)
				.getXdiAttributeSingleton(XDIAddress.create("<#proxy>"), true)
				.getXdiAttributeSingleton(XDIAddress.create("<#server>"), true);
			proxyServerMessage.getXdiValue(true).setLiteralString(server);
			logger.info("Setting proxy server as '" + server + "'");
			logger.info(tempGraph.toString("XDI DISPLAY", null));

			MessageEnvelope messageEnvelope = new MessageEnvelope();
			Message message = messageEnvelope.createMessage(cloudNumber.getXDIAddress());
			message.setToPeerRootXDIArc(cloudNumber.getPeerRootXDIArc());
			message.setLinkContract(RootLinkContract.class);
			message.setSecretToken(data.getSecretToken());
			message.createSetOperation(tempGraph);

			new XDIHttpClient(data.getCloudUrl()).send(messageEnvelope, null);
		}
		catch( Exception e )
		{
			logger.error("setDependentProxyServer() failed - " + e.getMessage(), e);
		}
	}

	public static List<AccessData> getAccessDataList( DependentData data, String type )
	{
		logger.info("getAccessDataList " + type + " " + data);
		List<AccessData> rtn = new ArrayList<AccessData>();
		try
		{
			CloudNumber cloudNumber = data.getCloudNumber();
			MessageEnvelope getMessageEnvelope = new MessageEnvelope();
			Message getMessage = getMessageEnvelope.createMessage(cloudNumber.getXDIAddress());
			getMessage.setToPeerRootXDIArc(cloudNumber.getPeerRootXDIArc());
			getMessage.setLinkContract(RootLinkContract.class);
			getMessage.setSecretToken(data.getSecretToken());
			getMessage.createGetOperation(XDIAddressUtil.concatXDIAddresses(cloudNumber.getXDIAddress(), XDIAddress.create("<#proxy>"), XDIAddress.create("[<#" + type + ">]")));
			MessageResult getMessageResult = new XDIHttpClient(data.getCloudUrl()).send(getMessageEnvelope, null);

			logger.info(getMessageResult.getGraph().toString("XDI DISPLAY", null));

			for( Literal literal : getMessageResult.getGraph().getRootContextNode().getAllLiterals() )
			{
				String uuid = null;
				List<XDIArc> list = literal.getStatement().getSubject().getXDIArcs();
				if( list != null )
				{
					for( XDIArc arc : list )
					{
						String s = arc.getLiteral();
						if( (s != null) && s.startsWith(":uuid:") )
						{
							uuid = s.substring(6);
						}
					}
				}
				if( uuid != null )
				{
					AccessData access = AccessData.toAccessData(literal.getLiteralDataString(), uuid);
					if( access != null )
					{
						if( access.getType() == null )
						{
							access.setType(type);
						}
						if( rtn == null )
						{
							rtn = new ArrayList<AccessData>();
						}
						rtn.add(access);
					}
				}
			}
		}
		catch( Exception e )
		{
			logger.error("getAccessDataList() failed - " + e.getMessage(), e);
		}
		return rtn;
	}

	public static void addAccessData( DependentData data, String type, AccessData acc )
	{
		try
		{
			CloudNumber cloudNumber = data.getCloudNumber();
			Graph tempGraph = MemoryGraphFactory.getInstance().openGraph();
			XdiAttribute proxyAccessMessage = XdiCommonRoot.findCommonRoot(tempGraph)
				.getXdiEntity(cloudNumber.getXDIAddress(), true)
				.getXdiAttributeSingleton(XDIAddress.create("<#proxy>"), true)
				.getXdiAttributeCollection(XDIAddress.create("[<#" + type + ">]"), true)
				.setXdiMemberUnordered(null);
			proxyAccessMessage.getXdiValue(true).setLiteralString(AccessData.fromAccessData(acc));

			logger.info(tempGraph.toString("XDI DISPLAY", null));

			MessageEnvelope messageEnvelope = new MessageEnvelope();
			Message message = messageEnvelope.createMessage(cloudNumber.getXDIAddress());
			message.setToPeerRootXDIArc(cloudNumber.getPeerRootXDIArc());
			message.setLinkContract(RootLinkContract.class);
			message.setSecretToken(data.getSecretToken());
			message.createSetOperation(tempGraph);

			MessageResult messageResult = new XDIHttpClient(data.getCloudUrl()).send(messageEnvelope, null);

			logger.info(messageResult.getGraph().toString("XDI DISPLAY", null));

			for( Literal literal : tempGraph.getRootContextNode().getAllLiterals() )
			{
				String uuid = null;
				List<XDIArc> list = literal.getStatement().getSubject().getXDIArcs();
				if( list != null )
				{
					for( XDIArc arc : list )
					{
						String s = arc.getLiteral();
						if( (s != null) && s.startsWith(":uuid:") )
						{
							uuid = s.substring(6);
						}
					}
				}
				if( uuid != null )
				{
					acc.setUuid(uuid);
					return;
				}
			}
			logger.error("addAccessData() failed - uuid not found");
		}
		catch( Exception e )
		{
			logger.error("addAccessData() failed - " + e.getMessage(), e);
		}
		throw new ProxyInternalErrorException("addAccessData() failed");
	}

	public static void deleteAccessData( DependentData data, String type, String uuid )
	{
		try
		{
			CloudNumber cloudNumber = data.getCloudNumber();

			MessageEnvelope messageEnvelope = new MessageEnvelope();
			Message message = messageEnvelope.createMessage(cloudNumber.getXDIAddress());
			message.setToPeerRootXDIArc(cloudNumber.getPeerRootXDIArc());
			message.setLinkContract(RootLinkContract.class);
			message.setSecretToken(data.getSecretToken());
			message.createDelOperation(XDIAddressUtil.concatXDIAddresses(cloudNumber.getXDIAddress(), XDIAddress.create("<#proxy>"), XDIAddress.create("[<#" + type + ">]"), XDIAddress.create("<!:uuid:" + uuid + ">")));

			MessageResult messageResult = new XDIHttpClient(data.getCloudUrl()).send(messageEnvelope, null);

			logger.info(messageResult.getGraph().toString("XDI DISPLAY", null));
		}
		catch( Exception e )
		{
			logger.error("addAccessData() failed - " + e.getMessage(), e);
			throw new ProxyInternalErrorException("deleteAccessData() failed");
		}
	}
}
