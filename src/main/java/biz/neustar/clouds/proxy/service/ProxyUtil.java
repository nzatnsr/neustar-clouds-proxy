package biz.neustar.clouds.proxy.service;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.digest.DigestUtils;

public class ProxyUtil
{
	public static String getDigest( String globalSalt, String localSalt, String secretToken ) throws java.io.UnsupportedEncodingException
	{
		String rtn = DigestUtils.sha512Hex(globalSalt + ":" + localSalt + ":" + DigestUtils.sha512Hex(globalSalt + ":" + Base64.encodeBase64String(secretToken.getBytes("UTF-8"))));
		return rtn;
	}

	private static void usage()
	{
		System.out.println("Usage: java -cp ... biz.neustar.clouds.proxy.service.ProxyUtil option parameters");
		System.out.println("");
		System.out.println("       Option: -digest globalSalt localSalt secretToken");
		System.exit(0);
	}

	public static void main( String args[] )
	{
		if( args.length == 0 )
		{
			usage();
		}
		if( args[0].equals("-digest") == true )
		{
			digest(args);
		}
		usage();
	}

	private static void digest( String args[] )
	{
		String secretToken = "secret";
		String globalSalt  = "global";
		String localSalt   = "local";
		if( args.length > 1 )
		{
			globalSalt = args[1];
		}
		if( args.length > 2 )
		{
                        localSalt = args[2];
		}
		if( args.length > 3 )
		{
			secretToken = args[3];
                }
		try
		{
                	String digest = ProxyUtil.getDigest(globalSalt, localSalt, secretToken);

			System.out.println("globalSalt  " + globalSalt);
			System.out.println("localSalt   " + localSalt);
	                System.out.println("secretToken " + secretToken);
			System.out.println("digest      " + digest);
		}
		catch( Exception e )
		{
			e.printStackTrace();
		}
		System.exit(0);
	}
}
