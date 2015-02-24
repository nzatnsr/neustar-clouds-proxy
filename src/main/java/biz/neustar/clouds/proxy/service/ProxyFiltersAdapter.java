package biz.neustar.clouds.proxy.service;

import biz.neustar.clouds.proxy.*;
import biz.neustar.clouds.proxy.model.*;
import biz.neustar.clouds.proxy.service.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.net.MalformedURLException;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import io.netty.buffer.*;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.*;
import io.netty.util.CharsetUtil;

import org.apache.commons.beanutils.*;

import org.littleshoot.proxy.*;

public class ProxyFiltersAdapter extends HttpFiltersAdapter
{
	private static final Logger logger = LoggerFactory.getLogger(ProxyFiltersAdapter.class);

	private DependentData dependent;

	public ProxyFiltersAdapter( HttpRequest originalRequest )
	{
		super(originalRequest);
		logger.info("ProxyFiltersAdapter created - " + originalRequest);
	}

	public DependentData getDependent()
	{
		return this.dependent;
	}

	public void setDependent( DependentData dependent )
	{
		this.dependent = dependent;
	}

	private boolean matchHost( String domain, String host )
	{
		int    i;
		String s = host;
		if( domain.equalsIgnoreCase(s) == true )
		{
			logger.debug("matchHost(" + domain + ", " + host + ") = true");
			return true;
		}
		while( (i = s.indexOf('.')) >= 0 )
		{
			s = s.substring(i + 1);
			if( domain.equalsIgnoreCase(s) == true )
			{
				logger.debug("matchHost(" + domain + ", " + host + ") = true");
				return true;
			}
		}
		logger.debug("matchHost(" + domain + ", " + host + ") = false");
		return false;
	}

	@Override
	public HttpResponse clientToProxyRequest( HttpObject httpObject )
	{
		logger.info("ProxyFiltersAdapter.clientToProxyRequest - " + httpObject);
		if( ! (httpObject instanceof HttpRequest) )
		{
			return null;
		}

		this.dependent.getNumberOfRequests().getAndIncrement();
		HttpRequest req = (HttpRequest) httpObject;
		String uri = req.getUri();
		logger.info("ProxyFiltersAdapter.clientToProxyRequest URL - '" + req.getMethod() + "' - '" + uri + "'");

		URL url = null;
		try
		{
			if( HttpMethod.CONNECT.equals(req.getMethod()) == true )
			{
				if( uri.endsWith(":80") || uri.endsWith(":80/") )
				{
					if( uri.startsWith("http") == false )
					{
						uri = "http://" + uri;
					}
					uri = uri.substring(0, uri.lastIndexOf(':'));
				}
				else if( uri.endsWith(":443") || uri.endsWith(":443/") )
				{
					if( uri.startsWith("http") == false )
					{
						uri = "https://" + uri;
					}
					uri = uri.substring(0, uri.lastIndexOf(':'));
				}
				else
				{
					uri = "http://" + uri;
				}
			}
			url = new URL(uri);
		}
		catch( MalformedURLException e )
		{
			String err = "Invalid URL - " + uri;
			logger.error(err, e);
			FullHttpResponse rsp = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.BAD_REQUEST, Unpooled.copiedBuffer(err, CharsetUtil.UTF_8));
			rsp.headers().set("Content-Type", "text/plain; charset=UTF-8");
			return rsp;
		}

		AccessData data = new AccessData();
		data.setTimestamp(new Date());
		data.setUrl(uri);

		String host = url.getHost();
		String err = null;
		for( AccessData acc : this.dependent.getBlocked() )
		{
			if( (acc.getHost() != null) && (this.matchHost(acc.getHost(), host) == true) )
			{
				err = "Host " + host + " is blocked";
				break;
			}
			if( (acc.getUrl() != null) && (acc.getUrl().equals(uri) == true) )
			{
				err = "URL " + uri + " is blocked";
				break;
			}
		}
		if( err != null )
		{
			logger.info(err);
			data.setType(AccessInfo.TYPE_BLOCKED);
			ProxyXdiService.addAccessData(this.dependent, AccessInfo.TYPE_LOG, data);
			this.dependent.addLog(data);

			this.dependent.getNumberOfRequestsBlocked().getAndIncrement();
			FullHttpResponse rsp = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.FORBIDDEN, Unpooled.copiedBuffer(err, CharsetUtil.UTF_8));
			rsp.headers().set("Content-Type", "text/plain; charset=UTF-8");
			return rsp;
		}
		for( AccessData acc : this.dependent.getAllowed() )
		{
			if( (acc.getHost() != null) && (this.matchHost(acc.getHost(), host) == true) )
			{
				logger.info("Host " + host + " is allowed");
				data.setType(AccessInfo.TYPE_ALLOWED);
				ProxyXdiService.addAccessData(this.dependent, AccessInfo.TYPE_LOG, data);
				this.dependent.addLog(data);

				this.dependent.getNumberOfRequestsAllowed().getAndIncrement();
				return null;
			}
			if( (acc.getUrl() != null) && (acc.getUrl().equals(uri) == true) )
			{
				logger.info("URL " + uri + " is allowed");
				data.setType(AccessInfo.TYPE_ALLOWED);
				ProxyXdiService.addAccessData(this.dependent, AccessInfo.TYPE_LOG, data);
				this.dependent.addLog(data);

				this.dependent.getNumberOfRequestsAllowed().getAndIncrement();
				return null;
			}
		}
		err = "No blocked/allowed rule found for " + uri;
		logger.info(err);
		data.setType(AccessInfo.TYPE_BLOCKED);
		ProxyXdiService.addAccessData(this.dependent, AccessInfo.TYPE_LOG, data);
		this.dependent.addLog(data);

		this.dependent.getNumberOfRequestsBlocked().getAndIncrement();
		FullHttpResponse rsp = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.FORBIDDEN, Unpooled.copiedBuffer(err, CharsetUtil.UTF_8));
		rsp.headers().set("Content-Type", "text/plain; charset=UTF-8");
		return rsp;
	}

	@Override
	public HttpResponse proxyToServerRequest( HttpObject httpObject )
	{
		// TODO: implement your filtering here
		logger.info("ProxyFiltersAdapter.proxyToServerRequest - " + httpObject);
		return null;
	}

	@Override
	public HttpObject serverToProxyResponse( HttpObject httpObject )
	{
		// TODO: implement your filtering here
		logger.info("ProxyFiltersAdapter.serverToProxyResponse - " + httpObject);
		return httpObject;
	}

	@Override
	public HttpObject proxyToClientResponse( HttpObject httpObject )
	{
		// TODO: implement your filtering here
		logger.info("ProxyFiltersAdapter.proxyToClientResponse - " + httpObject);
		return httpObject;
	}
}
