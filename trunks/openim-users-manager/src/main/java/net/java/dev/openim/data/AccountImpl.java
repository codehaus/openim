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

import java.io.Serializable;

import net.java.dev.openim.tools.Digester;

/**
 * @version 1.5
 * @author AlAg
 */
public class AccountImpl
    implements Serializable, Account
{

    private static final long serialVersionUID = 15L;

    private String name;
    private String password;

    
    
    public final void setName( String name )
    {
        this.name = name;
    }

    public final String getName()
    {
        return name;
    }

    public final void setPassword( String password )
    {
        this.password = password;
    }

    public final String getPassword()
    {
        return password;
    }

    // -----------------------------------------------------------------------
    public boolean isAuthenticationTypeSupported( int type )
    {
        boolean isSupported = false;
        if ( type == Account.AUTH_TYPE_DIGEST || type == Account.AUTH_TYPE_PLAIN )
        {
            isSupported = true;
        }
        return isSupported;
    }

    // -----------------------------------------------------------------------
    public final void authenticate( int type, String value, String sessionId )
        throws Exception
    {
        if ( type == AUTH_TYPE_PLAIN )
        {
            if ( !password.equals( value ) )
            {
                throw new Exception( "Unvalid plain password" );
            }

        }
        else if ( type == AUTH_TYPE_DIGEST )
        {
            String digest = Digester.digest( sessionId + password );

            if ( !digest.equals( value ) )
            {
                throw new Exception( "Unvalid digest password" + " \nGot   : " + value + "\nExpect: " + digest );
            }

        }
    }

    // -----------------------------------------------------------------------
    public String toString()
    {
        String s = "Username: " + name + " password: " + password;
        return s;
    }

}
