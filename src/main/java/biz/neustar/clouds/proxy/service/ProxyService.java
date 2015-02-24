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

import org.apache.commons.lang.StringUtils;

public class ProxyService
{
	private static final Logger logger = LoggerFactory.getLogger(ProxyService.class);

	private static ProxyService singleton = null;

	private final  AtomicLong counter = new AtomicLong();
	private int               portNumber =    8900; 
	private int               minPortNumber = 8900; 
	private int               maxPortNumber = 9000; 

	private List<DependentData> dependentList;
	private GuardianData        guardian;

	public static ProxyService getInstance()
	{
		if( singleton == null )
		{
			singleton = new ProxyService();
		}
		return singleton;
	}

	private long now()
	{
		return new Date().getTime();
	}

	public ProxyService()
	{
		this.dependentList = new ArrayList<DependentData>();
		if( ProxyApplication.getConfig().getMinPortNumber() != null )
		{
			this.minPortNumber = ProxyApplication.getConfig().getMinPortNumber().intValue();
		}
		if( ProxyApplication.getConfig().getMaxPortNumber() != null )
		{
			this.maxPortNumber = ProxyApplication.getConfig().getMaxPortNumber().intValue();
		}
		this.portNumber = this.minPortNumber;
		if( this.minPortNumber > this.maxPortNumber )
		{
			this.portNumber    = this.maxPortNumber;
			this.maxPortNumber = this.minPortNumber;
			this.minPortNumber = this.portNumber;
		}
	}

	private AccessInfo getAccessInfo( AccessData data )
	{
		AccessInfo rtn = null;
		try
		{	
			rtn = new AccessInfo();
			BeanUtils.copyProperties(rtn, data);
		}
		catch( Exception e )
		{
			String error = "getAccessInfo() failed";
			logger.error(error + " - " + this.guardian, e);
			throw new ProxyInternalErrorException(error);
		}

		return rtn;
	}

	private GuardianInfo getGuardianInfo( GuardianData data )
	{
		GuardianInfo rtn = null;
		try
		{	
			rtn = new GuardianInfo();
			BeanUtils.copyProperties(rtn, data);
		}
		catch( Exception e )
		{
			String error = "getGuardianInfo() failed";
			logger.error(error + " - " + this.guardian, e);
			throw new ProxyInternalErrorException(error);
		}

		return rtn;
	}

	public GuardianInfo getGuardian()
	{
		GuardianInfo rtn = null;
		if( this.guardian != null )
		{
			rtn = getGuardianInfo(this.guardian);
		}
		else
		{
			throw new GuardianAuthenticationFailureException("Guardian authentication not performed");
		}
		rtn.setSecretToken(null);
		return rtn;
	}

	public GuardianInfo setGuardian( GuardianInfo info )
	{
		if( this.guardian != null )
		{
			if(    this.guardian.getCloudName().equals(info.getCloudName())
			    && this.guardian.getSecretToken().equals(info.getSecretToken()) )
			{
				GuardianInfo rtn = getGuardianInfo(this.guardian);
				rtn.setSecretToken(null);
				return rtn;
			}
			throw new GuardianAuthenticationCompletedException("Guardian authentication already completed");
		}
		GuardianData data = ProxyXdiService.verifyGuardian(info);
		if( data != null )
		{
			this.guardian = data;
			data.setTimeStarted(new Date());
		}
		else
		{
			throw new GuardianAuthenticationFailureException("Guardian authentication failure");
		}
		List<DependentData> list = ProxyXdiService.getDependent(data);
		if( list != null )
		{
			this.dependentList = list;
		}
		GuardianInfo rtn = getGuardianInfo(data);
		rtn.setSecretToken(null);
		return rtn;
	}

	public GuardianInfo deleteGuardian( GuardianInfo info )
	{
		if( this.guardian == null )
		{
			throw new GuardianAuthenticationFailureException("Guardian authentication not performed");
		}
		if(    (this.guardian.getCloudName().equals(info.getCloudName()) == false)
		    || (this.guardian.getSecretToken().equals(info.getSecretToken()) == false) )
		{
			throw new GuardianAuthenticationFailureException("Guardian authentication failure");
		}
		for( DependentData dep : this.dependentList )
		{
			if( dep.getPort() != null )
			{
				throw new DependentProxyNotTerminatedException("Dependent proxy not terminated");
			}
		}
		GuardianInfo rtn = getGuardianInfo(this.guardian);
		rtn.setSecretToken(null);
		this.guardian = null;
		this.dependentList.clear();
		return rtn;
	}

