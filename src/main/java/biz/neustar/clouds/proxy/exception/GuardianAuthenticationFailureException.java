package biz.neustar.clouds.proxy.exception;

public class GuardianAuthenticationFailureException extends RuntimeException
{
	public GuardianAuthenticationFailureException( final String msg )
	{
		super(msg);
	}
}
