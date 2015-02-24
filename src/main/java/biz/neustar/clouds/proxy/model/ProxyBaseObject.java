package biz.neustar.clouds.proxy.model;

import org.springframework.http.converter.json.*;

//import org.codehaus.jackson.map.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;

import org.springframework.context.annotation.*;
import org.springframework.http.converter.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProxyBaseObject
{
	protected static final Logger logger = LoggerFactory.getLogger(ProxyBaseObject.class);

	public ProxyBaseObject()
	{
	}

	@Override
	public String toString()
	{
		String rtn = null;
		try
		{
			ObjectMapper mapper = new ObjectMapper();
			mapper.configure(SerializationFeature.INDENT_OUTPUT, true);
			mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
			mapper.setPropertyNamingStrategy(PropertyNamingStrategy.CAMEL_CASE_TO_LOWER_CASE_WITH_UNDERSCORES);
			mapper.setDateFormat(new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ"));
			rtn = mapper.writeValueAsString(this);
		}
		catch( Exception e )
		{
			logger.error("toString() failed - " + e.getMessage(), e);
		}
		return rtn;
	}
}
