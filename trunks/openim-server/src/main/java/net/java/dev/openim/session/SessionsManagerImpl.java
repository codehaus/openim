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
package net.java.dev.openim.session;

import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;

import org.codehaus.plexus.logging.AbstractLogEnabled;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.ServiceLocator;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Serviceable;

/**
 * @version 1.5
 * @author AlAg
 */
public class SessionsManagerImpl
    extends AbstractLogEnabled
    implements SessionsManager, Initializable, Serviceable
{

    private ServiceLocator serviceLocator;
    
    // We really need this to be able to also shutdown non registered sessions
    private Map<Long, IMSession> activeSessions;


    public void service( ServiceLocator serviceLocator )
    {
        this.serviceLocator = serviceLocator;
    }

    //-------------------------------------------------------------------------
    public IMServerSession getNewServerSession()
        throws Exception
    {
        IMServerSession session = (IMServerSession) serviceLocator.lookup( IMServerSession.class.getName(), "IMServerSession" );
        // Are server session even unregistered?
        return session;
    }

    //-------------------------------------------------------------------------
    public IMClientSession getNewClientSession()
        throws Exception
    {
        IMClientSession session = (IMClientSession) serviceLocator.lookup( IMClientSession.class.getName(), "IMClientSession" );
        synchronized ( activeSessions )
        {
            activeSessions.put( new Long( session.getId() ), session );
        }
        return session;
    }
    //-------------------------------------------------------------------------    
    public void initialize()
    throws InitializationException
    {
        activeSessions = new HashMap<Long, IMSession>();
    }
    //-------------------------------------------------------------------------
    public void release( IMSession session )
    {
        if ( session != null )
        {
            try
            {
                if ( !session.isClosed() )
                {
                    session.close();
                }
                else
                {
                    getLogger().warn( "Session " + session.getId() + " already diposed" );
                }
            }
            catch ( Exception e )
            {
                getLogger().warn( "Session " + session.getId() + " release failure " + e.getMessage(), e );
            }
            // Remove from sessionsMap
            synchronized ( activeSessions )
            {
                activeSessions.remove( new Long( session.getId() ) );
            }
        } // if
    }

    //-------------------------------------------------------------------------
    public void releaseSessions()
    {
        getLogger().debug( "Releasing sessions " );
        // Avoid concurrent mod
        Map<Long, IMSession> clonedSessions = new HashMap<Long, IMSession>( activeSessions );
        Iterator it = clonedSessions.values().iterator();
        while ( it.hasNext() )
        {
            IMSession sess = (IMSession) it.next();
            release( sess );
        } // end of while ()
    }

}
