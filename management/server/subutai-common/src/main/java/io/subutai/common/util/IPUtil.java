package io.subutai.common.util;


import java.net.InetAddress;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Strings;

import io.subutai.common.settings.Common;


public class IPUtil
{
    private static final Logger LOGGER = LoggerFactory.getLogger( IPUtil.class );
    private static long ipToLong(InetAddress ip)
    {
        byte[] octets = ip.getAddress();
        long result = 0;

        for (byte octet : octets)
        {
            result <<= 8;
            result |= octet & 0xff;
        }
        return result;
    }

    public static boolean isValidIPRange(String ipStart, String ipEnd, String ipToCheck)
    {
        try
        {
            if("*".equals( ipStart ) || "".equals( ipStart ))
            {
                return true;
            }
            else
            {
                long ipLo = ipToLong( InetAddress.getByName( ipStart ) );
                long ipHi = ipToLong( InetAddress.getByName( ipEnd ) );
                long ipToTest = ipToLong( InetAddress.getByName( ipToCheck ) );
                return ( ipToTest >= ipLo && ipToTest <= ipHi );
            }
        }
        catch (Exception e)
        {
            LOGGER.error( "Error parsing InetAddress", e );
            return false;
        }
    }

    public static boolean isValid( final String ip )
    {
        return !Strings.isNullOrEmpty( ip ) && ip.matches( Common.IP_REGEX );
    }
}
