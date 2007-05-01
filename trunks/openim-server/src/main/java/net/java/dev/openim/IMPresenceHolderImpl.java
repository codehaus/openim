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
package net.java.dev.openim;

import java.util.Map;
import java.util.HashMap;
import java.util.Collection;

import org.codehaus.plexus.logging.AbstractLogEnabled;

import net.java.dev.openim.data.jabber.IMPresence;
import net.java.dev.openim.tools.JIDParser;

/**
 * @version 1.5
 * @author AlAg
 */
public class IMPresenceHolderImpl
    extends AbstractLogEnabled
    implements IMPresenceHolder
{

    Map<String, Map<String, IMPresence>> presenceMap = new HashMap<String, Map<String, IMPresence>>();

    public void setPresence( String jid, IMPresence presence )
    {
        synchronized ( presenceMap )
        {
            String name = JIDParser.getName( jid );
            Map<String, IMPresence> map = presenceMap.get( name );
            if ( map == null )
            {
                map = new HashMap<String, IMPresence>();
            }
            map.put( jid, presence );
            presenceMap.put( name, map );
        }
    }

    public Collection<IMPresence> getPresence( String jid )
    {
        Collection<IMPresence> col = null;
        synchronized ( presenceMap )
        {
            String name = JIDParser.getName( jid );
            Map<String, IMPresence> map = presenceMap.get( name );
            if ( map != null )
            {
                col = map.values();
            }
        }
        return col;
    }

    public IMPresence removePresence( String jid )
    {
        IMPresence presence = null;
        synchronized ( presenceMap )
        {
            String name = JIDParser.getName( jid );
            Map<String, IMPresence> map = presenceMap.get( name );
            if ( map != null )
            {
                presence = map.remove( jid );
                if ( map.isEmpty() )
                {
                    presenceMap.remove( map );
                }
            }
        }
        return presence;
    }

}
