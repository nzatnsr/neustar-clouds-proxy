package biz.neustar.clouds.proxy.exception;

public class AdminUserAuthenticationFailureException extends RuntimeException
{
	public AdminUserAuthenticationFailureException( final String msg )
	{
		super(msg);
	}
}
