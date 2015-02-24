package biz.neustar.clouds.proxy.model;

import java.util.Date;
import java.util.concurrent.atomic.AtomicInteger;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import biz.neustar.clouds.proxy.*;

public class DependentInfo extends ProxyInfo
{
	@NotNull
	private String cloudName;

	@NotNull
	private String secretToken;

	private String  hostname;
	private Integer port;
	private AtomicInteger numberOfRequests;
	private AtomicInteger numberOfRequestsBlocked;
	private AtomicInteger numberOfRequestsAllowed;

	public DependentInfo()
	{
		super();
		init();
	}

	public DependentInfo( Long id, Date timeStarted )
	{
		super(id, timeStarted);
		init();
	}

	private void init()
	{
		this.hostname = null;
		this.port = null;
		this.cloudName = null;
		this.secretToken = null;
		this.numberOfRequests = null;
		this.numberOfRequestsBlocked = null;
		this.numberOfRequestsAllowed = null;
	}

	public Integer getPort()
	{
		return this.port;
	}

	public void setPort( Integer port )
	{
		this.port = port;
	}

	public String getHostname()
	{
		return this.hostname;
	}

	public void setHostname( String hostname )
	{
		this.hostname = hostname;
	}

	public String getCloudName()
	{
		return this.cloudName;
	}

	public void setCloudName( String cloudName )
	{
		this.cloudName = cloudName;
	}

	public String getSecretToken()
	{
		return this.secretToken;
	}

	public void setSecretToken( String secretToken )
	{
		this.secretToken = secretToken;
	}

	public AtomicInteger getNumberOfRequests()
	{
		return this.numberOfRequests;
	}

	public void setNumberOfRequests( AtomicInteger numberOfRequests )
	{
		this.numberOfRequests = numberOfRequests;
	}

	public AtomicInteger getNumberOfRequestsBlocked()
	{
		return this.numberOfRequestsBlocked;
	}

	public void setNumberOfRequestsBlocked( AtomicInteger numberOfRequestsBlocked )
	{
		this.numberOfRequestsBlocked = numberOfRequestsBlocked;
	}

	public AtomicInteger getNumberOfRequestsAllowed()
	{
		return this.numberOfRequestsAllowed;
	}

	public void setNumberOfRequestsAllowed( AtomicInteger numberOfRequestsAllowed )
	{
		this.numberOfRequestsAllowed = numberOfRequestsAllowed;
	}
}
