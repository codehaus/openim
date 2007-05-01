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
package net.java.dev.openim.jabber.iq.register;

import java.util.Map;

import net.java.dev.openim.DefaultSessionProcessor;
import net.java.dev.openim.session.IMSession;

/**
 * @version 1.5
 * @author AlAg
 */
public class RemoveImpl
    extends DefaultSessionProcessor
    implements Remove
{

    //-------------------------------------------------------------------------
    @SuppressWarnings("unchecked")
    public void process( final IMSession session, final Object context )
        throws Exception
    {
        ( (Map<Integer,Boolean>) context ).put( Query.CTX_SHOULD_REMOVE, Boolean.FALSE );
    }

}
