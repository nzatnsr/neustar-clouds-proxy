package biz.neustar.clouds.proxy.model;

import java.util.Date;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

public class GuardianInfo extends ProxyInfo
{
	@NotNull
	private String cloudName;

	@NotNull
	private String secretToken;

	public GuardianInfo()
	{
		super();
		init();
	}

	public GuardianInfo( Date timeStarted )
	{
		super(null, timeStarted);
		init();
	}

	private void init()
	{
		this.cloudName = null;
		this.secretToken = null;
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
}
