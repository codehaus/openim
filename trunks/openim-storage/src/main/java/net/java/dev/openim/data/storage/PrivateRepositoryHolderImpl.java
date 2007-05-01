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
package net.java.dev.openim.data.storage;

import java.io.File;

import org.codehaus.plexus.logging.AbstractLogEnabled;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;

import net.java.dev.openim.tools.XStreamStore;

/**
 * @version 1.5
 * @author AlAg
 */
public class PrivateRepositoryHolderImpl
    extends AbstractLogEnabled
    implements PrivateRepositoryHolder, Initializable
{

    private XStreamStore repository;

    // Configuration
    private String filename;
    private String encoding;

    public void initialize()
        throws InitializationException
    {

        File storeFile = new File( filename );
        if( !storeFile.exists() )
        {
            storeFile.getParentFile().mkdirs();
        }

        repository = new XStreamStore( storeFile, getLogger(), encoding );
        repository.load();
    }

    //--------------------------------------------------------------------------
    public String getData( String username, String key )
    {
        String data = null;
        try
        {
            String repKey = username + "::" + key;
            data = (String) repository.get( repKey );
            getLogger().debug( "Get key: " + repKey + " => " + data );
        }
        catch ( Exception e )
        {
            getLogger().debug( "Username " + username + " dont have private for element " + key );
        }
        return data;
    }

    public void setData( String username, String key, String data )
    {
        String repKey = username + "::" + key;
        getLogger().debug( "Put key: " + repKey + " => " + data );
        repository.put( repKey, data );
    }

}
