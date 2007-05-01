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
package net.java.dev.openim.jabber;

import org.xmlpull.v1.XmlPullParser;

import net.java.dev.openim.ServerParameters;
import net.java.dev.openim.DefaultSessionProcessor;
import net.java.dev.openim.session.IMServerSession;
import net.java.dev.openim.session.IMSession;

/**
 * @version 1.5
 * @author AlAg
 */
public class StreamsImpl
    extends DefaultSessionProcessor
    implements Streams
{

    // Requerements
    protected ServerParameters serverParameters;

    // Locals
    protected String namespace;


    public void process( final IMSession session, final Object context )
        throws Exception
    {
        final XmlPullParser xpp = session.getXmlPullParser();
        namespace = xpp.getNamespace( null );
        processAttribute( session, context );
        if ( session instanceof IMServerSession )
        {
            getLogger().info(
                              "Start stream " + ( (IMServerSession) session ).getRemoteHostname() + " id "
                                  + session.getId() );
        }
        super.process( session, context );
        if ( session instanceof IMServerSession )
        {
            getLogger().info(
                              "Stop stream " + ( (IMServerSession) session ).getRemoteHostname() + " id "
                                  + session.getId() );
        }
    }

    //-------------------------------------------------------------------------
    public void processAttribute( final IMSession session, final Object context )
        throws Exception
    {

        final XmlPullParser xpp = session.getXmlPullParser();
        String to = xpp.getAttributeValue( "", "to" );
        String from = xpp.getAttributeValue( "", "from" );

        if ( from == null || from.length() == 0 )
        {
            getLogger().debug( "from attribut not specified in stream declaration" );
        }
        else
        {
            if ( session instanceof IMServerSession )
            {
                ( (IMServerSession) session ).setRemoteHostname( from );
            }
        }

        if ( session.getConnectionType() == IMSession.S2S_L2R_CONNECTION )
        {
            getLogger().debug( "Local to Remote connection " + to );
        }
        else
        {
            String s = "<stream:stream xmlns:stream='http://etherx.jabber.org/streams' " + "id='" + session.getId()
                + "' ";
            if ( session.getConnectionType() == IMSession.C2S_CONNECTION )
            {
                s += "xmlns='jabber:client' ";
            }
            else if ( session.getConnectionType() == IMSession.S2S_R2L_CONNECTION )
            {
                s += "xmlns='jabber:server' xmlns:db='jabber:server:dialback' ";
            }
            s += "from='" + serverParameters.getHostName() + "'>";
            session.writeOutputStream( s );

        }
    }

    public String getNamespace()
    {
        return namespace;
    }

}
