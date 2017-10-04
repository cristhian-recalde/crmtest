/*
 * This code is a protected work and subject to domestic and international
 * copyright law(s).  A complete listing of authors of this work is readily
 * available.  Additionally, source code is, by its very nature, confidential
 * information and inextricably contains trade secrets and other information
 * proprietary, valuable and sensitive to Redknee.  No unauthorized use,
 * disclosure, manipulation or otherwise is permitted, and may only be used
 * in accordance with the terms of the license agreement entered into with
 * Redknee Inc. and/or its subsidiaries.
 *
 * Copyright (c) Redknee Inc. (Migreated for testing purposes) and its subsidiaries. All Rights Reserved.
 */
package com.trilogy.app.crm.home.account;

import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.account.Contact;
import com.trilogy.app.crm.bean.account.ContactXInfo;
import com.trilogy.app.crm.bean.account.SecurityQuestionAnswer;
import com.trilogy.app.crm.bean.account.SecurityQuestionAnswerHome;
import com.trilogy.framework.xhome.beans.CompoundIllegalStateException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.And;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.home.Adapter;
import com.trilogy.framework.xhome.home.AdapterHome;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xlog.log.LogSupport;

/**
 * 
 * Home to update lazyloaded properties of account
 * 
 * @author kumaran.sivasubramaniam
 * @created October 9, 2010
 */

public class AccountLazyLoadedPropertyUpdateHome extends AdapterHome
{

    private static final long serialVersionUID = 1L;
    
    
    public AccountLazyLoadedPropertyUpdateHome(final Context ctx, final Home home)
    {
        super(ctx, home, adapterInstance());
    }

    public static Adapter adapterInstance()
    {
        return adapterLazyLoadInstance_;
    }

    @Override
    public Object create(final Context ctx, final Object obj) throws HomeException
    {
        // DO NOT call super.create(ctx, obj) because in this particular case we have the correct bean with data
        final Account result = (Account) getDelegate(ctx).create(ctx, obj);

        AccountPropertyListeners listener = (AccountPropertyListeners) result.getAccountLazyLoadedPropertyListener();
        listener.checkLazyLoadedPropertiesInfoChangedFromDefault(result);
        listener.saveChangedInfo(ctx, result);

        result.watchLazyLoadedProperitesChange();
        
        return result;
    }

    @Override
    public Object store(final Context ctx, final Object obj) throws HomeException
    {
        // DO NOT call super.store(ctx, obj) because in this particular case we have the correct bean with data
        final Account result = (Account) getDelegate(ctx).store(ctx, obj);

        result.stopLazyLoadedProperitesChange();
        AccountPropertyListeners listener = (AccountPropertyListeners) result.getAccountLazyLoadedPropertyListener();
        listener.saveChangedInfo(ctx, result);
        
        result.watchLazyLoadedProperitesChange();
        
        return result;
    }
    
    


    /**
     * Adapter is used because adapt() will be called for all read methods like find() and select()
     */
    static class AccountLazyLoadedPropertyModificaitonAdapter implements Adapter
    {
        public Object adapt(final Context ctx, final Object obj) throws HomeException
        {
            final Account accountRead = (Account) obj;

           // accountRead.clearContactInfoChange();
            AccountPropertyListeners listener = (AccountPropertyListeners) accountRead.getAccountLazyLoadedPropertyListener();
            listener.clearPropertyInfoChange();
            accountRead.watchLazyLoadedProperitesChange();

            return accountRead;
        }


        public Object unAdapt(final Context ctx, final Object obj) throws HomeException
        {
            return obj;
        }
    }
    
    private static Adapter adapterLazyLoadInstance_ = new AccountLazyLoadedPropertyModificaitonAdapter();
}