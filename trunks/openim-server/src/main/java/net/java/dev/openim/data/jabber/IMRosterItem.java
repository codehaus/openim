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

import net.java.dev.openim.tools.XMLToString;
import net.java.dev.openim.tools.JIDParser;

/**
 * @version 1.5
 * @author AlAg
 */
public class IMRosterItem
    implements java.io.Serializable
{
    private static final long serialVersionUID = 15L;
    
    public static final String SUBSCRIPTION_REMOVE = "remove";
    public static final String SUBSCRIPTION_BOTH = "both";
    public static final String SUBSCRIPTION_NONE = "none";
    public static final String SUBSCRIPTION_TO = "to";
    public static final String SUBSCRIPTION_FROM = "from";
    public static final String ASK_SUBSCRIBE = "subscribe";
    public static final String ASK_UNSUBSCRIBE = "unsubscribe";

    private String name;
    private String jid;
    private String group;
    private String subscription;
    private String ask;

    public final void setName( String name )
    {
        this.name = name;
    }

    public final String getName()
    {
        return name;
    }

    public final void setJID( String jid )
    {
        if ( jid != null )
        {
            this.jid = JIDParser.getJID( jid );
        }
    }

    public final String getJID()
    {
        return jid;
    }

    public final void setGroup( String group )
    {
        this.group = group;
    }

    public final String getGroup()
    {
        return group;
    }

    public final void setSubscription( String subscription )
    {
        this.subscription = subscription;
    }

    public final String getSubscription()
    {
        return subscription;
    }

    public final void setAsk( String ask )
    {
        this.ask = ask;
    }

    public final String getAsk()
    {
        return ask;
    }

    public boolean equals( Object obj )
    {
        return jid.equals( ( (IMRosterItem) obj ).jid );
    }

    public String toString()
    {

        XMLToString item = new XMLToString( "item" );
        item.addFilledAttribut( "name", name );
        item.addFilledAttribut( "jid", jid );
        item.addFilledAttribut( "subscription", subscription );
        item.addFilledAttribut( "ask", ask );

        XMLToString group = new XMLToString( "group" );
        group.addTextNode( this.group );
        item.addElement( group );

        return item.toString();
    }
}
