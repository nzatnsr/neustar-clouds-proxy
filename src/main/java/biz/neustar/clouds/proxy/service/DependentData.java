package biz.neustar.clouds.proxy.service;


import java.util.*;
import biz.neustar.clouds.proxy.model.*;

import org.littleshoot.proxy.*;
import org.littleshoot.proxy.impl.*;
 
import org.apache.commons.beanutils.*;

import java.net.URL;
import xdi2.core.xri3.CloudNumber;

public class DependentData extends DependentInfo
{
	private CloudNumber           cloudNumber;
	private URL                   cloudUrl;
	private List<AccessData>      allowed;
	private List<AccessData>      blocked;
	private List<AccessData>      requested;
	private List<AccessData>      log;
	private HttpProxyServer       server;

	public DependentData()
	{
		super();
	}

	public DependentData( DependentInfo info )
	{
		try
		{
			BeanUtils.copyProperties(this, info);
		}
		catch( Exception e )
		{
		}
	}

	public HttpProxyServer getServer()
	{
		return this.server;
	}

	public void setServer( HttpProxyServer server )
	{
		this.server = server;
	}

	public CloudNumber getCloudNumber()
	{
		return this.cloudNumber;
	}

	public void setCloudNumber( CloudNumber cloudNumber )
	{
		this.cloudNumber = cloudNumber;
	}

	public URL getCloudUrl()
	{
		return this.cloudUrl;
	}

	public void setCloudUrl( URL cloudUrl )
	{
		this.cloudUrl = cloudUrl;
	}

	public List<AccessData> getAllowed()
	{
		return this.allowed;
	}

	public void setAllowed( List<AccessData> allowed )
	{
		this.allowed = allowed;
	}

	public void addAllowed( AccessData allowed )
	{
		this.allowed.add(allowed);
	}

	public void removeAllowed( AccessData allowed )
	{
		this.allowed = this.removeFromList(this.allowed, allowed);
	}

	public List<AccessData> getBlocked()
	{
		return this.blocked;
	}

	public void setBlocked( List<AccessData> blocked )
	{
		this.blocked = blocked;
	}

	public void addBlocked( AccessData blocked )
	{
		this.blocked.add(blocked);
	}

	public void removeBlocked( AccessData blocked )
	{
		this.blocked = this.removeFromList(this.blocked, blocked);
	}

	public List<AccessData> getRequested()
	{
		return this.requested;
	}

	public void setRequested( List<AccessData> requested )
	{
		this.requested = requested;
	}

	public void addRequested( AccessData requested )
	{
		this.requested.add(requested);
	}

	public void removeRequested( AccessData requested )
	{
		this.requested = this.removeFromList(this.requested, requested);
	}

	public List<AccessData> getLog()
	{
		return this.log;
	}

	public void setLog( List<AccessData> log )
	{
		this.log = log;
	}

	public void addLog( AccessData log )
	{
		this.log.add(log);
	}

	public void removeLog( AccessData log )
	{
		this.log = this.removeFromList(this.log, log);
	}

	private List<AccessData> removeFromList( List<AccessData> list, AccessData data )
	{
		List<AccessData> rtn = new ArrayList<AccessData>();
		for( AccessData obj : list )
		{
			if( data.getUuid().equalsIgnoreCase(obj.getUuid()) == false )
			{
				rtn.add(obj);
			}
		}
		list.clear();	
		return rtn;
	}
}
