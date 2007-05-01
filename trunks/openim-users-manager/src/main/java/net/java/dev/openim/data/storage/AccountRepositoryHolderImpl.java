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
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.codehaus.plexus.logging.AbstractLogEnabled;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;

import net.java.dev.openim.data.Account;
import net.java.dev.openim.data.AccountImpl;

/**
 * @version 1.5
 * @author AlAg
 */
public class AccountRepositoryHolderImpl
    extends AbstractLogEnabled
    implements AccountRepositoryHolder, AccountRepositoryHolderMBean, Initializable
{

    private Map<String, Account> accountRepository;

    private File storeFile;

    private XStream xstream;

    // Configuration
    private String filename;
    private boolean regexpSearch;

    //--------------------------------------------------------------------------
    @SuppressWarnings("unchecked")
    public void initialize()
        throws InitializationException
    {

        storeFile = new File( filename );
        if ( !storeFile.exists() )
        {
            storeFile.getParentFile().mkdirs();
        }

        xstream = new XStream( new DomDriver() );
        xstream.alias( "account", AccountImpl.class );
        accountRepository = loadMap( storeFile );
    }

    //--------------------------------------------------------------------------
    public Account getAccount( String username )
    {
        Account account = null;
        try
        {
            if ( username != null && username.length() > 0 )
            {
                synchronized ( accountRepository )
                {
                    if ( accountRepository.containsKey( username ) )
                    {
                        account = (AccountImpl) accountRepository.get( username );
                    }
                }
            }
        }

        catch ( Exception e )
        {
            getLogger().error( e.getMessage(), e );
            account = null;
        }

        if ( account == null )
        {
            getLogger().warn( "User " + username + " not found" );
        }
        return account;
    }

    //--------------------------------------------------------------------------
    public List<Account> getAccountList( String searchPattern )
    {
        List<Account> list = new ArrayList<Account>();
        if ( !regexpSearch )
        {
            searchPattern = searchPattern.replaceAll( "\\*", ".*" );
        }

        try
        {
            synchronized ( accountRepository )
            {
                Iterator iter = accountRepository.values().iterator();
                while ( iter.hasNext() )
                {
                    String name = iter.next().toString();
                    if ( name.matches( searchPattern ) )
                    {
                        Account account = (Account) accountRepository.get( name );
                        list.add( account );
                    }
                }
            }
        }
        catch ( Exception e )
        {
            getLogger().warn( e.getMessage(), e );
        }

        return list;
    }

    //--------------------------------------------------------------------------
    public Account removeAccount( String username )
    {
        Account account = null;
        synchronized ( accountRepository )
        {
            account = (Account) accountRepository.remove( username );
            if ( account != null )
            {
                saveMap( storeFile, accountRepository );
            }
            else
            {
                getLogger().warn( "User " + username + " not found" );
            }
        }

        return account;
    }

    //--------------------------------------------------------------------------
    public void setAccount( Account userAccount )
    {
        AccountImpl account = new AccountImpl();

        account.setName( userAccount.getName() );
        account.setPassword( userAccount.getPassword() );

        getLogger().debug( "Setting account in repository " + account );
        synchronized ( accountRepository )
        {
            accountRepository.put( account.getName(), account );
            saveMap( storeFile, accountRepository );
        }
    }

    //--------------------------------------------------------------------------
    public void setAccount( String accountStr )
    {
        try
        {
            Account account = new AccountImpl();
            int index = accountStr.indexOf( '/' );
            if ( index < 0 )
            {
                account.setName( accountStr );
                account.setPassword( accountStr );
            }
            else
            {
                account.setName( accountStr.substring( 0, index ) );
                account.setPassword( accountStr.substring( index + 1 ) );
            }
            setAccount( account );
        }
        catch ( Exception e )
        {
            getLogger().warn( e.getMessage(), e );
        }
    }

    //--------------------------------------------------------------------------
    public List<Account> getAccountList()
    {
        List<Account> list = new ArrayList<Account>();
        synchronized ( accountRepository )
        {
            Iterator iter = accountRepository.values().iterator();
            while ( iter.hasNext() )
            {
                Account o = (Account) iter.next();
                getLogger().debug( "Item " + o + " account " + getAccount( o.toString() ) );
                list.add( o );
            }
        }
        return list;
    }

    //--------------------------------------------------------------------------
    //--------------------------------------------------------------------------
    //--------------------------------------------------------------------------
    //--------------------------------------------------------------------------
    private void saveMap( File file, Map map )
    {
        String xstreamData = xstream.toXML( map );
        FileOutputStream fos = null;
        try
        {
            fos = new FileOutputStream( file );
            fos.write( xstreamData.getBytes() );
        }
        catch ( IOException e )
        {
            getLogger().error( e.getMessage(), e );
        }
        finally
        {
            if ( fos != null )
            {
                try
                {
                    fos.close();
                }
                catch ( IOException e )
                {
                    getLogger().error( e.getMessage() );
                }
            }
        }

    }

    //--------------------------------------------------------------------------
    private Map loadMap( File file )
    {
        Map map = null;

        if ( file.exists() )
        {
            try
            {
                FileInputStream fis = new FileInputStream( file );
                String xmlData = IOUtils.toString( fis );
                fis.close();
                map = (Map) xstream.fromXML( xmlData );
            }
            catch ( Exception e )
            {
                getLogger().error( e.getMessage(), e );
            }

        }
        else
        {
            getLogger().info( "No " + file + " => starting with void user list" );
            map = new HashMap();
        }

        return map;
    }

}
