/*
 * Created on Jun 27, 2005
 */
package com.trilogy.app.crm.web.service;

import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.AccountHome;
import com.trilogy.app.crm.bean.MsisdnStateEnum;
import com.trilogy.app.crm.bean.PackageStateEnum;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberHome;
import com.trilogy.app.crm.numbermgn.MsisdnManagement;
import com.trilogy.app.crm.support.AccountSupport;
import com.trilogy.app.crm.support.MsisdnSupport;
import com.trilogy.app.crm.support.PackageSupportHelper;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.visitor.AbortVisitException;
import com.trilogy.framework.xhome.visitor.Visitor;
import com.trilogy.framework.xlog.log.LogSupport;

public class SubscriberDeletingVisitor implements Visitor
{
    private long processed = 0;
    private long count = 0;

    private StringBuilder errorBuffer = new StringBuilder();

    public long getProcessed()
    {
        return processed;
    }
    
    public long getCount()
    {
        return count;
    }

    public String getErrors()
    {
        return errorBuffer.toString();
    }

    public void setProcessed(long processed)
    {
        this.processed = processed;
    }

    public void visit(Context ctx, Object obj) throws AgentException,
            AbortVisitException
    {
        Home subHome = (Home) ctx.get(SubscriberHome.class);
        Home accHome = (Home) ctx.get(AccountHome.class);
        Subscriber sub = (Subscriber) obj;
        String msisdn = sub.getMSISDN();
        String pkgId = sub.getPackageId();
        String ban = sub.getBAN();
        String subId = sub.getId();
        boolean error = false;
        
        count++;
        
        try
        {

            MsisdnManagement.deassociateMsisdnWithSubscription(ctx, msisdn, subId, "voiceMsisdn");
            MsisdnManagement.releaseMsisdn(ctx, msisdn, ban, "CRM - SubscriberDeletingVisitor");
        }
        catch (Exception e)
        {
            String msg = "\nFailed to put msisdn " + msisdn
                    + " to available state for subscriber " + subId;
            LogSupport.debug(ctx, this, msg, e);
            errorBuffer.append(msg);
            error = true;
        }

        try
        {
            PackageSupportHelper.get(ctx).setPackageState(ctx, sub.getPackageId(), sub.getTechnology(),
                    PackageStateEnum.AVAILABLE_INDEX, sub.getSpid());

        }
        catch (Exception e)
        {
            String msg = "\nFailed to put package " + pkgId
                    + " to available state for subscriber " + subId;
            LogSupport.debug(ctx, this, msg, e);
            errorBuffer.append(msg);
            error = true;
        }

        try
        {
            subHome.remove(ctx, sub);
            try
            {
                // remove the account after the subscriber
                Account acc = AccountSupport.getAccount(ctx, ban);
                accHome.remove(ctx, acc);

            }
            catch (Exception e)
            {
                String msg = "\nFailed to delete Account " + ban
                        + " for subscriber " + subId;
                LogSupport.debug(ctx, this, msg, e);
                errorBuffer.append(msg);
                error = true;
            }
        }
        catch (Exception e)
        {
            String msg = "\nFailed to delete subscriber " + sub.getId();
            LogSupport.debug(ctx, this, msg, e);
            errorBuffer.append(msg);
            error = true;
        }

        
        if(!error)
        {
        	processed++;
        }
    }
}
