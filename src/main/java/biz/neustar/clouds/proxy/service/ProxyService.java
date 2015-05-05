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

	private List<GuardianData> guardianList;

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
		this.guardianList = new ArrayList<GuardianData>();
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
			logger.error(error + " - " + data, e);
			throw new ProxyInternalErrorException(error);
		}

		return rtn;
	}

	private GuardianData getGuardianData( DependentInfo info )
	{
		GuardianData rtn = null;
		for( GuardianData data : this.guardianList )
		{
			List<DependentData> list = data.getDependent();
			for( DependentData dep : list )
			{
				if( dep.getCloudName().equals(info.getCloudName()) )
				{
					rtn = data;
					break;
				}
			}
		}
		if( rtn == null )
		{
			throw new GuardianAuthenticationFailureException("Guardian authentication not performed");
		}
		return rtn;
	}

	private GuardianData getGuardianData( GuardianInfo info )
	{
		return this.getGuardianData(info, false);
	}

	private GuardianData getGuardianData( GuardianInfo info, boolean flag )
	{
		GuardianData rtn = null;
		for( GuardianData data : this.guardianList )
		{
			if( data.getCloudName().equals(info.getCloudName()) )
			{
				rtn = data;
				break;
			}
		}
		if( flag == true )
		{
			return rtn;
		}
		if( rtn == null )
		{
			throw new GuardianAuthenticationFailureException("Guardian authentication not performed");
		}
		if(    (rtn.getCloudName().equals(info.getCloudName()) == false)
		    || (rtn.getSecretToken().equals(info.getSecretToken()) == false) )
		{
			throw new GuardianAuthenticationFailureException("Guardian authentication failed");
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
			logger.error(error + " - " + data, e);
			throw new ProxyInternalErrorException(error);
		}

		return rtn;
	}

	public GuardianInfo getGuardian( GuardianInfo info )
	{
		GuardianData data = this.getGuardianData(info);
		GuardianInfo rtn  = this.getGuardianInfo(data);
		rtn.setSecretToken(null);
		return rtn;
	}

	public synchronized GuardianInfo setGuardian( GuardianInfo info )
	{
		GuardianInfo rtn  = null;
		GuardianData data = this.getGuardianData(info, true);
		if( data != null )
		{
			if(    (data.getCloudName().equals(info.getCloudName()) == false)
			    || (data.getSecretToken().equals(info.getSecretToken()) == false) )
			{
				throw new GuardianAuthenticationCompletedException("Guardian re-authentication failure");
			}
			rtn = this.getGuardianInfo(data);
			rtn.setSecretToken(null);
			return rtn;
		}
		data = ProxyXdiService.verifyGuardian(info);
		if( data == null )
		{
			throw new GuardianAuthenticationFailureException("Guardian authentication failure");
		}
		data.setTimeStarted(new Date());
		List<DependentData> list = ProxyXdiService.getDependent(data);
		if( list != null )
		{
			data.setDependent(list);
		}
		this.guardianList.add(data);

		rtn = getGuardianInfo(data);
		rtn.setSecretToken(null);

		logger.info("Guardian " + rtn + " added");

		return rtn;
	}

	public GuardianInfo deleteGuardian( GuardianInfo info )
	{
		GuardianData data = this.getGuardianData(info);
		List<DependentData> list = data.getDependent();
		if( list != null )
		{
			for( DependentData dep : list )
			{
				if( dep.getPort() != null )
				{
					throw new DependentProxyNotTerminatedException("Dependent proxy not terminated");
				}
			}
		}
		GuardianInfo rtn = this.getGuardianInfo(data);
		rtn.setSecretToken(null);

		this.guardianList.remove(data);
		data.getDependent().clear();

		logger.info("Guardian " + rtn + " added");

		return rtn;
	}

	public List<DependentInfo> listDependentProxy( GuardianInfo info )
	{
		logger.info("listDependentProxy");

		GuardianData data = this.getGuardianData(info);
		List<DependentInfo> rtn = new ArrayList<DependentInfo>();
		List<DependentData> list = data.getDependent();
		for( DependentData dep : list )
		{
			DependentInfo dinfo = getDependentInfo(dep);
			dinfo.setSecretToken(null);
			rtn.add(dinfo);
		}
		return rtn;
	}

	public DependentInfo getDependentProxy( String cloudName, GuardianInfo info )
	{
		logger.info("getDependentProxy - " + cloudName);

		if( StringUtils.isBlank(cloudName) == true )
		{
			throw new CloudNameInvalidException("Dependent cloud name invalid");
		}
		GuardianData data = this.getGuardianData(info);
		List<DependentData> list = data.getDependent();
		for( DependentData dep : list )
		{
			if( cloudName.equals(dep.getCloudName()) == true )
			{
				DependentInfo rtn = this.getDependentInfo(dep);
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
			logger.error(error + " - " + data, e);
			throw new ProxyInternalErrorException(error);
		}
		return rtn;
	}

	private DependentData getDependentData( GuardianData guardian, DependentInfo info )
	{
		return this.getDependentData(guardian, info.getCloudName());
	}

	private DependentData getDependentData( GuardianData guardian, String cloudName )
	{
		DependentData rtn = null;
		if( guardian == null )
		{
			throw new GuardianAuthenticationFailureException("Guardian authentication not performed");
		}
		List<DependentData> list = guardian.getDependent();
		if( list != null )
		{
			for( DependentData dep : list )
			{
				if( dep.getCloudName().equals(cloudName) )
				{
					rtn = dep;
					break;
				}
			}
		}
		if( rtn == null )
		{
			throw new DependentCloudNameNotFoundException("cloudname is not a dependent");
		}
		return rtn;
	}

	public synchronized DependentInfo startDependentProxy( DependentInfo info )
	{
		logger.info("startDependentProxy - " + info);

		GuardianData guardian = this.getGuardianData(info);
		DependentData found = this.getDependentData(guardian, info);
		if( found.getPort() != null )
		{
			DependentInfo rtn = this.getDependentInfo(found);
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

		GuardianData guardian = this.getGuardianData(data);
		DependentData found = this.getDependentData(guardian, data);
		if( found.getPort() == null )
		{
			DependentInfo rtn = this.getDependentInfo(found);
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
		GuardianData  guardian = this.getGuardianData(gInfo);

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
			if(    (guardian.getCloudName().equals(gInfo.getCloudName()) == false)
			    || (guardian.getSecretToken().equals(gInfo.getSecretToken()) == false) )
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
		DependentData found = this.getDependentData(guardian, cloudName);
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

	public List<AccessInfo> getAccessData( String cloudName, String type, GuardianInfo info )
	{
		GuardianData guardian = this.getGuardianData(info);
		if( AccessInfo.isTypeValid(type) == false )
		{
			throw new AccessDataTypeInvalidException("Access data type invalid");
		}
		if( StringUtils.isBlank(cloudName) == true )
		{
			throw new CloudNameInvalidException("Dependent cloud name invalid");
		}
		DependentData found = this.getDependentData(guardian, cloudName);
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
			AccessInfo ainfo = this.getAccessInfo(acc);
			rtn.add(ainfo);
		}
		return rtn;
	}

	public AccessInfo deleteAccessData( String cloudName, String type, String uuid, GuardianInfo info )
	{
		GuardianData guardian = this.getGuardianData(info);
		if( AccessInfo.isTypeValid(type) == false )
		{
			throw new AccessDataTypeInvalidException("Access data type invalid");
		}
		if( StringUtils.isBlank(cloudName) == true )
		{
			throw new CloudNameInvalidException("Dependent cloud name invalid");
		}
		DependentData found = this.getDependentData(guardian, cloudName);
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
			for( GuardianData guardian : this.guardianList )
			{
				List<DependentData> list = guardian.getDependent();
				if( list != null )
				{
					for( DependentData data : list )
					{
						if( (data.getPort() != null) && (data.getPort().intValue() == port) )
						{
							found = true;
							break;
						}
					}
				}
			}
			if( found == false )
			{
				return new Integer(port);
			}
		}
		throw new ProxyInternalErrorException("Proxy port number exhausted");
	}

	private boolean authenticateAdmin( AdminUserInfo data )
	{
		if( ProxyApplication.getConfig().getAdminUsername().equals(data.getUsername()) == false )
		{
			return false;
		}
		try
		{
			String digest = ProxyUtil.getDigest(ProxyApplication.getConfig().getAdminSalt(), data.getUsername(), data.getPassword());
			if( ProxyApplication.getConfig().getAdminPassword().equals(digest) == false )
			{
				return false;
			}
		}
		catch( java.io.UnsupportedEncodingException ex )
		{
			logger.error("getDigest() failed", ex);
			return false;
		}
		return true;
	}

	public ProxyDetailInfo getProxyDetailInfo( AdminUserInfo data )
	{
		if( this.authenticateAdmin(data) == false )
		{
			throw new AdminUserAuthenticationFailureException("Admin user authentication failure");
		}
		ProxyDetailInfo rtn = new ProxyDetailInfo();
		List<GuardianInfo> list = new ArrayList<GuardianInfo>();
		for( GuardianData guardian : this.guardianList )
		{
			GuardianInfo info = this.getGuardianInfo(guardian);
			list.add(info);
		}
		rtn.setGuardian(list);
		return rtn;
	}
}
