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
package net.java.dev.openim.tools;

import java.io.InputStream;
import java.io.IOException;

import org.codehaus.plexus.logging.Logger;

/**
 * @version 1.5
 * @author AlAg
 */
public class InputStreamDebugger
    extends InputStream
{

    private InputStream is;
    private Logger logger;
    private long id;

    public InputStreamDebugger( InputStream is, Logger logger, long id )
    {
        this.is = is;
        this.logger = logger;
        this.id = id;
    }

    public int available()
        throws IOException
    {
        return is.available();
    }

    public void close()
        throws IOException
    {
        is.close();
    }

    public void mark( int readlimit )
    {
        is.mark( readlimit );
    }

    public boolean markSupported()
    {
        return is.markSupported();
    }

    public int read()
        throws IOException
    {
        int b = is.read();
        logger.info( "Input (" + id + "): " + new Character( (char) b ) );
        return b;
    }

    public int read( byte[] b )
        throws IOException
    {
        int i = is.read( b );
        if ( i != -1 )
        {
            logger.info( "Input (" + id + "): " + new String( b, 0, i ) );
        }
        return i;
    }

    public int read( byte[] b, int off, int len )
        throws IOException
    {
        int i = is.read( b, off, len );
        if ( i != -1 )
        {
            logger.info( "Input (" + id + "): " + new String( b, off, i ) );
        }

        return i;
    }

    public void reset()
        throws IOException
    {
        is.reset();
    }

    public long skip( long n )
        throws IOException
    {
        return is.skip( n );
    }
}
