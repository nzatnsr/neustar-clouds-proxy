package biz.neustar.clouds.proxy.service;

import biz.neustar.clouds.proxy.*;
import biz.neustar.clouds.proxy.model.*;
import biz.neustar.clouds.proxy.service.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import io.netty.handler.codec.http.*;
import io.netty.channel.ChannelHandlerContext;


import org.apache.commons.beanutils.*;

import org.littleshoot.proxy.*;

public class ProxyFilter extends HttpFiltersSourceAdapter
{
	private static final Logger logger = LoggerFactory.getLogger(ProxyFilter.class);

	private DependentData dependent;

	public ProxyFilter( DependentData dependent )
	{
		logger.info("ProxyFilter created - " + dependent);
		this.dependent = dependent;
	}

	public HttpFilters filterRequest( HttpRequest originalRequest )
	{
		logger.info("ProxyFilter filterRequest() - " + originalRequest);
		ProxyFiltersAdapter rtn = new ProxyFiltersAdapter(originalRequest);
		rtn.setDependent(this.dependent);
		return rtn;
	}
}
