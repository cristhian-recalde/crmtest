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
package com.trilogy.app.crm.web.control;

import java.io.PrintWriter;
import java.util.Collection;

import com.trilogy.app.crm.account.AccountConstants;
import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.AccountCategoryHome;
import com.trilogy.app.crm.bean.AccountCategoryXInfo;
import com.trilogy.app.crm.bean.AccountXInfo;
import com.trilogy.app.crm.bean.CustomerTypeEnum;
import com.trilogy.app.crm.bean.SubscriberTypeEnum;
import com.trilogy.app.crm.bean.core.AccountCategory;
import com.trilogy.app.crm.home.AccountFamilyPlanHome;
import com.trilogy.app.crm.support.AccountSupport;
import com.trilogy.app.crm.support.AccountTypeSupport;
import com.trilogy.app.crm.support.WebControlSupportHelper;
import com.trilogy.framework.xhome.beans.XBeans;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextLocator;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.webcontrol.ProxyWebControl;
import com.trilogy.framework.xhome.webcontrol.WebControl;
import com.trilogy.framework.xlog.log.LogSupport;

/**
 * @author ankit.nagpal@redknee.com
 *
 * Performances view customization for account web control.
 */
public class AccountWebControlProxy extends ProxyWebControl
{
    public AccountWebControlProxy()
    {
        this((WebControl)XBeans.getInstanceOf(ContextLocator.locate(), Account.class, WebControl.class));
    }

    /**
     * @param delegate
     */
    public AccountWebControlProxy(WebControl delegate) {
        super(delegate);
    }
    
    @Override
    public void toWeb(Context ctx_, PrintWriter p1, String p2, Object p3)  
    {
        Context subCtx = ctx_.createSubContext();
        Account account = (Account) p3;
        
        Collection collAcct =null;
        
        Account parentAccount;
        try
        {
            parentAccount = account.getParentAccount(ctx_);
            
            Home home = (Home) ctx_.get(AccountCategoryHome.class);
            if (parentAccount != null)
            {
                AccountCategory accountCategory = (AccountCategory) home.find(ctx_, new EQ(
                        AccountCategoryXInfo.IDENTIFIER, parentAccount.getType()));
                if (parentAccount.isPooled(ctx_) && accountCategory != null
                        && accountCategory.getCustomerType().equals(CustomerTypeEnum.FAMILY))
                {
                    try
                    {
                    	//TTOTST-754:fixed BSS:parent a/c Group Pooled, and a/c type Family and current a/c not active Billing Type Mismatch of Account from Source to Destination
                    	collAcct = AccountSupport.getImmediateChildrenAccounts(subCtx, parentAccount.getBAN());
                    }
                    catch (final HomeException exception)
                    {
                        LogSupport.minor(subCtx, AccountWebControlProxy.class, "Not able to find sub accounts",
                                exception);
                    }
                    if (collAcct==null || collAcct.size() == 0)
                    {
                        account.setSystemType(SubscriberTypeEnum.POSTPAID);
                        WebControlSupportHelper.get(subCtx).setPropertyReadOnly(subCtx, AccountXInfo.SYSTEM_TYPE);
                    }
                }
            }
        }
        catch (HomeException e)
        {
            LogSupport.minor(subCtx, AccountWebControlProxy.class, "Not able to find parent account", e);
        }
        super.toWeb(subCtx, p1, p2, p3);
    }
}
