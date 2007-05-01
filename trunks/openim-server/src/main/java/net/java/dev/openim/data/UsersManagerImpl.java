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
package net.java.dev.openim.data;

import org.codehaus.plexus.logging.AbstractLogEnabled;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.ServiceLocator;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Serviceable;

import net.java.dev.openim.data.jabber.User;

/**
 * @version 1.5
 * @author AlAg
 */
public class UsersManagerImpl
    extends AbstractLogEnabled
    implements UsersManager, Serviceable
{

    private ServiceLocator serviceLocator;

    public void service( ServiceLocator serviceLocator )
    {
        this.serviceLocator = serviceLocator;

    }

    //-------------------------------------------------------------------------
    public User getNewUser()
        throws Exception
    {
        return (User) serviceLocator.lookup( User.class.getName(), "User" );
    }

}
