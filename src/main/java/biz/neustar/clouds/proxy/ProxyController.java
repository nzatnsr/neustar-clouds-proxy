package biz.neustar.clouds.proxy;

import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import javax.validation.Valid;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.*;

import org.springframework.context.annotation.*;
import org.springframework.http.converter.*;
import org.springframework.http.converter.json.*;
import org.springframework.http.HttpStatus;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;

import biz.neustar.clouds.proxy.model.*;
import biz.neustar.clouds.proxy.service.*;
import biz.neustar.clouds.proxy.exception.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
public class ProxyController
{
	private static final Logger logger = LoggerFactory.getLogger(ProxyController.class);

	private final AtomicLong counter = new AtomicLong();

	@Bean
	public MappingJackson2HttpMessageConverter mappingJackson2HttpMessageConverter()
	{
		MappingJackson2HttpMessageConverter jsonConverter = new MappingJackson2HttpMessageConverter();

		ObjectMapper objectMapper = new ObjectMapper();
		objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		objectMapper.configure(SerializationFeature.INDENT_OUTPUT, true);
		objectMapper.setPropertyNamingStrategy(PropertyNamingStrategy.CAMEL_CASE_TO_LOWER_CASE_WITH_UNDERSCORES);
		objectMapper.setDateFormat(new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ"));
		objectMapper.setSerializationInclusion(com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL);

		jsonConverter.setObjectMapper(objectMapper);
		return jsonConverter;
	}

	@ExceptionHandler
	@ResponseStatus(HttpStatus.UNAUTHORIZED)
	public ProxyError handleGuardianAuthenticationFailureException( GuardianAuthenticationFailureException e )
	{
		return new ProxyError(e.getMessage());
	}

	@ExceptionHandler
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public ProxyError handleGuardianAuthenticationCompletedException( GuardianAuthenticationCompletedException e )
	{
		return new ProxyError(e.getMessage());
	}

	@ExceptionHandler
	@ResponseStatus(HttpStatus.UNAUTHORIZED)
	public ProxyError handleDependentAuthenticationFailureException( DependentAuthenticationFailureException e )
	{
		return new ProxyError(e.getMessage());
	}

	@ExceptionHandler
	@ResponseStatus(HttpStatus.NOT_FOUND)
	public ProxyError handleDependentProxyNotFoundException( DependentProxyNotFoundException e )
	{
		return new ProxyError(e.getMessage());
	}

	@ExceptionHandler
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public ProxyError handleDependentProxyNotTerminatedException( DependentProxyNotTerminatedException e )
	{
		return new ProxyError(e.getMessage());
	}

	@ExceptionHandler
	@ResponseStatus(HttpStatus.NOT_FOUND)
	public ProxyError handleDependentCloudNameNotFoundException( DependentCloudNameNotFoundException e )
	{
		return new ProxyError(e.getMessage());
	}

	@ExceptionHandler
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public ProxyError handleCloudNameInvalidException( CloudNameInvalidException e )
	{
		return new ProxyError(e.getMessage());
	}

	@ExceptionHandler
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public ProxyError handleAccessDataTypeInvalidException( AccessDataTypeInvalidException e )
	{
		return new ProxyError(e.getMessage());
	}

	@ExceptionHandler
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public ProxyError handleAccessDataOperationInvalidException( AccessDataOperationInvalidException e )
	{
		return new ProxyError(e.getMessage());
	}

	@ExceptionHandler
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public ProxyError handleAccessDataFieldInvalidException( AccessDataFieldInvalidException e )
	{
		return new ProxyError(e.getMessage());
	}

	@ExceptionHandler
	@ResponseStatus(HttpStatus.NOT_FOUND)
	public ProxyError handleAccessDataUuidNotFoundException( AccessDataUuidNotFoundException e )
	{
		return new ProxyError(e.getMessage());
	}

	@ExceptionHandler
	@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
	public ProxyError handleProxyInternalErrorException( ProxyInternalErrorException e )
	{
		return new ProxyError(e.getMessage());
	}

	@RequestMapping(value = "/proxies/status", method = RequestMethod.GET)
	public @ResponseBody ProxyInfo getProxyInfo()
	{
		ProxyInfo rtn = new ProxyInfo(counter.incrementAndGet(), ProxyApplication.getTimestamp());
		return rtn;
	}

	@RequestMapping(value = "/proxies/guardian", method=RequestMethod.GET)
	public @ResponseBody GuardianInfo getGuardian()
	{
		GuardianInfo rtn = ProxyService.getInstance().getGuardian();
		return rtn;
	}

	@RequestMapping(value = "/proxies/guardian", method=RequestMethod.POST)
	public @ResponseBody GuardianInfo setGuardian( @RequestBody @Valid final GuardianInfo data )
	{
		GuardianInfo rtn = ProxyService.getInstance().setGuardian(data);
		return rtn;
	}

	@RequestMapping(value = "/proxies/guardian", method=RequestMethod.DELETE)
	public @ResponseBody GuardianInfo deleteGuardian( @RequestBody @Valid final GuardianInfo data )
	{
		GuardianInfo rtn = ProxyService.getInstance().deleteGuardian(data);
		return rtn;
	}

	@RequestMapping(value = "/proxies/dependents", method=RequestMethod.POST)
	public @ResponseBody DependentInfo startDependentProxy( @RequestBody @Valid final DependentInfo data )
	{
		DependentInfo rtn = ProxyService.getInstance().startDependentProxy(data);
		return rtn;
	}

	@RequestMapping(value = "/proxies/dependents", method=RequestMethod.GET)
	public @ResponseBody List<DependentInfo> listDependentProxy()
	{
		List<DependentInfo> rtn = ProxyService.getInstance().listDependentProxy();
		return rtn;
	}

	@RequestMapping(value = "/proxies/dependents/{cloudName}", method=RequestMethod.GET)
	public @ResponseBody DependentInfo getDependentProxy( @PathVariable("cloudName") final String cloudName )
	{
		DependentInfo rtn = ProxyService.getInstance().getDependentProxy(cloudName);
		return rtn;
	}

	@RequestMapping(value = "/proxies/dependents", method=RequestMethod.DELETE)
	public @ResponseBody DependentInfo stopDependentProxy( @RequestBody @Valid final DependentInfo data )
	{
		DependentInfo rtn = ProxyService.getInstance().stopDependentProxy(data);
		return rtn;
	}

	@RequestMapping(value = "/proxies/dependents/{cloudName}/access", method=RequestMethod.POST)
	public @ResponseBody AccessInfo addAccessData( @PathVariable("cloudName") final String cloudName, @RequestBody @Valid final AccessRequestInfo data )
	{
		AccessInfo rtn = ProxyService.getInstance().addAccessData(cloudName, data);
		return rtn;
	}
	@RequestMapping(value = "/proxies/dependents/{cloudName}/access/{type}", method=RequestMethod.GET)
	public @ResponseBody List<AccessInfo> getAccessData( @PathVariable("cloudName") final String cloudName, @PathVariable("type") final String type )
	{
		List<AccessInfo> rtn = ProxyService.getInstance().getAccessData(cloudName, type);
		return rtn;
	}

	@RequestMapping(value = "/proxies/dependents/{cloudName}/access/{type}/uuid/{uuid}", method=RequestMethod.DELETE)
	public @ResponseBody AccessInfo deleteAccessData( @PathVariable("cloudName") final String cloudName, @PathVariable("type") final String type, @PathVariable("uuid") String uuid, @RequestBody @Valid final GuardianInfo data )
	{
		AccessInfo rtn = ProxyService.getInstance().deleteAccessData(cloudName, type, uuid, data);
		return rtn;
	}
}