	public List<DependentInfo> listDependentProxy()
	{
		logger.info("listDependentProxy");

		if( this.guardian == null )
		{
			throw new GuardianAuthenticationFailureException("Guardian authentication not performed");
		}

		List<DependentInfo> rtn = new ArrayList<DependentInfo>();
		for( DependentData dep : dependentList )
		{
			DependentInfo info = getDependentInfo(dep);
			info.setSecretToken(null);
			rtn.add(info);
		}
		return rtn;
	}

	public DependentInfo getDependentProxy( String cloudName )
	{
		logger.info("getDependentProxy - " + cloudName);

		if( this.guardian == null )
		{
			throw new GuardianAuthenticationFailureException("Guardian authentication not performed");
		}

		if( StringUtils.isBlank(cloudName) == true )
		{
			throw new CloudNameInvalidException("Dependent cloud name invalid");
		}
		for( DependentData data : this.dependentList )
		{
			if( cloudName.equals(data.getCloudName()) == true )
			{
				DependentInfo rtn = getDependentInfo(data);
				rtn.setSecretToken(null);
				return rtn;
			}
		}
		throw new DependentProxyNotFoundException("Dependent info/proxy not found");
	}

	private DependentInfo getDependentInfo( DependentData data )
	{
		DependentInfo rtn = new DependentInfo();
		try
		{
			BeanUtils.copyProperties(rtn, data);
		}
		catch( Exception e )
		{
			String error = "getDependentInfo() failed";
			logger.error(error + " - " + this.guardian, e);
			throw new ProxyInternalErrorException(error);
		}
		return rtn;
	}

	public DependentInfo startDependentProxy( DependentInfo info )
	{
		logger.info("startDependentProxy - " + info);

		if( this.guardian == null )
		{
			throw new GuardianAuthenticationFailureException("Guardian authentication not performed");
		}
		DependentData found = null;
		for( DependentData dep : this.dependentList )
		{
			if( info.getCloudName().equals(dep.getCloudName()) )
			{
				found = dep;
				break;
			}
		}
		if( found == null )
		{
			throw new DependentCloudNameNotFoundException("cloudname is not a dependent");
		}
		if( found.getPort() != null )
		{
			DependentInfo rtn = getDependentInfo(found);
			rtn.setSecretToken(null);
			return rtn;
		}
			
		DependentData data = ProxyXdiService.verifyDependent(info);
		if( data == null )
		{
			throw new DependentAuthenticationFailureException("Dependent authentication failure");
		}

		found.setSecretToken(info.getSecretToken());
		found.setCloudNumber(data.getCloudNumber());
		found.setCloudUrl(data.getCloudUrl());
		found.setTimeStarted(new Date());
		found.setPort(this.getPortNumber());
		found.setHostname(ProxyApplication.getConfig().getHostname());
		found.setNumberOfRequests(new AtomicInteger());
		found.setNumberOfRequestsBlocked(new AtomicInteger());
		found.setNumberOfRequestsAllowed(new AtomicInteger());

		ProxyXdiService.setDependentProxyServer(found);

		List<AccessData> blocked   = ProxyXdiService.getAccessDataList(found, AccessInfo.TYPE_BLOCKED);
		List<AccessData> allowed   = ProxyXdiService.getAccessDataList(found, AccessInfo.TYPE_ALLOWED);
		List<AccessData> requested = ProxyXdiService.getAccessDataList(found, AccessInfo.TYPE_REQUESTED);
		List<AccessData> log       = ProxyXdiService.getAccessDataList(found, AccessInfo.TYPE_LOG);

		found.setBlocked(blocked);
		found.setAllowed(allowed);
		found.setRequested(requested);
		found.setLog(log);

		ProxyFilter filter = new ProxyFilter(found);
		HttpProxyServer server = DefaultHttpProxyServer.bootstrap().withPort(found.getPort().intValue()).withFiltersSource(filter).withAllowLocalOnly(false).start();
                found.setServer(server);


		DependentInfo rtn = getDependentInfo(found);
		rtn.setSecretToken(null);
		return rtn;
	}

	public DependentInfo stopDependentProxy( DependentInfo data )
	{
		logger.info("stopDepenentProxy - " + data);

		if( this.guardian == null )
		{
			throw new GuardianAuthenticationFailureException("Guardian authentication not performed");
		}
		DependentData found = null;
		for( DependentData dep : this.dependentList )
		{
			if( data.getCloudName().equals(dep.getCloudName()) )
			{
				found = dep;
				break;
			}
		}
		if( found == null )
		{
			throw new DependentCloudNameNotFoundException("cloudname is not a dependent");
		}
		if( found.getPort() == null )
		{
			DependentInfo rtn = getDependentInfo(found);
			rtn.setSecretToken(null);
			return rtn;
		}
		if( data.getSecretToken().equals(found.getSecretToken()) == false )
		{
			throw new DependentAuthenticationFailureException("Dependent authentication failure");
		}
		found.getServer().stop();
		found.setServer(null);
		found.setTimeStarted(null);
		found.setPort(null);
		found.setHostname(null);
		found.setNumberOfRequests(null);
		found.setNumberOfRequestsBlocked(null);
		found.setNumberOfRequestsAllowed(null);

		ProxyXdiService.setDependentProxyServer(found);

		DependentInfo rtn = getDependentInfo(found);
		rtn.setSecretToken(null);
		return rtn;
	}

