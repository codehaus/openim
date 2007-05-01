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
package net.java.dev.openim.jabber.iq.search;

import java.util.List;

import net.java.dev.openim.DefaultSessionProcessor;
import net.java.dev.openim.ServerParameters;

import net.java.dev.openim.data.Account;
import net.java.dev.openim.data.jabber.IMIq;
import net.java.dev.openim.data.storage.AccountRepositoryHolder;
import net.java.dev.openim.session.IMClientSession;
import net.java.dev.openim.session.IMSession;

import org.xmlpull.v1.XmlPullParser;

/**
 * @version 1.5
 * @author AlAg
 */
public class QueryImpl
    extends DefaultSessionProcessor
    implements Query
{

    private ServerParameters serverParameters;

    private AccountRepositoryHolder accountHolder;

    //-------------------------------------------------------------------------
    public void process( final IMSession session, final Object context )
        throws Exception
    {

        IMClientSession clientSession = (IMClientSession) session;
        String type = ( (IMIq) context ).getType();

        // GET
        if ( IMIq.TYPE_GET.equals( type ) )
        {
            get( clientSession, context );
        }
        else if ( IMIq.TYPE_SET.equals( type ) )
        {
            set( clientSession, context );
        }
    }

    //-------------------------------------------------------------------------
    private void get( final IMClientSession session, Object context )
        throws Exception
    {

        String iqId = ( (IMIq) context ).getId();

        String s = "<iq type='result'";
        s += " from='" + serverParameters.getHostName() + "'";
        s += " to='" + session.getUser().getJIDAndRessource() + "'";
        s += " id='" + iqId + "'";
        s += ">";
        s += "<query xmlns='jabber:iq:search'>";
        s += "<nick/>";
        /*
         We will be able to search thru all these when we'll refactor user-manager lib
         s += "<first/>";
         s += "<last/>";
         s += "<email/>";
         */
        s += "<instructions>Fill in one or more fields to search for any matching Jabber users.</instructions>";
        s += "</query></iq>";

        session.writeOutputStream( s );
    }

    //-------------------------------------------------------------------------
    private void set( final IMClientSession session, final Object context )
        throws Exception
    {

        final XmlPullParser xpp = session.getXmlPullParser();
        String iqId = ( (IMIq) context ).getId();

        xpp.nextTag(); // <nick>
        String searchText = xpp.nextText();
        xpp.nextTag(); // </nick>

        getLogger().debug( "Search for account name " + searchText );
        // maybe we should proceed to a search via the vcard...
        List<Account> list = accountHolder.getAccountList( searchText );

        String s = "<iq type='result'";
        s += " from='" + serverParameters.getHostName() + "'";
        s += " to='" + session.getUser().getJIDAndRessource() + "'";
        s += " id='" + iqId + "'";
        s += ">";

        if ( list.size() > 0l )
        {
            s += "<query xmlns='jabber:iq:search'>";

            for ( int i = 0, l = list.size(); i < l; i++ )
            {
                Account account = list.get( i );
                s += "<item jid='" + account.getName() + '@' + serverParameters.getHostName() + "'>";
                s += "<first>" + account.getName() + "</first>";
                s += "<last>" + account.getName() + "</last>";
                s += "<nick>" + account.getName() + "</nick>";
                s += "<email>" + account.getName() + '@' + serverParameters.getHostName() + "</email>";
                s += "</item>";
            }
            s += "</query>";
        }
        else
        {
            s += "<query xmlns='jabber:iq:search'/>";
        }
        s += "</iq>";

        session.writeOutputStream( s );
    }

}
