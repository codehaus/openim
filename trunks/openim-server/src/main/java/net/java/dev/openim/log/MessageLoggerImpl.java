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
package net.java.dev.openim.log;

import java.util.Date;

import org.codehaus.plexus.logging.AbstractLogEnabled;

import net.java.dev.openim.data.Transitable;
import net.java.dev.openim.data.jabber.IMMessage;


/**
 * @version 1.5
 * @author AlAg
 */

public class MessageLoggerImpl
    extends AbstractLogEnabled
    implements MessageLogger
{

    //-------------------------------------------------------------------------
    public void log( Transitable message )
    {
        if ( getLogger().isInfoEnabled() )
        {
            if ( message instanceof IMMessage )
            {
                IMMessage m = (IMMessage) message;
                getLogger().info( new Date() + " " + m.getFrom() + " " + m.getTo() + " " + m.toString().length() );
            }
        }
    }
}
