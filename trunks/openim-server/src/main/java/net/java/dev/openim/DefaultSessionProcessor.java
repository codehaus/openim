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


import net.java.dev.openim.jabber.Streams;
import net.java.dev.openim.session.IMSession;

import org.codehaus.plexus.logging.AbstractLogEnabled;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.ServiceLocator;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Serviceable;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

/**
 * @author AlAg
 */
public class DefaultSessionProcessor
    extends AbstractLogEnabled
    implements SessionProcessor, Serviceable
{


    //public Map<String, SessionProcessor> sessionProcessorMap;

    protected ServiceLocator serviceLocator;
    public void service( ServiceLocator serviceLocator )
    {
        this.serviceLocator = serviceLocator;
    }

    //-------------------------------------------------------------------------
    public void process( final IMSession session )
        throws Exception
    {
        process( session, null );
    }

    //-------------------------------------------------------------------------
    public void process( final IMSession session, final Object context )
        throws Exception
    {

        final XmlPullParser xpp = session.getXmlPullParser();
        final String currentEventName = getEventName( session, xpp.getNamespace(), xpp.getName() );

        for ( int eventType = xpp.next(); eventType != XmlPullParser.END_DOCUMENT; eventType = xpp.next() )
        {

            if ( eventType == XmlPullParser.START_TAG )
            {
                processStartTag( session, context );
            }
            else if ( eventType == XmlPullParser.TEXT )
            {
                processText( session, context );
            }
            else if ( eventType == XmlPullParser.END_TAG )
            {

                if ( currentEventName.equals( getEventName( session, xpp.getNamespace(), xpp.getName() ) ) )
                {
                    processEndTag( session, context );
                    break;
                }
            }
        } // for
        //getLogger().debug( "END_DOCUMENT" );

    }

    //-------------------------------------------------------------------------
    public void processStartTag( final IMSession session, final Object context )
        throws Exception
    {
        final XmlPullParser xpp = session.getXmlPullParser();
        final String eventName = getEventName( session, xpp.getNamespace(), xpp.getName() );
        getLogger().debug( "[" + session.getId() + "] <" + eventName + ">" );
        SessionProcessor processor = null;
        try
        {
            processor = (SessionProcessor)serviceLocator.lookup( eventName );
        }
        catch (Exception e) 
        {
            getLogger().debug( e.getMessage(), e );
        }
        if ( processor != null )
        {
            //getLogger().debug( "Got processor "+processor+" for " +roleName);
            try
            {
                processor.process( session, context );
            }
            finally
            {
                //m_serviceManager.release( processor );
            }
        }
        else
        {
            getLogger().warn( "No processor for event: " + eventName + " in " + getClass().getName() );
            
            skip( xpp );
        }
    }

    //-------------------------------------------------------------------------
    public void processEndTag( final IMSession session, final Object context )
        throws Exception
    {
        final XmlPullParser xpp = session.getXmlPullParser();
        final String eventName = getEventName( session, xpp.getNamespace(), xpp.getName() );
        getLogger().debug( "[" + session.getId() + "] </" + eventName + ">" );
    }

    //-------------------------------------------------------------------------
    public void processText( final IMSession session, final Object context )
        throws Exception
    {
        final String text = session.getXmlPullParser().getText().trim();
        if ( text.length() > 0 )
        {
            getLogger().debug( "[ " + text + " ]" );
        }
    }

    //-------------------------------------------------------------------------
    protected void skip( final XmlPullParser xpp )
        throws XmlPullParserException, java.io.IOException
    {

        int eventType = xpp.getEventType();

        if ( eventType == XmlPullParser.START_TAG )
        {

            while ( eventType != XmlPullParser.END_TAG )
            {
                eventType = xpp.next();
                if ( eventType == XmlPullParser.START_TAG )
                {
                    skip( xpp );
                }
            }
        }
    }

    //-------------------------------------------------------------------------
    protected StringBuffer serialize( final XmlPullParser xpp )
        throws XmlPullParserException, java.io.IOException
    {

        StringBuffer sb = null;

        int eventType = xpp.getEventType();

        if ( eventType == XmlPullParser.START_TAG )
        {

            sb = getStartElementAsStringBuffer( xpp );
            String elementName = xpp.getName();

            while ( eventType != XmlPullParser.END_TAG )
            {
                eventType = xpp.next();
                if ( eventType == XmlPullParser.START_TAG )
                {
                    sb.append( serialize( xpp ) );
                }
                else if ( eventType == XmlPullParser.TEXT )
                {
                    sb.append( xpp.getText() );
                }
            } // while

            sb.append( "</" ).append( elementName ).append( ">" );

        }

        return sb;
    }

    //-------------------------------------------------------------------------
    protected String asString( final XmlPullParser xpp )
        throws XmlPullParserException
    {
        String s = null;
        int eventType = xpp.getEventType();
        if ( eventType == XmlPullParser.START_TAG )
        {
            s = getStartElementAsStringBuffer( xpp ).toString();
        }
        if ( eventType == XmlPullParser.TEXT )
        {
            s = xpp.getText();
        }
        if ( eventType == XmlPullParser.END_TAG )
        {
            s = "</" + xpp.getName() + ">";
        }
        return s;
    }

    //-------------------------------------------------------------------------
    private StringBuffer getStartElementAsStringBuffer( final XmlPullParser xpp )
    {
        StringBuffer sb = new StringBuffer();

        String elementName = xpp.getName();
        String elementNamespace = xpp.getNamespace();
        // no access to stream and its default namespce

        sb.append( "<" ).append( elementName );
        if ( elementNamespace != null && elementNamespace.length() > 0 )
        {
            sb.append( " xmlns='" ).append( elementNamespace ).append( "'" );
        }
        for ( int i = 0, l = xpp.getAttributeCount(); i < l; i++ )
        {
            String value = xpp.getAttributeValue( i );
            String name = xpp.getAttributeName( i );
            sb.append( " " ).append( name ).append( "='" ).append( value ).append( "'" );
        }
        sb.append( ">" );
        return sb;

    }

    protected String getEventName( final IMSession session, final String currentNamespace, final String name )
    {
        String ns = getNamespace( session, currentNamespace );
        ns = ns != null ? ns : "";
        return ns + ":" + name;
    }

    /**
     * Get namespace, using the Streams namespace if current is null or empty string.
     */
    protected String getNamespace( final IMSession session, String current )
    {
        String ns = current;
        if ( current == null || current.length() == 0 )
        {
            // try get the streams namespace
            Streams s = session.getStreams();
            if ( s != null )
            {
                ns = s.getNamespace();
            } // end of if ()
        } // end of if ()
        return ns;
    }


}
