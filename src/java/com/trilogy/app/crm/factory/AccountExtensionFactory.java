/*
 * This code is a protected work and subject to domestic and international copyright
 * law(s). A complete listing of authors of this work is readily available. Additionally,
 * source code is, by its very nature, confidential information and inextricably contains
 * trade secrets and other information proprietary, valuable and sensitive to Redknee. No
 * unauthorized use, disclosure, manipulation or otherwise is permitted, and may only be
 * used in accordance with the terms of the license agreement entered into with Redknee
 * Inc. and/or its subsidiaries.
 * 
 * Copyright (c) Redknee Inc. (Migreated for testing purposes) and its subsidiaries. All Rights Reserved.
 */
package com.trilogy.app.crm.factory;

import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.extension.account.AccountExtension;
import com.trilogy.app.crm.support.BeanLoaderSupportHelper;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextFactory;
import com.trilogy.framework.xlog.log.MinorLogMsg;


/**
 * 
 * @author simar.singh@redknee.com
 * 
 */
public class AccountExtensionFactory<BEAN extends AccountExtension> implements ContextFactory
{

    public AccountExtensionFactory(Class<BEAN> type)
    {
        this.type_ = type;
    }


    public Object create(Context ctx)
    {
        try
        {
            AccountExtension extension = type_.newInstance();
            Account account = BeanLoaderSupportHelper.get(ctx).getBean(ctx, Account.class);
            if (null != account)
            {
                extension.setSpid(account.getSpid());
                extension.setBAN(account.getBAN());
            }
            return extension;
        }
        catch (Throwable t)
        {
            new MinorLogMsg(this, "Could not create Instance of Extension. Error [" + t.getMessage() + "]", t).log(ctx);
            return null;
        }
    }

    private final Class<BEAN> type_;
}