package biz.neustar.clouds.proxy.service;

import java.util.*;
import java.text.*;
import biz.neustar.clouds.proxy.model.*;

import org.littleshoot.proxy.*;
import org.littleshoot.proxy.impl.*;
 
import org.apache.commons.beanutils.*;

import java.net.URL;
import xdi2.core.xri3.CloudNumber;

import org.apache.commons.lang.StringUtils;

public class AccessData extends AccessInfo
{
	public AccessData()
	{
		super();
	}

	public AccessData( AccessInfo info )
	{
		try
		{
			BeanUtils.copyProperties(this, info);
		}
		catch( Exception e )
		{
		}
	}

	public static String fromAccessData( AccessData data )
	{
		SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
		String rtn = fmt.format(data.getTimestamp());
		if( data.getType() != null )
		{
			rtn = rtn + " type=" + data.getType();
		}
		if( data.getUrl() != null )
		{
			rtn = rtn + " url=" + data.getUrl();
		}
		if( data.getHost() != null )
		{
			rtn = rtn + " host=" + data.getHost();
		}
		return rtn;
	}

	public static AccessData toAccessData( String data, String uuid )
	{
		SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
		String[] tokens = data.split(" ");
		if( tokens.length < 2)
		{
			logger.error("Invalid access data - " + data);
			return null;
		}
		Date ts = null;
		try
		{
			ts = fmt.parse(tokens[0]);
		}
		catch( Exception e )
		{
			logger.error("Invalid access data timestamp - " + data);
			return null;
		}
		String type = null;
		if( tokens[1].startsWith("type=") )
		{
			type = tokens[1].substring(5);
			if( AccessInfo.isTypeValid(type) == false )
			{
				logger.error("Invalid access data type - " + data);
				return null;
			}
		}
		if( (type != null) && (tokens.length < 3) )
		{
			logger.error("Invalid access data - no host/url - " + data);
			return null;
		}
		int idx = 1;
		if( type != null )
		{
			idx = 2;
		}
		String str = tokens[idx++];
		while( idx < tokens.length )
		{
			str = str + " " + tokens[idx++];
		}
		String url  = null;
		String host = null;
		if( str.startsWith("url=") )
		{
			url = str.substring(4);
			if( StringUtils.isBlank(url) )
			{
				logger.error("Invalid access data - url empty - " + data);
				return null;
			}
		}
		else if( str.startsWith("host=") )
		{
			host = str.substring(5);
			if( StringUtils.isBlank(host) )
			{
				logger.error("Invalid access data - host empty - " + data);
				return null;
			}
		}
		else
		{
			logger.error("Invalid access data - host/url not found - " + data);
			return null;
		}
		AccessData rtn = new AccessData();

		rtn.setUuid(uuid);
		rtn.setType(type);
		rtn.setTimestamp(ts);
		rtn.setUrl(url);
		rtn.setHost(host);

		return rtn;
	}
}
