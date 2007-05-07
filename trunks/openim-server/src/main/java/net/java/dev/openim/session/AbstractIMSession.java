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

import java.net.Socket;

import java.io.InputStreamReader;
import java.io.DataInputStream;
import java.io.OutputStreamWriter;
import java.io.DataOutputStream;
import java.io.IOException;


import org.codehaus.plexus.logging.AbstractLogEnabled;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import net.java.dev.openim.IMRouter;
import net.java.dev.openim.jabber.Streams;

//import net.java.dev.openim.tools.InputStreamDebugger;

public abstract class AbstractIMSession
    extends AbstractLogEnabled
    implements IMSession
{

    protected String defaultEncoding;

    protected OutputStreamWriter outputStreamWriter;
    private XmlPullParser xpp;
    protected Socket socket;
    private String encoding;
    protected IMRouter router;
    protected volatile Boolean disposed;
    protected long sessionId;
    protected static Long lastSessionId = new Long( System.currentTimeMillis() );
    protected Streams streams;

    //-------------------------------------------------------------------------
    public boolean isClosed()
    {
        boolean value = false;
        if ( disposed != null )
        {
            synchronized ( disposed )
            {
                value = disposed.booleanValue();
            }
        }
        return value;
    }

    //-------------------------------------------------------------------------
    public void setup( final Socket socket )
        throws Exception
    {
        this.socket = socket;

        XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
        factory.setNamespaceAware( true );
        xpp = factory.newPullParser();

        // to be checked -- getInputEncoding should detect encoding (if parser impl do so)
        /*m_encoding = m_xpp.getInputEncoding();
         if( m_encoding == null || m_encoding.length() == 0 ){
         m_encoding = m_defaultEncoding;
         }
         */

        //DataInputStream is = new DataInputStream( new net.java.dev.openim.tools.InputStreamDebugger( socket.getInputStream(), getLogger() ) );
        DataInputStream is = new DataInputStream( socket.getInputStream() );

        InputStreamReader inputStreamReader = new InputStreamReader( is, defaultEncoding );
        //InputStreamReader inputStreamReader = new InputStreamReader( new net.java.dev.openim.tools.InputStreamDebugger( is, getLogger(), m_sessionId ) , m_defaultEncoding );

        xpp.setInput( inputStreamReader );

        DataOutputStream os = new DataOutputStream( socket.getOutputStream() );
        outputStreamWriter = new OutputStreamWriter( os, defaultEncoding );

        getLogger().debug( "Starting session: " + sessionId + " with encoding " + encoding );

    }

    //-------------------------------------------------------------------------
    public final XmlPullParser getXmlPullParser()
    {
        return xpp;
    }

    //-------------------------------------------------------------------------
    /*
     public final OutputStream getOutputStream() {
     return m_outputStream;
     }    
     */

    //-------------------------------------------------------------------------
    public final long getId()
    {
        return sessionId;
    }

    //-------------------------------------------------------------------------

    public final String getEncoding()
    {
        return defaultEncoding;
    }

    //-------------------------------------------------------------------------
    public final void writeOutputStream( final String s )
        //throws IOException
    {
        getLogger().debug( "Output (" + sessionId + "/" + getConnectionType() + "): " + s );
        if ( s != null && outputStreamWriter != null )
        {
            if ( !socket.isClosed() && socket.isConnected() )
            {
                try
                {
                synchronized ( outputStreamWriter )
                {
                    outputStreamWriter.write( s );

                    outputStreamWriter.flush();
                }
                }
                catch( IOException e )
                {
                    getLogger().warn( "Unable to send data: " + e.getMessage() );
                }
                
            }
            else
            {
                getLogger().warn( "Unable to send data: Output socket closed or not connected" );
                //throw new IOException( "Output socket closed or not connected" );
            }
        }
    }

    //-------------------------------------------------------------------------
    public final IMRouter getRouter()
    {
        return router;
    }

    //-------------------------------------------------------------------------
    public final void setRouter( IMRouter router )
    {
        this.router = router;
    }

    public void setStreams( Streams streams )
    {
        this.streams = streams;
    }

    public Streams getStreams()
    {
        return streams;
    }

    //-------------------------------------------------------------------------
    public final String toString()
    {
        return "I: " + getId();
    }

    //-------------------------------------------------------------------------
    public final int hashCode()
    {
        Long sessionL = new Long( sessionId );
        return sessionL.hashCode();
    }

    //-------------------------------------------------------------------------
    // implementer to make PMD happy ,)
    public boolean equals( final Object obj )
    {
        boolean result = false;
        if ( obj instanceof IMSession )
        {
            result = obj == this;
        }
        return result;
    }

}
