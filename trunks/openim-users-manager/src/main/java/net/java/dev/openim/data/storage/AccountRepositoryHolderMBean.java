/*
 * BSD License http://open-im.net/bsd-license.html
 * Copyright (c) 2003, OpenIM Project http://open-im.net
 * All rights reserved.
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation. For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 */
package net.java.dev.openim.data.storage;

import java.util.List;

import net.java.dev.openim.data.Account;


/*
 * @phoenix:mx-topic name="AccountRepositoryHolder"
 */
public interface AccountRepositoryHolderMBean
{
    public void setAccount(String accountStr);
    public List<Account> getAccountList();
    
}