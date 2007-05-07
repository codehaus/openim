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

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import net.java.dev.openim.data.Account;
import net.java.dev.openim.data.Deferrable;
import net.java.dev.openim.data.Transitable;
import net.java.dev.openim.data.jabber.User;
import net.java.dev.openim.data.storage.AccountRepositoryHolder;
import net.java.dev.openim.data.storage.DeferrableListRepositoryHolder;
import net.java.dev.openim.log.MessageLogger;
import net.java.dev.openim.log.MessageRecorder;
import net.java.dev.openim.session.IMClientSession;
import net.java.dev.openim.session.IMSession;
import net.java.dev.openim.session.SessionsManager;
import net.java.dev.openim.tools.JIDParser;

import org.codehaus.plexus.logging.AbstractLogEnabled;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;

/**
 * @version 1.5
 * @author AlAg
 */
public class IMRouterImpl
    extends AbstractLogEnabled
    implements IMRouter, Initializable
{

    // Requirements
    private ServerParameters serverParameters;
    private SessionsManager sessionsManager;
    private DeferrableListRepositoryHolder deferrableListHolder;
    private AccountRepositoryHolder accountHolder;
    private MessageLogger messageLogger;
    private MessageRecorder messageRecorder;

    // Configurations
    private int deliveryRetryDelay;
    private int deliveryMaxRetry;
    private long deliveryMessageQueueTimeout;

    // Locals
    private Map<String,IMSession> sessionMap;
    private Map<String,RemoteDeliveryThreadPerHost> remoteDeliveryThreadMap;
    private S2SConnectorManager s2sConnectorManager;

    //-------------------------------------------------------------------------
    public void initialize()
        throws InitializationException
    {
        //m_validHost = new HashSet();
        sessionMap = new HashMap<String,IMSession>();
        remoteDeliveryThreadMap = new HashMap<String,RemoteDeliveryThreadPerHost>();
    }

    //-------------------------------------------------------------------------
    public S2SConnectorManager getS2SConnectorManager()
    {
        return s2sConnectorManager;
    }

    //-------------------------------------------------------------------------
    public void setS2SConnectorManager( S2SConnectorManager s2sConnectorManager )
    {
        this.s2sConnectorManager = s2sConnectorManager;
    }

    //-------------------------------------------------------------------------
    public void registerSession( final IMClientSession session )
    {

        final User user = session.getUser();

        if ( session.getConnectionType() == IMSession.C2S_CONNECTION && user != null )
        {
            getLogger().debug( "Session map before register : " + sessionMap );
            getLogger().debug(
                               "Register session user: " + user.getNameAndRessource() + " session id "
                                   + session.getId() );
            try
            {
                IMSession prevSession = (IMSession) sessionMap.get( user.getNameAndRessource() );
                if ( prevSession != null )
                {
                    getLogger().debug( "Allready register session: " + prevSession.getId() );
                    sessionsManager.release( prevSession );
                }
            }
            catch ( Exception e )
            {
                getLogger().error( e.getMessage(), e );
            }
            synchronized ( sessionMap )
            {                
                sessionMap.put( user.getNameAndRessource(), session );
            }

            try
            {
                deliverQueueMessage( session, user.getName() );
            }
            catch ( Exception e )
            {
                getLogger().warn( "Failed to deliver queue message " + e.getMessage(), e );
            }

        } // if

    }

    //-------------------------------------------------------------------------
    public void unregisterSession( final IMClientSession session )
    {
        if ( session instanceof IMClientSession )
        {
            User user = ( (IMClientSession) session ).getUser();
            if ( user != null )
            {
                getLogger().debug(
                                   "Unregister register session user: " + user.getJIDAndRessource() + " session id "
                                       + session.getId() );
                synchronized ( sessionMap )
                {                    
                    sessionMap.remove( user.getNameAndRessource() );
                }
                //m_sessionMap.remove( user.getName() );
            }
        }
    }

    //-------------------------------------------------------------------------    
    public List<IMSession> getAllRegisteredSession( final String name )
    {

        List<IMSession> list = new ArrayList<IMSession>( 1 );
        final String[] nameArray = (String[]) sessionMap.keySet().toArray( new String[0] );
        for ( int i = 0, l = nameArray.length; i < l; i++ )
        {
            getLogger().debug( "Check if " + name + " could match " + nameArray[i] );
            if ( nameArray[i].startsWith( name ) )
            {
                list.add( sessionMap.get( nameArray[i] ) );
            }
        } // for
        return list;
    }

    //-------------------------------------------------------------------------
    private IMClientSession getRegisteredSession( final String name )
    {
        IMClientSession session = (IMClientSession) sessionMap.get( name );
        getLogger().debug( ">>> getting session for " + name + " having map key " + sessionMap.keySet() );
        if ( session == null )
        {
            String username = name;
            if ( name.indexOf( '/' ) > 0 )
            {
                // we have a ressource => get the login
                username = JIDParser.getName( name );
            }

            //TODO: check if correct (was name)
            List list = getAllRegisteredSession( username );
            for ( int i = 0, l = list.size(); i < l; i++ )
            {
                IMClientSession s = (IMClientSession) list.get( i );
                if ( session == null || ( getPriorityNumber( s ) > getPriorityNumber( session ) ) )
                {
                    session = s;
                    getLogger().debug( "Select session " + s );
                }
            } // for
        } // if

        return session;
    }

    //-------------------------------------------------------------------------
    private final int getPriorityNumber( IMClientSession session )
    {
        int priorityNumber = 0;
        if ( session.getPresence() != null )
        {
            String priorityStr = session.getPresence().getPriority();

            if ( priorityStr != null )
            {
                try
                {
                    priorityNumber = Integer.parseInt( priorityStr );
                }
                catch ( Exception e )
                {
                    getLogger().error( e.getMessage(), e );
                }
            }
        }
        return priorityNumber;
    }

    //-------------------------------------------------------------------------
    @SuppressWarnings("unchecked")
    public void route( final IMSession currentSession, final Transitable transit )
        throws java.io.IOException
    {
        final String to = transit.getTo();
        //final String from = transit.getFrom();
        final String toHostname = JIDParser.getHostname( to );

        if ( serverParameters.getHostNameList().contains( toHostname ) )
        { // local delivery

            final IMClientSession session = getRegisteredSession( JIDParser.getNameAndRessource( to ) );

            if ( session == null )
            {
                if ( transit instanceof Deferrable )
                {

                    final String username = JIDParser.getName( to );
                    Account account = accountHolder.getAccount( username );
                    if ( account == null )
                    {
                        getLogger().debug( to + " unknown user. Transit value was: " + transit );
                        String from = transit.getFrom();
                        transit.setError( "Not Found" );
                        transit.setErrorCode( 404 );
                        transit.setFrom( to );
                        transit.setTo( from );
                        transit.setType( Transitable.TYPE_ERROR );

                        messageLogger.log( transit );
                        currentSession.writeOutputStream( transit.toString() );
                        messageLogger.log( transit );
                        messageRecorder.record( transit );

                    }
                    else
                    {
                        getLogger()
                            .debug(
                                    to
                                        + " is not connected for getting message, should store for offline dispatch. Transit value was: "
                                        + transit );

                        List<Transitable> list = deferrableListHolder.getDeferrableList( username );
                        if ( list == null )
                        {
                            list = new ArrayList<Transitable>();
                        }
                        list.add( transit );
                        deferrableListHolder.setDeferrableList( username, list );
                    } // if else
                } // if
            } // if
            else
            {
                transit.setTo( session.getUser().getJIDAndRessource() );
                session.writeOutputStream( transit.toString() );
                messageLogger.log( transit );
                messageRecorder.record( transit );
            } // else
        } // if

        else
        { // remote delivery
            getLogger().debug( "Remote delivery to " + transit.getTo() );
            enqueueRemoteDelivery( transit, currentSession );
            getLogger().debug( "Enqueued to " + transit.getTo() );
            //new Thread( new AsyncDeliverer( transit, toHostname, currentSession ) ).start();
        }

    }

    //-------------------------------------------------------------------------
    public void deliverQueueMessage( IMSession currentSession, String username )
        throws java.io.IOException
    {
        final List list = deferrableListHolder.getDeferrableList( username );
        if ( list != null )
        {
            for ( int i = 0, l = list.size(); i < l; i++ )
            {
                route( currentSession, (Transitable) list.get( i ) );
            }
        }
        // empty list
        deferrableListHolder.setDeferrableList( username, new ArrayList() );

    }

    //-------------------------------------------------------------------------
    private void enqueueRemoteDelivery( Transitable transitable, IMSession session )
    {
        TransitableAndSession tas = new TransitableAndSession( transitable, session );

        final String hostname = tas.getHostname();
        synchronized ( remoteDeliveryThreadMap )
        {
            RemoteDeliveryThreadPerHost remoteDeliveryThread = (RemoteDeliveryThreadPerHost) remoteDeliveryThreadMap
                .get( hostname );
            if ( remoteDeliveryThread == null )
            {
                // should get from a pool (to implem later)
                if ( hostname == null )
                {
                    getLogger().warn( "Absurd hostname for Transitable " + transitable );
                }

                remoteDeliveryThread = new RemoteDeliveryThreadPerHost( hostname );
                remoteDeliveryThread.enqueue( tas );

                remoteDeliveryThread.start();
                remoteDeliveryThreadMap.put( hostname, remoteDeliveryThread );
            }

            else
            {
                remoteDeliveryThread.enqueue( tas );
            }
        } // sync
    }

    //-------------------------------------------------------------------------
    //-------------------------------------------------------------------------
    //-------------------------------------------------------------------------
    public class TransitableAndSession
    {
        private Transitable m_transitable;

        private IMSession m_session;

        public TransitableAndSession( Transitable transitable, IMSession session )
        {
            m_transitable = transitable;
            m_session = session;
        }

        public Transitable getTransitable()
        {
            return m_transitable;
        }

        public IMSession getSession()
        {
            return m_session;
        }

        public String getHostname()
        {
            return JIDParser.getHostname( m_transitable.getTo() );
        }
    }

    //-------------------------------------------------------------------------
    //-------------------------------------------------------------------------
    //-------------------------------------------------------------------------
    public class RemoteDeliveryThreadPerHost
        extends Thread
    {
        private LinkedBlockingQueue<TransitableAndSession> perHostRemoteDeliveryQueue;

        private IMSession remoteSession = null;

        private String hostname;

        private String currentStatus;

        //----------------------------------------------------------------------
        public RemoteDeliveryThreadPerHost( String hostname )
        {
            this.hostname = hostname;
            perHostRemoteDeliveryQueue = new LinkedBlockingQueue<TransitableAndSession>();
            currentStatus = "";
        }

        //----------------------------------------------------------------------
        public void enqueue( TransitableAndSession tas )
        {
            getLogger().debug(
                               "Adding tas for " + hostname + " this thread (" + this + ") isAlive: " + isAlive()
                                   + " current status: " + currentStatus );
            perHostRemoteDeliveryQueue.add( tas );
        }

        //----------------------------------------------------------------------
        public void run()
        {
            currentStatus = "Started";
            getLogger().debug( "Starting thread " + this );
            while ( true )
            {
                TransitableAndSession tas = null;
                try
                {
                    tas = (TransitableAndSession) perHostRemoteDeliveryQueue.poll( 120, TimeUnit.SECONDS );
                }
                catch ( InterruptedException e )
                {
                    getLogger().debug( e.getMessage(), e );
                }
                getLogger().debug( "Remove tas for " + hostname );

                if ( tas != null )
                {
                    deliver( tas );
                    getLogger().debug( "Delivered tas for " + hostname );
                }
                else
                {
                    synchronized ( remoteDeliveryThreadMap )
                    {
                        if ( perHostRemoteDeliveryQueue.isEmpty() )
                        {
                            getLogger().debug( "Removing thread (" + this + "/" + hostname + ") from list" );
    
                            //RemoteDeliveryThreadPerHost remoteDeliveryThread = (RemoteDeliveryThreadPerHost) 
                            remoteDeliveryThreadMap.remove( hostname );
                            // should get back to pool (to implem later)
                            break;
                        } // if
                    }//sync
                }
            } // while true

            // cleanup
            //m_validHost.remove( m_hostname );
            sessionsManager.release( remoteSession );
            remoteSession = null;

            currentStatus = "Ended";
            getLogger().debug( "Ending thread " + this );
        } // run

        //----------------------------------------------------------------------
        private void deliver( TransitableAndSession tas )
        {
            Transitable transitable = tas.getTransitable();
            try
            {
                boolean failedToDeliver = true;
                for ( int retry = 0; retry < deliveryMaxRetry; retry++ )
                {
                    try
                    {
                        getLogger().debug(
                                           "Trying to send (" + transitable + ") to hostname " + hostname + " step "
                                               + retry );
                        if ( remoteSession == null || remoteSession.isClosed() )
                        {
                            remoteSession = s2sConnectorManager
                                .getRemoteSessionWaitForValidation( hostname, deliveryMessageQueueTimeout );
                        }

                        remoteSession.writeOutputStream( transitable.toString() );
                        messageLogger.log( transitable );
                        messageRecorder.record( transitable );
                        getLogger().debug( "Sent (" + transitable + ") to hostname " + hostname + " step " + retry );
                        failedToDeliver = false;
                        break;
                    }
                    catch ( java.net.SocketException e )
                    {
                        sessionsManager.release( remoteSession );
                        remoteSession = null;
                        temporise( e );
                    }
                    catch ( java.io.IOException e )
                    {
                        sessionsManager.release( remoteSession );
                        remoteSession = null;
                        temporise( e );
                    }
                    catch ( Exception e )
                    {
                        sessionsManager.release( remoteSession );
                        remoteSession = null;
                        //m_validHost.remove( m_hostname );
                        getLogger().warn( "Remote send failed " + e.getMessage(), e );
                        break;
                    }

                } // for

                if ( failedToDeliver )
                {
                    String to = transitable.getTo();
                    getLogger().info( "Failed to sent (from " + transitable.getFrom() + ") to hostname " + hostname );
                    String from = transitable.getFrom();
                    transitable.setError( "Delivery failed" );
                    transitable.setErrorCode( 500 );
                    transitable.setFrom( to );
                    transitable.setTo( from );
                    transitable.setType( Transitable.TYPE_ERROR );

                    try
                    {
                        tas.getSession().writeOutputStream( transitable.toString() );
                        messageLogger.log( transitable );
                        messageRecorder.record( transitable );
                    }
                    catch ( IOException e )
                    {
                        getLogger().warn( "Error delivery failed " + e.getMessage(), e );
                    }

                } // if

            }
            catch ( Exception e )
            {
                getLogger().warn( e.getMessage(), e );
            }
        } // deliver

        //----------------------------------------------------------------------
        private final void temporise( Exception e )
        {
            getLogger().warn( "Remote send failed (retying in " + deliveryRetryDelay + "ms) " + e.getMessage() );
            sessionsManager.release( remoteSession );
            remoteSession = null;

            try
            {
                sleep( deliveryRetryDelay );
            }
            catch ( InterruptedException ie )
            {
                getLogger().debug( ie.getMessage(), ie );
            }
            // we retry
        }
        //----------------------------------------------------------------------

    }

    public void releaseSessions()
    {
        getLogger().debug( "Releasing sessions " );
        synchronized ( sessionMap )
        {            
            Iterator it = sessionMap.values().iterator();
            while ( it.hasNext() )
            {
                IMSession sess = (IMSession) it.next();
                sessionsManager.release( sess );
            } // end of while ()
        }
    }
}
