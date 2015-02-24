package biz.neustar.clouds.proxy.service;

import java.util.Date;
import biz.neustar.clouds.proxy.model.*;

import org.littleshoot.proxy.*;
import org.littleshoot.proxy.impl.*;
 
import org.apache.commons.beanutils.*;

public class ProxyData extends ProxyInfo
{
	HttpProxyServer server;
	ProxyFilter     filter;

	public ProxyData()
	{
		super();
	}

	public ProxyData( ProxyInfo info )
	{
		try
		{
			BeanUtils.copyProperties(this, info);
		}
		catch( Exception e )
		{
		}
	}

	public ProxyData( Long id, Date timeStarted )
	{
		super(id, timeStarted);
	}


	public void setFilter( ProxyFilter filter )
	{
		this.filter = filter;
	}

	public ProxyFilter getFilter()
	{
		return this.filter;
	}

	public void setServer( HttpProxyServer server )
	{
		this.server = server;
	}

	public HttpProxyServer getServer()
	{
		return this.server;
	}
}
