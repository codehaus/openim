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
package net.java.dev.openim.jabber.iq.vcardtemp;

import net.java.dev.openim.DefaultSessionProcessor;

import net.java.dev.openim.data.jabber.IMIq;
import net.java.dev.openim.data.storage.PrivateRepositoryHolder;

import org.xmlpull.v1.XmlPullParser;

import net.java.dev.openim.ServerParameters;
import net.java.dev.openim.session.IMClientSession;
import net.java.dev.openim.session.IMSession;
import net.java.dev.openim.tools.JIDParser;

/**
 * @version 1.5
 * @author AlAg
 */
public class VCardImpl
    extends DefaultSessionProcessor
    implements VCard
{

    private ServerParameters serverParameters;

    private PrivateRepositoryHolder privateRepository;

    //-------------------------------------------------------------------------

    public void process( final IMSession session, final Object context )
        throws Exception
    {

        String type = ( (IMIq) context ).getType();

        // GET
        if ( IMIq.TYPE_GET.equals( type ) )
        {
            get( session, context );
        }
        else if ( IMIq.TYPE_SET.equals( type ) )
        {
            set( (IMClientSession) session, context );
        }
        else if ( IMIq.TYPE_RESULT.equals( type ) )
        {
            result( session, context );
        }
    }

    //-------------------------------------------------------------------------
    private void get( final IMSession session, Object context )
        throws Exception
    {

        final XmlPullParser xpp = session.getXmlPullParser();
        final String vcardname = xpp.getNamespace() + ':' + xpp.getName();

        String iqId = ( (IMIq) context ).getId();
        String to = ( (IMIq) context ).getTo();
        String from = ( (IMIq) context ).getFrom();

        if ( to == null || to.length() == 0 )
        {
            to = ( (IMClientSession) session ).getUser().getJID();
        }
        if ( from == null || from.length() == 0 )
        {
            from = ( (IMClientSession) session ).getUser().getJID();
        }

        IMIq iq = null;

        if ( serverParameters.getHostNameList().contains( JIDParser.getHostname( to ) ) )
        {
            String data = privateRepository.getData( to, vcardname.toLowerCase() );
            if ( data == null )
            {
                data = "<vCard xmlns='vcard-temp'/>";
            }

            getLogger().debug( "Get " + to + "/" + vcardname + " vcard: " + data );

            // local request
            iq = new IMIq();
            iq.setFrom( to );
            iq.setTo( from );
            iq.setId( iqId );
            iq.setType( IMIq.TYPE_RESULT );
            iq.setStringData( data );
        }
        else
        {
            iq = new IMIq();
            iq.setFrom( from );
            iq.setTo( to );
            iq.setId( iqId );
            iq.setType( IMIq.TYPE_GET );
            iq.setStringData( "<vCard xmlns='vcard-temp'/>" );
        }

        session.getRouter().route( session, iq );

        skip( xpp );
    }

    //-------------------------------------------------------------------------
    private void set( final IMClientSession session, final Object context )
        throws Exception
    {

        final XmlPullParser xpp = session.getXmlPullParser();

        String vcardname = xpp.getNamespace() + ':' + xpp.getName();

        String data = serialize( xpp ).toString();

        getLogger().debug( "Set " + session.getUser().getJID() + "/" + vcardname + " vcard: " + data );
        if ( data != null )
        {
            privateRepository.setData( session.getUser().getJID(), vcardname.toLowerCase(), data );
        }

        IMIq iq = (IMIq) context;
        String iqId = iq.getId();
        String to = iq.getTo();
        String from = iq.getFrom();
        
        if( to == null )
        {
            to = session.getUser().getJID();
        }
        if( from == null )
        {
            to = session.getUser().getJID();
        }
           

        String s = "<iq type='result'";
        s += " from='" + to + "'";
        s += " to='" + from + "'";
        s += " id='" + iqId + "'/>";

        getLogger().info( "-------->" + s );
        session.writeOutputStream( s );

    }

    //-------------------------------------------------------------------------
    private void result( final IMSession session, final Object context )
        throws Exception
    {

        final XmlPullParser xpp = session.getXmlPullParser();
        String to = ( (IMIq) context ).getTo();

        if ( serverParameters.getHostNameList().contains( JIDParser.getHostname( to ) ) )
        {
            // local request
            String iqId = ( (IMIq) context ).getId();
            String from = ( (IMIq) context ).getFrom();
            String data = serialize( xpp ).toString();

            IMIq iq = new IMIq();
            iq.setFrom( from );
            iq.setTo( to );
            iq.setId( iqId );
            iq.setType( IMIq.TYPE_RESULT );
            iq.setStringData( data );
            session.getRouter().route( session, iq );
        }

        else
        {
            getLogger().warn( "Abnormal result for remote delivery?" );
            skip( xpp );
        }
    }
}
