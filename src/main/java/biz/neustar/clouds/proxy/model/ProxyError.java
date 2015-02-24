package biz.neustar.clouds.proxy.model;

public class ProxyError extends ProxyBaseObject
{
	private String error;

	public ProxyError( String error )
	{
		this.error = error;
	}

	public String getError()
	{
		return this.error;
	}

	public void setError( String error )
	{
		this.error = error;
	}
}
