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
package net.java.dev.openim.jabber.iq.jprivate;

import net.java.dev.openim.DefaultSessionProcessor;

import net.java.dev.openim.data.jabber.IMIq;
import net.java.dev.openim.data.storage.PrivateRepositoryHolder;
import net.java.dev.openim.session.IMClientSession;
import net.java.dev.openim.session.IMSession;

import org.xmlpull.v1.XmlPullParser;

/**
 * @version 1.5
 * @author AlAg
 */
public class QueryImpl
    extends DefaultSessionProcessor
    implements Query
{

    private PrivateRepositoryHolder privateRepository;

    //-------------------------------------------------------------------------

    public void process( final IMSession session, final Object context )
        throws Exception
    {

        IMClientSession clientSession = (IMClientSession) session;
        String type = ( (IMIq) context ).getType();

        // GET
        if ( IMIq.TYPE_GET.equals( type ) )
        {
            get( clientSession, context );
        }
        else if ( IMIq.TYPE_SET.equals( type ) )
        {
            set( clientSession, context );
        }
    }

    //-------------------------------------------------------------------------
    private void get( final IMClientSession session, Object context )
        throws Exception
    {

        final XmlPullParser xpp = session.getXmlPullParser();
        //final String privateName = xpp.getNamespace()+':'+xpp.getName();

        String iqId = ( (IMIq) context ).getId();

        xpp.next();
        String privateKey = xpp.getNamespace() + ':' + xpp.getName();
        String data = privateRepository.getData( session.getUser().getName(), privateKey );
        if ( data == null )
        {
            data = "<" + xpp.getName() + " xmlns='" + xpp.getNamespace() + "'/>";
        }

        getLogger().debug( "Got data (" + privateKey + "): " + data );
        String s = "<iq type='result'";
        s += " from='" + session.getUser().getJIDAndRessource() + "'";
        s += " to='" + session.getUser().getJIDAndRessource() + "'";
        s += " id='" + iqId + "'>";
        s += "<query xmlns='jabber:iq:private'>";
        s += data;
        s += "</query>";
        s += "</iq>";

        session.writeOutputStream( s );

        skip( xpp );
        /*
         while( !( eventType == XmlPullParser.END_TAG 
         && privateName.equals( xpp.getNamespace()+':'+xpp.getName() ) ) ){
         eventType = xpp.next();
         }
         */
    }

    //-------------------------------------------------------------------------
    private void set( final IMClientSession session, final Object context )
        throws Exception
    {

        final XmlPullParser xpp = session.getXmlPullParser();

        //int eventType = xpp.next();
        String privateKey = xpp.getNamespace() + ':' + xpp.getName();

        String data = serialize( xpp ).toString();

        getLogger().debug( "Got private key " + privateKey + " => data: " + data );
        if ( data != null && data.length() > 0 )
        {
            privateRepository.setData( session.getUser().getName(), privateKey, data );
        }

        String iqId = ( (IMIq) context ).getId();

        String s = "<iq type='result'";
        s += " from='" + session.getUser().getJIDAndRessource() + "'";
        s += " to='" + session.getUser().getJIDAndRessource() + "'";
        s += " id='" + iqId + "'/>";

    }

}
