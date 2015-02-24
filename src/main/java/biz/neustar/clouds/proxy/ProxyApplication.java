package biz.neustar.clouds.proxy;

import java.util.Date;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.context.annotation.ComponentScan;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
//@ComponentScan
@ComponentScan(basePackages="biz.neustar.clouds.proxy" )
@EnableAutoConfiguration
public class ProxyApplication
{
	private static ProxyApplication singleton;

	private Date timeStarted;

	@Autowired
	private ProxyConfig proxyConfig;

	public ProxyApplication()
	{
		this.timeStarted = new Date();
		singleton = this;
	}

	public Date getTimeStarted()
	{
		return this.timeStarted;
	}

	public void setTimeStarted( Date timeStarted )
	{
		this.timeStarted = timeStarted;
	}

	public ProxyConfig getProxyConfig()
	{
		return this.proxyConfig;
	}

	public void setProxyConfig( ProxyConfig proxyConfig )
	{
		this.proxyConfig = proxyConfig;
	}

	public static ProxyConfig getConfig()
	{
		return singleton.getProxyConfig();
	}

	public static Date getTimestamp()
	{
		return singleton.getTimeStarted();
	}

	public static void main(String[] args)
	{
		SpringApplication.run(ProxyApplication.class, args);
	}
}