	public AccessInfo addAccessData( String cloudName, AccessRequestInfo data )
	{
		GuardianInfo  gInfo = data.getGuardian();
		DependentInfo dInfo = data.getDependent();
		AccessInfo    aInfo = data.getAccess();
		if( this.guardian == null )
		{
			throw new GuardianAuthenticationFailureException("Guardian authentication not performed");
		}
		String type = aInfo.getType();
		if( AccessInfo.isTypeValid(type) == false )
		{
			throw new AccessDataTypeInvalidException("Access data type invalid");
		}
		if( AccessInfo.isTypeAddable(type) == false )
		{
			throw new AccessDataOperationInvalidException("Access data operation invalid");
		}
		if( AccessInfo.isTypeAddableByDependent(type) == true )
		{
			gInfo = null;
			if( dInfo == null )
			{
				throw new DependentAuthenticationFailureException("Dependent authentication information required");
			}
		}
		else
		{
			dInfo = null;
			if( gInfo == null )
			{
				throw new GuardianAuthenticationFailureException("Guardian authentication information required");
			}
			if(    (this.guardian.getCloudName().equals(gInfo.getCloudName()) == false)
			    || (this.guardian.getSecretToken().equals(gInfo.getSecretToken()) == false) )
			{
				throw new GuardianAuthenticationFailureException("Guardian authentication failure");
			}
		}
		String url = aInfo.getUrl();
		String host = aInfo.getHost();
		if( StringUtils.isBlank(url) )
		{
			url = null;
		}
		if( StringUtils.isBlank(host) )
		{
			host = null;
		}
		if( (url == null) && (host == null) )
		{
			throw new AccessDataFieldInvalidException("Access data url and host missing");
		}
		if( (url != null) && (host != null) )
		{
			throw new AccessDataFieldInvalidException("Access data url and host mutually exclusive");
		}
		if( StringUtils.isBlank(cloudName) == true )
		{
			throw new CloudNameInvalidException("Dependent cloud name invalid");
		}
		DependentData found = null;
		for( DependentData dep : this.dependentList )
		{
			if( dep.getCloudName().equals(cloudName) )
			{
				found = dep;
				break;
			}
		}
		if( found == null )
		{
			throw new DependentCloudNameNotFoundException("cloudname is not a dependent");
		}
		if( found.getPort() == null )
		{
			throw new DependentProxyNotFoundException("Dependent info/proxy not started");
		}
		if( dInfo != null )
		{
			if(    (found.getCloudName().equals(dInfo.getCloudName()) == false)
			    || (found.getSecretToken().equals(dInfo.getSecretToken()) == false) )
			{
				throw new DependentAuthenticationFailureException("Dependent authentication failure");
			}
		}
		List<AccessData> list = null;
		if( AccessInfo.TYPE_ALLOWED.equals(type) )
		{
			list = found.getAllowed();
		}
		else if( AccessInfo.TYPE_BLOCKED.equals(type) )
		{
			list = found.getBlocked();
		}
		else if( AccessInfo.TYPE_REQUESTED.equals(type) )
		{
			list = found.getRequested();
		}
		else
		{
			throw new AccessDataOperationInvalidException("Access data operation invalid");
		}
		for( AccessData acc : list )
		{
			if(    ((url  != null) && url.equals(acc.getUrl()))
			    || ((host != null) && host.equalsIgnoreCase(acc.getHost())) )
			{
				AccessInfo rtn = getAccessInfo(acc);
				return rtn;
			}
		}
		AccessData acc = new AccessData();
		acc.setTimestamp(new Date());
		acc.setUrl(url);
		acc.setHost(host);

		ProxyXdiService.addAccessData(found, type, acc);
		acc.setType(type);
		list.add(acc);

		AccessInfo rtn = getAccessInfo(acc);
		return rtn;
	}

