package biz.neustar.clouds.proxy.exception;

public class DependentProxyNotFoundException extends RuntimeException
{
	public DependentProxyNotFoundException( final String msg )
	{
		super(msg);
	}
}
