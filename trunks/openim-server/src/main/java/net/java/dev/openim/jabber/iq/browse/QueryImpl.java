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
package net.java.dev.openim.jabber.iq.browse;





import net.java.dev.openim.DefaultSessionProcessor;
import net.java.dev.openim.ServerParameters;

import net.java.dev.openim.data.jabber.IMIq;
import net.java.dev.openim.session.IMClientSession;
import net.java.dev.openim.session.IMSession;






/**
 *  @version 1.5
 * @author AlAg
 */
public class QueryImpl extends DefaultSessionProcessor implements Query {

    private ServerParameters serverParameters;    

    //-------------------------------------------------------------------------
    public void process( final IMSession session, final Object context ) throws Exception{
        
        IMClientSession clientSession = (IMClientSession)session;
        String type = ((IMIq)context).getType();

        // GET
        if( IMIq.TYPE_GET.equals( type ) ){
            get( clientSession, context );
        }
        else if( IMIq.TYPE_SET.equals( type ) ){
            set( clientSession, context );
        }        
    }
    
    
    //-------------------------------------------------------------------------
    private void get( final IMClientSession session, Object context ) throws Exception {
        
        
        //final XmlPullParser xpp = session.getXmlPullParser();

        String iqId = ((IMIq)context).getId();
        
        String s = "<iq type='result'";
        s += " from='"+serverParameters.getHostName()+"'";
        s += " to='"+session.getUser().getJIDAndRessource()+"'";
        s += " id='"+iqId+"'";
        s += ">";
        s += "<service jid='"+serverParameters.getHostName()+"' name='OpenIM Server' type='jabber' xmlns='jabber:iq:browse'>"; 
        s += "<item category='service' jid='"+serverParameters.getHostName()+"' name='OpenIM User Directory' type='jud'>";
        s += "<ns>jabber:iq:register</ns>";
        s += "</item>";
        s += "</service></iq>";

        session.writeOutputStream( s );
    }
    
    //-------------------------------------------------------------------------
    private void set( final IMClientSession session, final Object context ) throws Exception {

        //final XmlPullParser xpp = session.getXmlPullParser();
        getLogger().warn( "Skipping jabber:iq:browse:query set" );  
    }
    
}

