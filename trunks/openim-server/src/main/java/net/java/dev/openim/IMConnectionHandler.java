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



/**
 * @version 1.5
 * @author AlAg
 */
public interface IMConnectionHandler extends SessionProcessor
{
    public void handleConnection(java.net.Socket socket) throws java.io.IOException, java.net.ProtocolException;
}


