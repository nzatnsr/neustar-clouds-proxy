package biz.neustar.clouds.proxy;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Component;

import org.springframework.beans.factory.annotation.Value;

@Component
public class ProxyConfig
{
	public static final String XDI_ENV_OTE  = "ote";
	public static final String XDI_ENV_PROD = "prod";

	@Value("${proxy.server.hostname}")
	private String hostname;

	@Value("${proxy.port.min}")
	private Integer minPortNumber;

	@Value("${proxy.port.max}")
	private Integer maxPortNumber;

	@Value("${proxy.xdi.env}")
	private String xdiEnv;


	public ProxyConfig()
	{
	}

	public String getHostname()
	{
		return this.hostname;
	}

	public void setHostname( String hostname )
	{
		this.hostname = hostname;
	}

	public Integer getMinPortNumber()
	{
		return this.minPortNumber;
	}

	public void setMinPortNumber( Integer minPortNumber )
	{
		this.minPortNumber = minPortNumber;
	}

	public Integer getMaxPortNumber()
	{
		return this.maxPortNumber;
	}

	public void setMaxPortNumber( Integer maxPortNumber )
	{
		this.maxPortNumber = maxPortNumber;
	}

	public String getXdiEnv()
	{
		return this.xdiEnv;
	}

	public void setXdiEnv( String xdiEnv )
	{
		this.xdiEnv = xdiEnv;
	}
}
