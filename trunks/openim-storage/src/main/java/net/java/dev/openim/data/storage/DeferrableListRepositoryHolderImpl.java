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
import java.util.List;

import org.codehaus.plexus.logging.AbstractLogEnabled;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;

import net.java.dev.openim.tools.XStreamStore;

/**
 * @version 1.5
 * @author AlAg
 */
public class DeferrableListRepositoryHolderImpl
    extends AbstractLogEnabled
    implements DeferrableListRepositoryHolder, Initializable
{

    // Locals
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
        repository.substitute( "net.java.dev.openim.data.jabber.IMMessage", "message" );
        repository.load();

    }

    
    //--------------------------------------------------------------------------     
    public List getDeferrableList( String username )
    {
        List list = null;
        try
        {
            list = (List) repository.get( username );
        }
        catch ( Exception e )
        {
            getLogger().warn( "User " + username + " message list not found" );
        }
        return list;
    }

    //--------------------------------------------------------------------------    
    public void setDeferrableList( String username, List deferrableList )
    {
        if ( username != null && deferrableList != null )
        {
            repository.put( username, deferrableList );
        }
    }

}
