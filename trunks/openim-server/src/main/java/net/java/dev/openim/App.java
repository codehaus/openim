package net.java.dev.openim;

import org.codehaus.plexus.embed.Embedder;

public class App 
{
    
    public void launch()
    throws Exception
    {
        Embedder embedder = new Embedder();
        embedder.start();

        embedder.lookup( IMServer.class.getName(), "IMServer" );
    	
    }
    
}
