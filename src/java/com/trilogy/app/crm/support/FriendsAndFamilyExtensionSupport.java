/*
 * Copyright (c) 2007, REDKNEE.com. All Rights Reserved.
 * 
 * This software is the confidential and proprietary information of REDKNEE.com.
 * ("Confidential Information"). You shall not disclose such Confidential Information and
 * shall use it only in accordance with the terms of the license agreement you entered
 * into with REDKNEE.com.
 * 
 * REDKNEE.COM MAKES NO REPRESENTATIONS OR WARRANTIES ABOUT THE SUITABILITY OF THE
 * SOFTWARE, EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE, OR NON-INFRINGEMENT.
 * REDKNEE.COM SHALL NOT BE LIABLE FOR ANY DAMAGES SUFFERED BY LICENSEE AS A RESULT OF
 * USING, MODIFYING OR DISTRIBUTING THIS SOFTWARE OR ITS DERIVATIVES.
 */
package com.trilogy.app.crm.support;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.locks.ReentrantLock;

import com.trilogy.app.crm.account.AccountConstants;
import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.AccountHome;
import com.trilogy.app.crm.bean.ClosedUserGroup;
import com.trilogy.app.crm.extension.ExtensionInstallationException;
import com.trilogy.app.crm.extension.account.AccountExtensionHolder;
import com.trilogy.app.crm.extension.account.FriendsAndFamilyExtension;
import com.trilogy.app.crm.extension.account.FriendsAndFamilyExtensionHome;
import com.trilogy.app.crm.extension.account.FriendsAndFamilyExtensionXInfo;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.xgen.tmpl.Exception;
import com.trilogy.framework.xlog.log.LogSupport;


/**
 * @author bpandey
 * 
 */
public class FriendsAndFamilyExtensionSupport
{

    private static Map<String, ReentrantLock> lockMap_ = new WeakHashMap<String, ReentrantLock>();


    public static synchronized ReentrantLock getLockOnBan(String ban)
    {
        ReentrantLock lock = lockMap_.get(ban);
        if (lock == null)
        {
            lock = new ReentrantLock();
            lockMap_.put(ban, lock);
        }
        return lock;
    }


    public static synchronized void clearLockOnBan(String ban, ReentrantLock lock)
    {
        if (!lock.hasQueuedThreads())
        {
            lockMap_.remove(ban);
        }
    }

    
    public static void removeFnfExtensionFromAccount(Context ctx, Account account, long cugId) throws HomeException
    {
        ClosedUserGroup cug = null;
        FriendsAndFamilyExtension ext = null;
        
        Home fnfExtHome = (Home)ctx.get(FriendsAndFamilyExtensionHome.class);
        if( fnfExtHome != null )
        {
            ext = (FriendsAndFamilyExtension)fnfExtHome.find(ctx, account.getBAN());
        }
        Collection<AccountExtensionHolder> extensions = account.getAccountExtensions();
        Iterator<AccountExtensionHolder> iter = extensions.iterator();
        while (iter.hasNext())
        {
            AccountExtensionHolder extHolder = iter.next();
            if (extHolder.getExtension() instanceof FriendsAndFamilyExtension)
            {
                iter.remove();
                fnfExtHome.remove(ctx, ext);
                break;
            }
        }
        if (LogSupport.isDebugEnabled(ctx))
        {
            LogSupport.debug(ctx, FriendsAndFamilyExtensionSupport.class.getName(),
                    "Removing Fnf extension from account : " + account.getBAN());
        }
        HomeSupportHelper.get(ctx).storeBean(ctx, account);
        LogSupport.info(ctx, FriendsAndFamilyExtensionSupport.class.getName(), "Removed Fnf extension from account : "
                + account.getBAN());
        StringBuilder noteMessage = new StringBuilder();
        noteMessage.append("Friends and Family Account extension removed CUG {");
        noteMessage.append(cugId);
        noteMessage.append("}");
        ClosedUserGroupSupport.addAccountCugNote(ctx, account, noteMessage.toString());
    }


    public static FriendsAndFamilyExtension addFnfExtensionToAccount(Context ctx, Account account, long cugTemplateId,
            String ownerMsisdn) throws HomeException
    {
        if (LogSupport.isDebugEnabled(ctx))
        {
            LogSupport.debug(ctx, FriendsAndFamilyExtensionSupport.class.getName(),
                    "Adding Fnf extension to account : " + account.getBAN() + " cugtemplateId : " + cugTemplateId
                            + " ownerMsidn : " + ownerMsisdn);
        }
        
        Context subCtx = ctx.createSubContext();
        
        FriendsAndFamilyExtension fnfExtension = new FriendsAndFamilyExtension();
        fnfExtension.setCugTemplateID(cugTemplateId);
        fnfExtension.setBAN(account.getBAN());
        fnfExtension.setSpid(account.getSpid());
        fnfExtension.setCugOwnerMsisdn(ownerMsisdn);
        Collection<AccountExtensionHolder> extensions = account.getAccountExtensions();
        AccountExtensionHolder actExtHolder = new AccountExtensionHolder();
        actExtHolder.setExtension(fnfExtension);
        extensions.add(actExtHolder);
        
        Context appCtx = (Context) ctx.get("app");
        appCtx.put("newPricePlanChange", true);
        HomeSupportHelper.get(ctx).createBean(ctx, fnfExtension);
                
        LogSupport.info(subCtx, FriendsAndFamilyExtensionSupport.class.getName(), "Added Fnf extension to account : "
                + account.getBAN() + " cugId : " + fnfExtension.getCugID());
        // Create Note message for FNF creation
        StringBuilder noteMessage = new StringBuilder();
        noteMessage.append("Account extension created with CUG {");
        noteMessage.append(fnfExtension.getCugID());
        noteMessage.append("}");
        if (ownerMsisdn != null && ownerMsisdn.length() > 0)
        {
            noteMessage.append(", {");
            noteMessage.append(ownerMsisdn);
            noteMessage.append("} added to account CUG");
        }
        ClosedUserGroupSupport.addAccountCugNote(subCtx, account, noteMessage.toString());
        return fnfExtension;
    }


    public static FriendsAndFamilyExtension getFnfExtension(final Context ctx, final String ban) throws HomeException
    {
        return HomeSupportHelper.get(ctx).findBean(ctx, FriendsAndFamilyExtension.class,
                new EQ(FriendsAndFamilyExtensionXInfo.BAN, ban));
    }
}
