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

import java.util.Collection;

import com.trilogy.framework.xhome.beans.CompoundIllegalStateException;
import com.trilogy.framework.xhome.beans.IllegalPropertyArgumentException;
import com.trilogy.framework.xhome.beans.Validator;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.language.LangHome;
import com.trilogy.framework.xhome.language.LangXInfo;

import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.AccountXInfo;

/**
 * Validates the presence of proper billing language used
 *
 * @author kumaran.sivasubramaniam@redknee.com
 */
public final class AccountBillingLanguageValidator implements Validator
{
    private static final AccountBillingLanguageValidator INSTANCE = new AccountBillingLanguageValidator();

    /**
     * Prevents initialization
     */
    private AccountBillingLanguageValidator()
    {
    }

    public static AccountBillingLanguageValidator instance()
    {
        return INSTANCE;
    }

    public void validate(final Context ctx, final Object obj) throws IllegalStateException
    {
        final com.redknee.app.crm.bean.Account account = (Account) obj;     
        
        if ( ! account.getLanguage().equals("") )
        {
            Home home = (Home) ctx.get(LangHome.class);
            final Object condition = new EQ(LangXInfo.CODE, account.getLanguage());
            
            Collection collection = null;
            try
            {
                collection = home.select(ctx,condition);
            }
            catch (Exception e)
            {
                
            }
            if (collection == null || collection.size() != 1 )
            {
                final CompoundIllegalStateException el = new CompoundIllegalStateException();
                el.thrown(new IllegalPropertyArgumentException(AccountXInfo.LANGUAGE, "Invalid language type."));
                el.throwAll();
            }
        }
    }

}
