package biz.neustar.clouds.proxy.model;

import java.util.Date;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

public class AccessInfo extends ProxyBaseObject
{
	public static final String TYPE_ALLOWED   = "allowed";
	public static final String TYPE_BLOCKED   = "blocked";
	public static final String TYPE_REQUESTED = "requested";
	public static final String TYPE_LOG       = "log";

	public static boolean isTypeAddableByDependent( String type )
	{
		if( TYPE_REQUESTED.equals(type) )
		{
			return true;
		}
		return false;
	}

	public static boolean isTypeAddable( String type )
	{
		if(    TYPE_ALLOWED.equals(type)
		    || TYPE_BLOCKED.equals(type)
		    || TYPE_REQUESTED.equals(type) )
		{
			return true;
		}
		return false;
	}

	public static boolean isTypeValid( String type )
	{
		if(    TYPE_ALLOWED.equals(type)
		    || TYPE_BLOCKED.equals(type)
		    || TYPE_REQUESTED.equals(type)
		    || TYPE_LOG.equals(type) )
		{
			return true;
		}
		return false;
	}

	@NotNull
	private String type;
	private String uuid;
	private Date   timestamp;
	private String url;
	private String host;

	public AccessInfo()
	{
		super();
		this.type      = null;
		this.uuid      = null;
		this.timestamp = null;
		this.url       = null;
		this.host      = null;
	}

	public String getType()
	{
		return this.type;
	}

	public void setType( String type )
	{
		this.type = type;
	}

	public String getUuid()
	{
		return this.uuid;
	}

	public void setUuid( String uuid )
	{
		this.uuid = uuid;
	}

	public Date getTimestamp()
	{
		return this.timestamp;
	}

	public void setTimestamp( Date timestamp )
	{
		this.timestamp = timestamp;
	}

	public String getUrl()
	{
		return this.url;
	}

	public void setUrl( String url )
	{
		this.url = url;
	}

	public String getHost()
	{
		return this.host;
	}

	public void setHost( String host )
	{
		this.host = host;
	}
}
