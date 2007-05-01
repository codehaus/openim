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
package net.java.dev.openim.tools;


/**
 * @author AlAg
 */
public class JIDParser {
    
    //-------------------------------------------------------------------------
    public static final String getJID( final String jidAndRes ) {
        return getName( jidAndRes )+'@'+getHostname( jidAndRes );
    }
    //-------------------------------------------------------------------------
    public static final String getHostname( final String jidAndRes ) {
        String hostname = null;
        
            int index = jidAndRes.indexOf( '@' );
            if( index > 0 ){
                hostname = jidAndRes.substring( index + 1 );
                index = hostname.indexOf( '/' );
                if( index > 0 ){
                    hostname = hostname.substring( 0, index );
                }
                hostname = hostname.toLowerCase();
            }
        
        return hostname;
    }
    
    //-------------------------------------------------------------------------
    public static final String getNameAndRessource( final String jidAndRes ) {
        String nameAndRes = null;
        
        if( jidAndRes != null ){
            int index = jidAndRes.indexOf( '@' );
            if( index > 0 ){
                nameAndRes = jidAndRes.substring( 0, index ).toLowerCase();
                index = jidAndRes.lastIndexOf( '/' );
                if( index > 0 ){
                    nameAndRes += jidAndRes.substring( index );
                }
            }
        }
        
        return nameAndRes;
    }
    //-------------------------------------------------------------------------
    public static final String getName( final String jidAndRes ) {
        String name = jidAndRes;
    
        if( jidAndRes != null ){

            int index = jidAndRes.lastIndexOf( '/' );
            if( index > 0 ){
                name = jidAndRes.substring( 0, index );
            }

            index = name.indexOf( '@' );
            if( index > 0 ){
                name = name.substring( 0, index );
            } 
            
            if( name != null ){
                name = name.toLowerCase();
            }
        }
        
        return name;
    }

    
    
    
}

