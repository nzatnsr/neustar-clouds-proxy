package biz.neustar.clouds.proxy.exception;

public class ProxyInternalErrorException extends RuntimeException
{
	public ProxyInternalErrorException( final String msg )
	{
		super(msg);
	}
}
