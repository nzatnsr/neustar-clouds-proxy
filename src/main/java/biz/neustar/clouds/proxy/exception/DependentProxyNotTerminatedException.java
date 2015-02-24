package biz.neustar.clouds.proxy.exception;

public class DependentProxyNotTerminatedException extends RuntimeException
{
	public DependentProxyNotTerminatedException( final String msg )
	{
		super(msg);
	}
}
