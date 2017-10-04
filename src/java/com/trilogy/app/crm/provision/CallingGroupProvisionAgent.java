/*
 * This code is a protected work and subject to domestic and international copyright
 * law(s). A complete listing of authors of this work is readily available. Additionally,
 * source code is, by its very nature, confidential information and inextricably contains
 * trade secrets and other information proprietary, valuable and sensitive to Redknee, no
 * unauthorised use, disclosure, manipulation or otherwise is permitted, and may only be
 * used in accordance with the terms of the licence agreement entered into with Redknee
 * Inc. and/or its subsidiaries.
 * 
 * Copyright � Redknee Inc. and its subsidiaries. All Rights Reserved.
 */
package com.trilogy.app.crm.provision;

import java.util.concurrent.locks.ReentrantLock;

import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.CallingGroupTypeEnum;
import com.trilogy.app.crm.bean.ClosedUserGroup;
import com.trilogy.app.crm.bean.Service;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.extension.account.FriendsAndFamilyExtension;
import com.trilogy.app.crm.home.ClosedUserGroupValidator;
import com.trilogy.app.crm.support.ClosedUserGroupSupport;
import com.trilogy.app.crm.support.FriendsAndFamilyExtensionSupport;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xlog.log.LogSupport;


/**
 * 
 */
public class CallingGroupProvisionAgent extends CommonProvisionAgent
{

    static String MODULE = CallingGroupProvisionAgent.class.getName();


    public void execute(Context ctx) throws AgentException
    {
        Account account = getAccount(ctx);
        Service service = getService(ctx);
        Subscriber subscriber = getSubscriber(ctx);
        switch ((int) service.getCallingGroupType())
        {
        case CallingGroupTypeEnum.CUG_INDEX:
            try
            {
                Account rootAccount = account.getRootAccount(ctx);
                if (rootAccount.isIndividual(ctx))
                {
                    throw new AgentException("Calling group service can not be provisioned to individual Account "
                            + rootAccount.getBAN(), null);
                }
                if (LogSupport.isDebugEnabled(ctx))
                {
                    LogSupport.debug(ctx, this, "Provisioniong Calling Group Cug Service : " + service.getID()
                            + " to SubscriberID : " + subscriber.getId());
                }
                provisionCUG(ctx, rootAccount, service.getCugTemplateID(), subscriber.getMsisdn());
            }
            catch (HomeException e)
            {
                LogSupport.minor(ctx, this, "Exception while adding subscriber to Cug", e);
                throw new AgentException("Exception while adding subscriber to CUG, subscriber : "
                        + subscriber.getMsisdn(), e);
            }
            break;
        }
    }


    public static long provisionCUG(Context ctx, Account rootAccount, long cugTemplateID, String msisdn)
            throws HomeException
    {
        if (LogSupport.isDebugEnabled(ctx))
        {
            LogSupport.debug(ctx, MODULE,
                    "Root account of subscription with msisdn : " + msisdn + " is " + rootAccount.getBAN());
        }
        ctx.put(ClosedUserGroupValidator.SKIP_CUG_ACCOUNT_VALIDATION,
                ClosedUserGroupValidator.SKIP_CUG_ACCOUNT_VALIDATION);
        ClosedUserGroup accountCug = ClosedUserGroupSupport.getCug(ctx, rootAccount.getBAN());
        long cugId = 0;
        boolean newFnfExtnCreated = false;
        if (accountCug == null)
        {
            ReentrantLock lock = FriendsAndFamilyExtensionSupport.getLockOnBan(rootAccount.getBAN());
            lock.lock();
            try
            {
                Context appCtx = (Context) ctx.get("app");
                if (appCtx.getBoolean("groupPricePlanChange"))
                {
                    new CallingGroupUnprovisionAgent().execute(ctx);
                }
                ClosedUserGroup accCug = ClosedUserGroupSupport.getCug(ctx, rootAccount.getBAN());
                if (accCug == null)
                {
                    if (LogSupport.isDebugEnabled(ctx))
                    {
                        LogSupport.debug(ctx, MODULE, "Root account : " + rootAccount.getBAN()
                                + " does not have Fnf extension. Creating new Fnf extension with templateId : "
                                + cugTemplateID);
                    }
                    FriendsAndFamilyExtension ext = FriendsAndFamilyExtensionSupport.addFnfExtensionToAccount(ctx,
                            rootAccount, cugTemplateID, msisdn);
                    cugId = ext.getCugID();
                    newFnfExtnCreated = true;
                }
            }
            catch (AgentException e)
            {
                if (LogSupport.isDebugEnabled(ctx))
                {
                    LogSupport.debug(ctx, MODULE, "Unable to unprovision CUG of Ban " + rootAccount.getBAN()
                            + " with templateId : "
                            + cugTemplateID);
                }
            }
            finally
            {
                // thrown exception is caught by the caller
                lock.unlock();
                FriendsAndFamilyExtensionSupport.clearLockOnBan(rootAccount.getBAN(), lock);
            }
        }
        else
        {
            cugId = accountCug.getID();
        }
        if (!newFnfExtnCreated)
        {
            if (LogSupport.isDebugEnabled(ctx))
            {
                LogSupport.debug(ctx, MODULE, "Root Account : " + rootAccount.getBAN()
                        + " already associated with cug : " + accountCug.getID() + ". Adding subscriber : " + msisdn);
            }
            ClosedUserGroupSupport.addSubscriberToCug(ctx, accountCug.getID(), msisdn, false);
            StringBuilder msg = new StringBuilder();
            msg.append("{");
            msg.append(msisdn);
            msg.append("} added to account CUG {");
            msg.append(accountCug.getID());
            msg.append("}");
            ClosedUserGroupSupport.addAccountCugNote(ctx, rootAccount, msg.toString());
        }
        return cugId;
    }
}
