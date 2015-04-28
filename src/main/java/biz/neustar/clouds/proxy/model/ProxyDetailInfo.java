package biz.neustar.clouds.proxy.model;

import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import biz.neustar.clouds.proxy.*;

public class ProxyDetailInfo extends ProxyInfo
{
	private List<GuardianInfo> guardian;

	public ProxyDetailInfo()
	{
		super();
		init();
	}

	public ProxyDetailInfo( Long id, Date timeStarted )
	{
		super(id, timeStarted);
		init();
	}

	private void init()
	{
		this.guardian = null;
	}

	public List<GuardianInfo> getGuardian()
	{
		return this.guardian;
	}

	public void setGuardian( List<GuardianInfo> guardian )
	{
		this.guardian = guardian;
	}
}
