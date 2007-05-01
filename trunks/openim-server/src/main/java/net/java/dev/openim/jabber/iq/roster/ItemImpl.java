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
package net.java.dev.openim.jabber.iq.roster;

import org.xmlpull.v1.XmlPullParser;

import net.java.dev.openim.data.jabber.IMRosterItem;
import net.java.dev.openim.session.IMSession;
import net.java.dev.openim.DefaultSessionProcessor;

/**
 * @version 1.5
 * @author AlAg
 */
public class ItemImpl
    extends DefaultSessionProcessor
    implements Item
{

    public void process( final IMSession session, final Object context )
        throws Exception
    {
        XmlPullParser xpp = session.getXmlPullParser();
        IMRosterItem rosterItem = (IMRosterItem) context;
        rosterItem.setName( xpp.getAttributeValue( "", "name" ) );
        rosterItem.setJID( xpp.getAttributeValue( "", "jid" ) );
        rosterItem.setSubscription( xpp.getAttributeValue( "", "subscription" ) );
        rosterItem.setAsk( xpp.getAttributeValue( "", "ask" ) );
        super.process( session, context );
    }

}
