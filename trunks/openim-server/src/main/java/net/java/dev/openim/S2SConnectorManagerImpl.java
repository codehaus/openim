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

import java.util.Map;
import java.util.HashMap;

import org.codehaus.plexus.logging.AbstractLogEnabled;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.ServiceLocator;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Serviceable;

import net.java.dev.openim.session.SessionsManager;
import net.java.dev.openim.session.IMServerSession;

/**
 * @version 1.5
 * @author AlAg
 */

public class S2SConnectorManagerImpl
    extends AbstractLogEnabled
    implements S2SConnectorManager, Initializable, Serviceable
{

    private Map<String,S2SConnector> hostnameAndS2SMap;
    private ServiceLocator serviceManager;
    private IMConnectionHandler connectionHandler;

    // Requirements
    private IMRouter router;
    private SessionsManager sessionsManager;

    
    
    
    public void service( ServiceLocator serviceLocator )
    {
        this.serviceManager = serviceLocator;
    }

    //-------------------------------------------------------------------------
    public void initialize()
        throws InitializationException
    {
        hostnameAndS2SMap = new HashMap<String,S2SConnector>();
    }

    //-------------------------------------------------------------------------    
    public void setConnectionHandler( IMConnectionHandler connectionHandler )
    {
        this.connectionHandler = connectionHandler;
    }

    //-------------------------------------------------------------------------
    public IMServerSession getCurrentRemoteSession( String hostname )
        throws Exception
    {
        IMServerSession session = null;
        synchronized ( hostnameAndS2SMap )
        {
            S2SConnector s2s = hostnameAndS2SMap.get( hostname );
            if ( s2s != null && !s2s.getSession().isClosed() )
            {
                session = s2s.getSession();
            }
        }
        return session;
    }

    //----------------------------------------------------------------------
    public IMServerSession getRemoteSessionWaitForValidation( String hostname, long timeout )
        throws Exception
    {

        IMServerSession session = null;
        S2SConnector s2s = null;
        synchronized ( hostnameAndS2SMap )
        {
            s2s = (S2SConnector) hostnameAndS2SMap.get( hostname );
            if ( s2s != null && !s2s.getSession().isClosed() )
            {
                session = s2s.getSession();
            }
            else
            {
                s2s = getS2SConnector( hostname );
                session = s2s.getSession();

            }
        }

        synchronized ( session )
        {
            // wait for validation
            if ( !session.getDialbackValid() )
            {
                s2s.sendResult();
                getLogger().info( "Wait validation for " + hostname + " for session " + session );
                session.wait( timeout );
            }
        }
        if ( !session.getDialbackValid() )
        {
            throw new Exception( "Unable to get dialback validation for " + hostname + " after timeout " + timeout
                + " ms" );
        }
        getLogger().info( "Validation granted from " + hostname + " for session " + session );

        return session;

    } // getremote session

    //-------------------------------------------------------------------------
    public void verifyRemoteHost( String hostname, String dialbackValue, String id, IMServerSession session )
        throws Exception
    {

        S2SConnector s2s = getS2SConnector( hostname );

        s2s.sendVerify( dialbackValue, id );
        if ( !s2s.getSession().getDialbackValid() )
        {
            s2s.sendResult();
        }

        session.setTwinSession( s2s.getSession() );
        s2s.getSession().setTwinSession( session );

    }

    //-------------------------------------------------------------------------
    private S2SConnector getS2SConnector( String hostname )
        throws Exception
    {
        S2SConnector s2s = null;
        synchronized ( hostnameAndS2SMap )
        {
            s2s = hostnameAndS2SMap.get( hostname );

            if ( s2s != null && !s2s.isAlive() )
            {
                getLogger().info( "Removing s2s for hostname (thread not alive) " + hostname );
                hostnameAndS2SMap.remove( hostname );
                s2s = null;
            }

            if ( s2s == null || s2s.getSession().isClosed() )
            {
                s2s = (S2SConnector) serviceManager.lookup( S2SConnector.class.getName() ,"S2SConnector" );
                s2s.setIMConnectionHandler( connectionHandler );
                s2s.setRouter( router );
                s2s.setSessionsManager( sessionsManager );
                s2s.setToHostname( hostname );
                new Thread( s2s ).start();
                hostnameAndS2SMap.put( hostname, s2s );
            }
        }
        return s2s;
    }

} // class
