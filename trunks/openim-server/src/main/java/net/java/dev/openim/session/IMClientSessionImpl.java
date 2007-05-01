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

import java.util.List;

import net.java.dev.openim.IMPresenceHolder;
import net.java.dev.openim.data.jabber.IMPresence;
import net.java.dev.openim.data.jabber.IMPresenceImpl;
import net.java.dev.openim.data.jabber.IMRosterItem;
import net.java.dev.openim.data.jabber.User;

import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;

//import net.java.dev.openim.tools.InputStreamDebugger;

/**
 * @version 1.5
 * @author AlAg
 */
public class IMClientSessionImpl
    extends AbstractIMSession
    implements IMClientSession, Initializable
{

    // Locals
    private User user;
    private IMPresence presence;
    
    // Requirements
    private IMPresenceHolder presenceHolder;

    //-------------------------------------------------------------------------


    public void initialize()
        throws InitializationException
    {
        disposed = new Boolean( false );
        synchronized ( lastSessionId )
        {
            sessionId = lastSessionId.longValue();
            lastSessionId = new Long( sessionId + 1 );
        }
    }

    //-------------------------------------------------------------------------
    public void close()
    {

        getLogger().debug( "Closing session id " + getId() );
        synchronized ( disposed )
        {

            try
            {
                // set disconnected to all roster friend
                if ( user != null && getConnectionType() == IMSession.C2S_CONNECTION )
                {
                    IMPresence presence = presenceHolder.removePresence( user.getJIDAndRessource() );
                    getLogger().debug( "Remove presence jid " + user.getJIDAndRessource() );

                    // emit unavailaible to all user
                    presence = new IMPresenceImpl();
                    presence.setFrom( user.getJIDAndRessource() );
                    presence.setType( IMPresence.TYPE_UNAVAILABLE );
                    presence.setStatus( "Disconnected" );
                    List rosterList = user.getRosterItemList();
                    if ( rosterList != null )
                    {
                        for ( int i = 0, l = rosterList.size(); i < l; i++ )
                        {
                            IMRosterItem item = (IMRosterItem) rosterList.get( i );
                            getLogger().debug( "Item " + item );
                            IMPresence localPresence = (IMPresence) presence.clone();
                            localPresence.setTo( item.getJID() );
                            if ( router != null )
                            {
                                router.route( this, localPresence );
                            }
                        }
                    }

                }

                if ( router != null )
                {
                    router.unregisterSession( this );
                }

            }
            catch ( Exception e )
            {
                getLogger().warn( "Session dispose failed (stage1): " + e.getMessage(), e );
            }

            try
            {

                writeOutputStream( "</stream:stream>" );

            }
            catch ( Exception e )
            {
                getLogger().warn( "Session dispose failed (stage2): " + e.getMessage() );
            }

            try
            {
                getLogger().debug( "Session " + sessionId + " closed" );

                if ( socket != null && !socket.isClosed() )
                {
                    socket.close();
                    outputStreamWriter.close();
                }
            }
            catch ( Exception e )
            {
                getLogger().warn( "Session dispose failed (stage3): " + e.getMessage(), e );
            }
            getLogger().debug( "Session " + sessionId + " disposed " );
        }

        disposed = new Boolean( true );

    }

    //-------------------------------------------------------------------------
    //-------------------------------------------------------------------------
    //-------------------------------------------------------------------------
    public final void setUser( final User user )
    {
        this.user = user;
    }

    //-------------------------------------------------------------------------
    public final User getUser()
    {
        return user;
    }

    //-------------------------------------------------------------------------
    public IMPresence getPresence()
    {
        return presence;
    }

    //-------------------------------------------------------------------------
    public void setPresence( IMPresence presence )
    {
        this.presence = presence;
    }

    //-------------------------------------------------------------------------
    //-------------------------------------------------------------------------
    public int getConnectionType()
    {
        return C2S_CONNECTION;
    }

}
