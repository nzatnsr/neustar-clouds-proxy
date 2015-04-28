package biz.neustar.clouds.proxy.model;

import java.util.Date;
import java.util.concurrent.atomic.AtomicInteger;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import biz.neustar.clouds.proxy.*;

public class AdminUserInfo extends ProxyInfo
{
	@NotNull
	private String username;

	@NotNull
	private String password;

	public AdminUserInfo()
	{
		super();
		init();
	}

	public AdminUserInfo( Long id, Date timeStarted )
	{
		super(id, timeStarted);
		init();
	}

	private void init()
	{
		this.username = null;
		this.password = null;
	}

	public String getUsername()
	{
		return this.username;
	}

	public void setUsername( String username )
	{
		this.username = username;
	}

	public String getPassword()
	{
		return this.password;
	}

	public void setPassword( String password )
	{
		this.password = password;
	}
}
