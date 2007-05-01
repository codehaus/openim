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
package net.java.dev.openim.jabber.client;

import org.xmlpull.v1.XmlPullParser;

import net.java.dev.openim.DefaultSessionProcessor;
import net.java.dev.openim.data.jabber.IMIq;
import net.java.dev.openim.session.IMClientSession;
import net.java.dev.openim.session.IMSession;

/**
 * @version 1.5
 * @author AlAg
 */
public class IqImpl
    extends DefaultSessionProcessor
    implements Iq
{

    public void process( final IMSession session, final Object context )
        throws Exception
    {

        XmlPullParser xpp = session.getXmlPullParser();

        for ( int i = 0, l = xpp.getAttributeCount(); i < l; i++ )
        {
            getLogger().debug(
                               "Attribut ns: " + xpp.getAttributeNamespace( i ) + " name: " + xpp.getAttributeName( i )
                                   + " value: " + xpp.getAttributeValue( i ) );
        }

        IMIq iq = new IMIq();
        iq.setId( xpp.getAttributeValue( "", "id" ) );
        iq.setType( xpp.getAttributeValue( "", "type" ) );
        iq.setTo( xpp.getAttributeValue( "", "to" ) );
        iq.setFrom( xpp.getAttributeValue( "", "from" ) );
        getLogger().debug( "Got IQ " + iq );

        super.process( session, iq );

        if ( IMIq.TYPE_RESULT.equals( iq.getType() ) )
        {
            iq.setFrom( ( (IMClientSession) session ).getUser().getJIDAndRessource() );
            session.getRouter().route( session, iq );
        }

    }

}
