package biz.neustar.clouds.proxy;

import java.io.IOException;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Component;

@Component
public class ProxyHttpFilter implements Filter
{
	public void doFilter( ServletRequest req, ServletResponse res, FilterChain chain ) throws IOException, ServletException
	{
		HttpServletResponse rsp = (HttpServletResponse) res;
		rsp.setHeader("Access-Control-Allow-Origin", "*");
		rsp.setHeader("Access-Control-Allow-Methods", "POST, GET, PUT, DELETE, OPTIONS");
		rsp.setHeader("Access-Control-Max-Age", "3600");
		rsp.setHeader("Access-Control-Allow-Headers", ((HttpServletRequest) req).getHeader("Access-Control-Request-Headers"));
		chain.doFilter(req, rsp);
	}

	public void init( FilterConfig filterConfig )
	{
		// do nothing
	}

	public void destroy()
	{
		// do nothing
	}

}
