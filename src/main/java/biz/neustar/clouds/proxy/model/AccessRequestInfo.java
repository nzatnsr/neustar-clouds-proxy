package biz.neustar.clouds.proxy.model;

import java.util.Date;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

public class AccessRequestInfo extends ProxyInfo
{
	private GuardianInfo guardian;

	private DependentInfo dependent;

	@NotNull
	private AccessInfo   access;

	public AccessRequestInfo()
	{
		super();
	}

	public GuardianInfo getGuardian()
	{
		return this.guardian;
	}

	public void setGuardian( GuardianInfo guardian )
	{
		this.guardian = guardian;
	}

	public DependentInfo getDependent()
	{
		return this.dependent;
	}

	public void setDependent( DependentInfo dependent )
	{
		this.dependent = dependent;
	}

	public AccessInfo getAccess()
	{
		return this.access;
	}

	public void setAccess( AccessInfo access )
	{
		this.access = access;
	}
}