	public List<AccessInfo> getAccessData( String cloudName, String type )
	{
		if( this.guardian == null )
		{
			throw new GuardianAuthenticationFailureException("Guardian authentication not performed");
		}
		if( AccessInfo.isTypeValid(type) == false )
		{
			throw new AccessDataTypeInvalidException("Access data type invalid");
		}
		if( StringUtils.isBlank(cloudName) == true )
		{
			throw new CloudNameInvalidException("Dependent cloud name invalid");
		}
		DependentData found = null;
		for( DependentData dep : this.dependentList )
		{
			if( dep.getCloudName().equals(cloudName) )
			{
				found = dep;
				break;
			}
		}
		if( found == null )
		{
			throw new DependentCloudNameNotFoundException("cloudname is not a dependent");
		}
		if( found.getPort() == null )
		{
			throw new DependentProxyNotFoundException("Dependent info/proxy not started");
		}
		List<AccessData> list = null;
		if( AccessInfo.TYPE_ALLOWED.equals(type) )
		{
			list = found.getAllowed();
		}
		else if( AccessInfo.TYPE_BLOCKED.equals(type) )
		{
			list = found.getBlocked();
		}
		else if( AccessInfo.TYPE_REQUESTED.equals(type) )
		{
			list = found.getRequested();
		}
		else if( AccessInfo.TYPE_LOG.equals(type) )
		{
			list = found.getLog();
		}
		else
		{
			throw new AccessDataOperationInvalidException("Access data operation invalid");
		}
		List<AccessInfo> rtn = new ArrayList<AccessInfo>();
		for( AccessData acc : list )
		{
			AccessInfo info = getAccessInfo(acc);
			rtn.add(info);
		}
		return rtn;
	}

	public AccessInfo deleteAccessData( String cloudName, String type, String uuid, GuardianInfo data )
	{
		if( this.guardian == null )
		{
			throw new GuardianAuthenticationFailureException("Guardian authentication not performed");
		}
		if(    (this.guardian.getCloudName().equals(data.getCloudName()) == false)
		    || (this.guardian.getSecretToken().equals(data.getSecretToken()) == false) )
		{
			throw new GuardianAuthenticationFailureException("Guardian authentication failure");
		}
		if( AccessInfo.isTypeValid(type) == false )
		{
			throw new AccessDataTypeInvalidException("Access data type invalid");
		}
		if( StringUtils.isBlank(cloudName) == true )
		{
			throw new CloudNameInvalidException("Dependent cloud name invalid");
		}
		DependentData found = null;
		for( DependentData dep : this.dependentList )
		{
			if( dep.getCloudName().equals(cloudName) )
			{
				found = dep;
				break;
			}
		}
		if( found == null )
		{
			throw new DependentCloudNameNotFoundException("cloudname is not a dependent");
		}
		if( found.getPort() == null )
		{
			throw new DependentProxyNotFoundException("Dependent info/proxy not started");
		}
		List<AccessData> list = null;
		if( AccessInfo.TYPE_ALLOWED.equals(type) )
		{
			list = found.getAllowed();
		}
		else if( AccessInfo.TYPE_BLOCKED.equals(type) )
		{
			list = found.getBlocked();
		}
		else if( AccessInfo.TYPE_REQUESTED.equals(type) )
		{
			list = found.getRequested();
		}
		else if( AccessInfo.TYPE_LOG.equals(type) )
		{
			list = found.getLog();
		}
		else
		{
			throw new AccessDataOperationInvalidException("Access data operation invalid");
		}
		AccessData acc_found = null;
		for( AccessData acc : list )
		{
			if( acc.getUuid().equals(uuid) == true )
			{
				acc_found = acc;
				break;
			}
		}
		if( acc_found == null )
		{
			throw new AccessDataUuidNotFoundException("Access data uuid not found");
		}
		ProxyXdiService.deleteAccessData(found, type, uuid);
		if( AccessInfo.TYPE_ALLOWED.equals(type) )
		{
			found.removeAllowed(acc_found);
		}
		else if( AccessInfo.TYPE_BLOCKED.equals(type) )
		{
			found.removeBlocked(acc_found);
		}
		else if( AccessInfo.TYPE_REQUESTED.equals(type) )
		{
			found.removeRequested(acc_found);
		}
		else if( AccessInfo.TYPE_LOG.equals(type) )
		{
			found.removeLog(acc_found);
		}
		else
		{
			throw new AccessDataOperationInvalidException("Access data operation invalid");
		}
		AccessInfo rtn = getAccessInfo(acc_found);
		return rtn;
	}

	private synchronized Integer getPortNumber()
	{
		Integer rtn = null;
		int count = this.maxPortNumber - this.minPortNumber + 1;
		while( count-- > 0 )
		{
			int port = this.portNumber++;
			if( this.portNumber == this.maxPortNumber )
			{
				this.portNumber = this.minPortNumber;
			}
			boolean found = false;
			for( DependentData data : this.dependentList )
			{
				if( (data.getPort() != null) && (data.getPort().intValue() == port) )
				{
					found = true;
					break;
				}
			}
			if( found == false )
			{
				return new Integer(port);
			}
		}
		throw new ProxyInternalErrorException("Proxy port number exhausted");
	}
}
