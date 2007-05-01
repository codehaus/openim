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
package net.java.dev.openim.data.jabber;

import java.util.List;

import org.codehaus.plexus.logging.AbstractLogEnabled;

import net.java.dev.openim.data.Account;
import net.java.dev.openim.data.storage.AccountRepositoryHolder;
import net.java.dev.openim.data.storage.RosterListRepositoryHolder;

/**
 * @version 1.5
 * @author AlAg
 */
public class UserImpl
    extends AbstractLogEnabled
    implements User
{
    // Requirements
    private AccountRepositoryHolder accountHolder;
    private RosterListRepositoryHolder rosterListHolder;

    // Locals
    private String name;
    private String hostname;
    private String password;
    private String digest;
    private String resource;

    //-------------------------------------------------------------------------
    public final String getName()
    {
        return name;
    }

    //-------------------------------------------------------------------------
    public final void setName( final String name )
    {
        this.name = name;
    }

    //-------------------------------------------------------------------------
    public final String getHostname()
    {
        return hostname;
    }

    //-------------------------------------------------------------------------
    public final void setHostname( final String hostname )
    {
        this.hostname = hostname;
    }

    //-------------------------------------------------------------------------
    public final void setPassword( final String password )
    {
        this.password = password;
    }

    //-------------------------------------------------------------------------
    public final String getPassword()
    {
        return password;
    }

    //-------------------------------------------------------------------------
    public final String getResource()
    {
        return resource;
    }

    //-------------------------------------------------------------------------
    public final String getDigest()
    {
        return digest;
    }

    //-------------------------------------------------------------------------
    public final void setDigest( final String digest )
    {
        this.digest = digest;
    }

    //-------------------------------------------------------------------------
    public final void setResource( final String resource )
    {
        this.resource = resource;
    }

    //-------------------------------------------------------------------------
    public boolean isAuthenticationTypeSupported( final int type )
    {
        Account account = accountHolder.getAccount( name );
        boolean b = false;
        if ( account == null )
        {
            getLogger().warn( "Account " + name + " does not exist" );
        }
        else
        {
            b = account.isAuthenticationTypeSupported( type );
        }
        return b;
    }

    //-------------------------------------------------------------------------
    public void authenticate( String sessionId )
        throws Exception
    {
        getLogger().info( "Authenticating " + getJID() + " digest " + digest );

        Account account = accountHolder.getAccount( name );
        if ( account == null )
        {
            throw new Exception( "Unknow JID " + getJIDAndRessource() );
        }

        // no password assuming digest
        if ( password == null )
        {
            account.authenticate( Account.AUTH_TYPE_DIGEST, digest, sessionId );
        }

        else
        { // password available: plain authentification
            account.authenticate( Account.AUTH_TYPE_PLAIN, password, sessionId );
        }
    }

    //-------------------------------------------------------------------------
    public final String getJID()
    {
        String s = name;
        if ( hostname != null )
        {
            s += "@" + hostname;
        }
        return s;
    }

    //-------------------------------------------------------------------------
    public final String getNameAndRessource()
    {
        return name + "/" + resource;
    }

    //-------------------------------------------------------------------------
    public final String getJIDAndRessource()
    {
        return getJID() + "/" + resource;
    }

    //-------------------------------------------------------------------------
    @SuppressWarnings("unchecked")
    public List<IMRosterItem> getRosterItemList()
    {
        List<IMRosterItem> rosterList = rosterListHolder.getRosterList( name );

        // correcting hostname hack? (maybe should be removed)
        /*
         if( rosterList != null ){
         for( int i=0, l=rosterList.size(); i<l; i++ ){
         IMRosterItem item = (IMRosterItem)rosterList.get( i );
         String hostname = JIDParser.getHostname( item.getJID() );
         if( hostname == null ){
         String name = JIDParser.getName( item.getJID() );
         item.setJID( name+'@'+m_serverParameters.getHostName() );
         }
         }
         }
         */
        return rosterList;
    }

    //-------------------------------------------------------------------------
    public void setRosterItemList( List rosterlist )
    {
        rosterListHolder.setRosterList( name, rosterlist );
    }

    //-------------------------------------------------------------------------
    public String toString()
    {
        return getJIDAndRessource();
    }

}
