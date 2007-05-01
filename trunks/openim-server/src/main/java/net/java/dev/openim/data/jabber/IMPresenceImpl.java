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
package net.java.dev.openim.data.jabber;

import net.java.dev.openim.tools.XMLToString;

/**
 * @version 1.5
 * @author AlAg
 */
public class IMPresenceImpl implements IMPresence, java.io.Serializable
{
    
    private static final long serialVersionUID = 15L;

    
    private String to;
    private String from;
    
    private String type;
    private String show;
    private String priority;
    private String status;
    
    private String error;
    private Integer errorCode;

    public final void setTo( String to ){
        this.to = to;
    }
    public final String getTo(){
        return to;
    }
    
    public final void setFrom( String from ){
        this.from = from;
    }
    public final String getFrom(){
        return from;
    }

    public final void setType( String type ){
        this.type = type;
    }
    public final String getType(){
        return type;
    }
    
    public final void setShow( String show ){
        this.show = show;
    }
    public final String getShow(){
        return show;
    }
    
    public final void setPriority( String priority ){
        this.priority = priority;
    }
    public final String getPriority(){
        return priority;
    }
    
    public final void setStatus( String status ){
        this.status = status;
    }
    public final String getStatus(){
        return status;
    }
    

    public final void setError( String error ){
        this.error = error;
    }
    
    public void setErrorCode( int errorCode ) {
        this.errorCode = new Integer( errorCode );
    }

    
    public Object clone(){
        IMPresenceImpl clone = new IMPresenceImpl();

        clone.to = to;
        clone.from = from;
        clone.type = type;
        clone.show = show;
        clone.priority = priority;
        clone.status = status;
        clone.error = error;
        clone.errorCode = errorCode;
        
        return clone;
    }
    
    public String toString(){
        
        XMLToString presence = new XMLToString( "presence" );
        presence.addFilledAttribut( "to", this.to );
        presence.addFilledAttribut( "from", this.from );
        presence.addFilledAttribut( "type", this.type );


        if( priority != null ){
            XMLToString priority = new XMLToString( "priority" );
            priority.addTextNode( this.priority );
            presence.addElement( priority );
        }
        
        
        if( show != null ){
            XMLToString show = new XMLToString( "show" );
            show.addTextNode( this.show );
            presence.addElement( show );
        }
        
        if( status != null ){
            XMLToString status = new XMLToString( "status" );
            status.addTextNode( this.status );
            presence.addElement( status );
        }
        
        if( error != null ){
            XMLToString error = new XMLToString( "error" );
            error.addTextNode( this.error );
            if( errorCode != null ){
                error.addFilledAttribut( "code", errorCode.toString() );
            }
            presence.addElement( error );
        }        

        return presence.toString();
    }
    
    
}

