package net.java.dev.openim;

import java.io.File;
import java.util.Collection;

import org.codehaus.plexus.PlexusTestCase;
import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.ChatManager;
import org.jivesoftware.smack.MessageListener;
import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.RosterListener;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Presence;

public class TestSimpleClient
    extends PlexusTestCase
{

    protected void setUp()
        throws Exception
    {
        super.setUp();
    }

    private void removeAllStorages()
    {
        File storages = new File( "./target/storages" );
        File[] files = storages.listFiles();
        if ( files != null )
            for ( File file : files )
            {
                file.delete();
            }
    }

    public void testClient()
        throws Exception
    {
        removeAllStorages();

        lookup( IMServer.class.getName(), "IMServer" );

        XMPPConnection conn1 = new XMPPConnection( "localhost" );
        conn1.connect();
        conn1.getAccountManager().createAccount( "user1", "pass1" );
        conn1.login( "user1", "pass1" );
        
        XMPPConnection conn2 = new XMPPConnection( "localhost" );
        conn2.connect();
        conn2.getAccountManager().createAccount( "user2", "pass2" );
        conn2.login( "user2", "pass2" );

        
        Roster roster1 = conn1.getRoster();
        roster1.addRosterListener( new RosterListener(){

            public void entriesAdded( Collection<String> entries )
            {
                assertTrue( entries.contains( "user2@localhost" ) || entries.contains( "user1@localhost" ) );
            }

            public void entriesDeleted( Collection<String> entries )
            {
            }

            public void entriesUpdated( Collection<String> entries )
            {
            }

            public void presenceChanged( Presence presence )
            {
            }            
        });
        roster1.createEntry( "user2@localhost", "User2", new String[]{"Buddy"} );
        
        
        
        Roster roster2 = conn1.getRoster();
        roster2.createEntry( "user1@localhost", "User1", new String[]{"Buddy"} );

        
        
        
        
        
        
        // chat
        ChatManager chatmanager1 = conn1.getChatManager();
        Chat chat1 = chatmanager1.createChat( "user2@localhost", null );
        chat1.sendMessage( "Test" );

        
        ChatManager chatmanager2 = conn2.getChatManager();
        chatmanager2.createChat( "user1@localhost", new MessageListener()
        {
            public void processMessage( Chat chat, Message message )
            {
                assertEquals( "Test", message.getBody() );
            }
        } );
        
        
        
        conn1.disconnect();
        conn2.disconnect();
        
        System.out.println( "End testClient" );

        
        
    }

}
