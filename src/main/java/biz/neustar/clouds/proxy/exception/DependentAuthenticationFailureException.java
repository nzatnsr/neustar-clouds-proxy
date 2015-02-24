package biz.neustar.clouds.proxy.exception;

public class DependentAuthenticationFailureException extends RuntimeException
{
	public DependentAuthenticationFailureException( final String msg )
	{
		super(msg);
	}
}
