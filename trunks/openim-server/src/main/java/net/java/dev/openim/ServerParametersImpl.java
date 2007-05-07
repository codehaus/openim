/*
 * BSD License http://open-im.net/bsd-license.html
 * Copyright (c) 2003, OpenIM Project http://open-im.net
 * All rights reserved.
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the OpenIM project. For more
 * information on the OpenIM project, please see
 * http://open-im.net/
 */
package net.java.dev.openim;

import java.util.List;
import java.net.InetAddress;

import org.codehaus.plexus.logging.AbstractLogEnabled;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;

/**
 * @version 1.5
 * @author AlAg
 */
public class ServerParametersImpl
    extends AbstractLogEnabled
    implements ServerParameters, Initializable
{

    private List<String> hostnames;

    private int localClientPort;
    private int localClientThreadPool;

    private int localSSLClientPort;
    private int localSSLClientThreadPool;
    
    private int localServerPort;
    private int localServerThreadPool;
    
    private int localSSLServerPort;
    private int localSSLServerThreadPool;
    
    private int remoteServerPort;

    //-------------------------------------------------------------------------
    public void initialize()
        throws InitializationException
    {
        if ( hostnames.size() == 0 )
        {
            try
            {
                hostnames.add( InetAddress.getLocalHost().getHostName() );
            }
            catch ( java.net.UnknownHostException e )
            {
                throw new InitializationException( e.getMessage(), e );
            }
        }

    }

    //-------------------------------------------------------------------------
    public final int getLocalClientPort()
    {
        return localClientPort;
    }

    //-------------------------------------------------------------------------
    public final int getLocalSSLClientPort()
    {
        return localSSLClientPort;
    }

    //-------------------------------------------------------------------------
    public final int getLocalServerPort()
    {
        return localServerPort;
    }

    //-------------------------------------------------------------------------
    public final int getLocalSSLServerPort()
    {
        return localSSLServerPort;
    }

    //-------------------------------------------------------------------------
    public final List getHostNameList()
    {
        return hostnames;
    }

    //-------------------------------------------------------------------------
    public final String getHostName()
    {
        return (String) hostnames.get( 0 );
    }

    //-------------------------------------------------------------------------
    public final int getRemoteServerPort()
    {
        return remoteServerPort;
    }
    
    
    //-------------------------------------------------------------------------
    public int getLocalClientThreadPool()
    {
        return localClientThreadPool;
    }
    //-------------------------------------------------------------------------
    public int getLocalSSLServerThreadPool()
    {
        return localSSLServerThreadPool;
    }
    //-------------------------------------------------------------------------
    public int getLocalServerThreadPool()
    {
        return localServerThreadPool;
    }
    //-------------------------------------------------------------------------
    public int getLocalSSLClientThreadPool()
    {
        return localSSLClientThreadPool;
    }

    
    
    
    
}
