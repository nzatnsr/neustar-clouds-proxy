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
import xdi2.discovery.XDIDiscoveryClient;
import xdi2.discovery.XDIDiscoveryException;
import xdi2.discovery.XDIDiscoveryResult;
import xdi2.core.Graph;
import xdi2.core.Literal;
import xdi2.core.Statement;
import xdi2.core.constants.XDIConstants;
import xdi2.core.constants.XDIDictionaryConstants;
import xdi2.core.constants.XDILinkContractConstants;
import xdi2.core.features.linkcontracts.GenericLinkContract;
import xdi2.core.features.linkcontracts.PublicLinkContract;
import xdi2.core.features.linkcontracts.RootLinkContract;
import xdi2.core.features.nodetypes.XdiAttribute;
import xdi2.core.features.nodetypes.XdiLocalRoot;

import xdi2.core.impl.memory.MemoryGraphFactory;
import xdi2.core.util.XDI3Util;
import xdi2.core.xri3.CloudName;
import xdi2.core.xri3.CloudNumber;
import xdi2.core.xri3.XDI3Segment;
import xdi2.core.xri3.XDI3Statement;
import xdi2.core.xri3.XDI3SubSegment;
import xdi2.discovery.XDIDiscoveryClient;
import xdi2.discovery.XDIDiscoveryResult;
import xdi2.messaging.Message;
import xdi2.messaging.MessageEnvelope;
import xdi2.messaging.MessageResult;

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
			result = getXDIDiscoveryClient().discoverFromRegistry(cloudName.getXri(), null);
		}
		/**
		catch( XDIDiscoveryException ex )
		{
			String error = "Guardian discovery failure";
			logger.error(error + " - " + info.getCloudName(), ex);
			throw new GuardianAuthenticationFailureException(error);
		}
		**/
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
		String url = result.getXdiEndpointUri();
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
		try
		{
			data.setCloudUrl(new URL(url));
		}
		catch( java.net.MalformedURLException mex )
		{
			String error = "Guardian authentication failure - malformed url " + url;
			logger.error(error + " - " + info.getCloudName(), mex);
			throw new GuardianAuthenticationFailureException(error);
		}
		return data;
	}

	public static DependentData verifyDependent( DependentInfo info )
	{
		CloudName cloudName = CloudName.create(info.getCloudName());
		XDIDiscoveryResult result = null;
		try
		{
			result = getXDIDiscoveryClient().discoverFromRegistry(cloudName.getXri(), null);
		}
		/**
		catch( XDIDiscoveryException ex )
		{
			String error = "Dependent discovery failure";
			logger.error(error + " - " + info.getCloudName(), ex);
			throw new DependentAuthenticationFailureException(error);
		}
		**/
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
		String url = result.getXdiEndpointUri();
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
		try
		{
			data.setCloudUrl(new URL(url));
		}
		catch( java.net.MalformedURLException mex )
		{
			String error = "Dependent authentication failure - malformed url " + url;
			logger.error(error + " - " + info.getCloudName(), mex);
			throw new GuardianAuthenticationFailureException(error);
		}
		return data;
	}

	public static List<DependentData> getDependent( GuardianData guardian )
	{
		List<DependentData> rtn = null;

		XDI3Segment isGuardian = XDI3Segment.create("$is#guardian");

		try
		{
			MessageEnvelope getDependentMessageEnvelope = new MessageEnvelope();
			Message getDependentMessage = getDependentMessageEnvelope.createMessage(guardian.getCloudNumber().getXri());
			getDependentMessage.setToPeerRootXri(guardian.getCloudNumber().getPeerRootXri());
			getDependentMessage.setLinkContractXri(RootLinkContract.createRootLinkContractXri(guardian.getCloudNumber().getXri()));
			getDependentMessage.setSecretToken(guardian.getSecretToken());
			getDependentMessage.createGetOperation(XDI3Statement.fromRelationComponents(guardian.getCloudNumber().getXri(), isGuardian, XDIConstants.XRI_S_VARIABLE));
			MessageResult getDependentResult = new XDIHttpClient(guardian.getCloudUrl().toString()).send(getDependentMessageEnvelope, null);

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
			XDIDiscoveryResult result = getXDIDiscoveryClient().discoverFromRegistry(cloudNumber.getXri(), null);
			String cloudUrl = result.getXdiEndpointUri();

			Graph tempGraph = MemoryGraphFactory.getInstance().openGraph();
			MessageEnvelope getMessageEnvelope = new MessageEnvelope();
			Message getMessage = getMessageEnvelope.createMessage(cloudNumber.getXri());
			getMessage.setToPeerRootXri(cloudNumber.getPeerRootXri());
			getMessage.setLinkContractXri(PublicLinkContract.createPublicLinkContractXri(cloudNumber.getXri()));
			getMessage.createGetOperation(XDI3Statement.fromRelationComponents(cloudNumber.getXri(), XDIDictionaryConstants.XRI_S_IS_REF, XDIConstants.XRI_S_VARIABLE));
			MessageResult getMessageResult = new XDIHttpClient(cloudUrl).send(getMessageEnvelope, null);
			for( Statement stmt1 : getMessageResult.getGraph().getRootContextNode().getAllStatements())
			{
				if( XDIDictionaryConstants.XRI_S_IS_REF.equals(stmt1.getPredicate()) == false )
				{
					logger.debug("getDependentData() - Ignore " + stmt1);
					continue;
				}
				logger.debug("getDependentData() - Process " + stmt1);
				String cloudName = stmt1.getObject().toString();
				DependentData rtn = new DependentData();
				rtn.setCloudName(cloudName);
				rtn.setCloudNumber(cloudNumber);
				rtn.setCloudUrl(new URL(cloudUrl));
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
			XdiAttribute proxyServerMessage = XdiLocalRoot.findLocalRoot(tempGraph)
				.getXdiEntity(cloudNumber.getXri(), true)
				.getXdiAttributeSingleton(XDI3Segment.create("<#proxy>"), true)
				.getXdiAttributeSingleton(XDI3Segment.create("<#server>"), true);
			proxyServerMessage.getXdiValue(true).setLiteralString(server);
			logger.info("Setting proxy server as '" + server + "'");
			logger.info(tempGraph.toString("XDI DISPLAY", null));

			MessageEnvelope messageEnvelope = new MessageEnvelope();
			Message message = messageEnvelope.createMessage(cloudNumber.getXri());
			message.setToPeerRootXri(cloudNumber.getPeerRootXri());
			message.setLinkContractXri(RootLinkContract.createRootLinkContractXri(cloudNumber.getXri()));
			message.setSecretToken(data.getSecretToken());
			message.createSetOperation(tempGraph);

			new XDIHttpClient(data.getCloudUrl().toString()).send(messageEnvelope, null);
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
			Message getMessage = getMessageEnvelope.createMessage(cloudNumber.getXri());
			getMessage.setToPeerRootXri(cloudNumber.getPeerRootXri());
			getMessage.setLinkContractXri(RootLinkContract.createRootLinkContractXri(cloudNumber.getXri()));
			getMessage.setSecretToken(data.getSecretToken());
			getMessage.createGetOperation(XDI3Statement.fromRelationComponents(cloudNumber.getXri(), XDI3Segment.create("<#proxy>"), XDI3Segment.create("[<#" + type + ">]")));
			MessageResult getMessageResult = new XDIHttpClient(data.getCloudUrl().toString()).send(getMessageEnvelope, null);

			logger.info(getMessageResult.getGraph().toString("XDI DISPLAY", null));

			for( Literal literal : getMessageResult.getGraph().getRootContextNode().getAllLiterals() )
			{
				String uuid = null;
				List<XDI3SubSegment> list = literal.getStatement().getSubject().getSubSegments();
				if( list != null )
				{
					for( XDI3SubSegment arc : list )
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
			XdiAttribute proxyAccessMessage = XdiLocalRoot.findLocalRoot(tempGraph)
				.getXdiEntity(cloudNumber.getXri(), true)
				.getXdiAttributeSingleton(XDI3Segment.create("<#proxy>"), true)
				.getXdiAttributeCollection(XDI3Segment.create("[<#" + type + ">]"), true)
				.setXdiMemberUnordered(null);
			proxyAccessMessage.getXdiValue(true).setLiteralString(AccessData.fromAccessData(acc));

			logger.info(tempGraph.toString("XDI DISPLAY", null));

			MessageEnvelope messageEnvelope = new MessageEnvelope();
			Message message = messageEnvelope.createMessage(cloudNumber.getXri());
			message.setToPeerRootXri(cloudNumber.getPeerRootXri());
			message.setLinkContractXri(RootLinkContract.createRootLinkContractXri(cloudNumber.getXri()));
			message.setSecretToken(data.getSecretToken());
			message.createSetOperation(tempGraph);

			MessageResult messageResult = new XDIHttpClient(data.getCloudUrl().toString()).send(messageEnvelope, null);

			logger.info(messageResult.getGraph().toString("XDI DISPLAY", null));

			for( Literal literal : tempGraph.getRootContextNode().getAllLiterals() )
			{
				String uuid = null;
				List<XDI3SubSegment> list = literal.getStatement().getSubject().getSubSegments();
				if( list != null )
				{
					for( XDI3SubSegment arc : list )
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
			Message message = messageEnvelope.createMessage(cloudNumber.getXri());
			message.setToPeerRootXri(cloudNumber.getPeerRootXri());
			message.setLinkContractXri(RootLinkContract.createRootLinkContractXri(cloudNumber.getXri()));
			message.setSecretToken(data.getSecretToken());
			message.createDelOperation(XDI3Util.concatXris(cloudNumber.getXri(), XDI3Segment.create("<#proxy>"), XDI3Segment.create("[<#" + type + ">]"), XDI3Segment.create("<!:uuid:" + uuid + ">")));
			MessageResult messageResult = new XDIHttpClient(data.getCloudUrl().toString()).send(messageEnvelope, null);

			logger.info(messageResult.getGraph().toString("XDI DISPLAY", null));
		}
		catch( Exception e )
		{
			logger.error("addAccessData() failed - " + e.getMessage(), e);
			throw new ProxyInternalErrorException("deleteAccessData() failed");
		}
	}
}
