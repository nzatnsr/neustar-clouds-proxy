package biz.neustar.clouds.proxy.model;

import java.util.Date;

public class ProxyInfo extends ProxyBaseObject
{
	private Long id;
	private Date timeStarted;

	public ProxyInfo()
	{
		this.id = null;
		this.timeStarted = null;
	}

	public ProxyInfo( Long id, Date timeStarted )
	{
		this.id = id;
		this.timeStarted = timeStarted;
	}

	public Long getId()
	{
		return this.id;
	}

	public void setId( Long id )
	{
		this.id = id;
	}

	public Date getTimeStarted()
	{
		return this.timeStarted;
	}

	public void setTimeStarted( Date timeStarted )
	{
		this.timeStarted = timeStarted;
	}
}
