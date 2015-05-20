package biz.neustar.clouds.proxy.service;

import java.util.*;
import biz.neustar.clouds.proxy.model.*;

import org.littleshoot.proxy.*;
import org.littleshoot.proxy.impl.*;
 
import org.apache.commons.beanutils.*;

import java.net.URL;
import xdi2.core.xri3.CloudNumber;

public class GuardianData extends GuardianInfo
{
	private CloudNumber cloudNumber;
	private URL         cloudUrl;
	private List<DependentData> dependent = new ArrayList<DependentData>();

	public GuardianData()
	{
		super();
	}

	public GuardianData( GuardianInfo info )
	{
		try
		{
			BeanUtils.copyProperties(this, info);
		}
		catch( Exception e )
		{
		}
	}

	public GuardianData( Date timeStarted )
	{
		super(timeStarted);
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

	public void setDependent( List<DependentData> list )
	{
		this.dependent.clear();
		if( list != null )
		{
			this.dependent.addAll(list);
		}
	}

	public List<DependentData> getDependent()
	{
		return this.dependent;
	}

	public void addDependent( DependentData dependent )
	{
		this.dependent.add(dependent);
	}
}
