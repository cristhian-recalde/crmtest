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
package com.trilogy.app.crm.contract;

import java.io.PrintWriter;
import java.util.Date;

import com.trilogy.app.crm.bean.AbstractSubscriber;
import com.trilogy.app.crm.bean.CRMSpid;
import com.trilogy.app.crm.bean.CRMSpidHome;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.support.CalendarSupportHelper;
import com.trilogy.app.crm.support.HomeSupportHelper;
import com.trilogy.app.crm.web.control.KeyWebControlProxy;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.And;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.elang.Or;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.webcontrol.AbstractKeyWebControl;
import com.trilogy.framework.xhome.webcontrol.AbstractWebControl;
import com.trilogy.framework.xhome.webcontrol.KeyWebControlOptionalValue;
import com.trilogy.framework.xlog.log.LogSupport;


/**
 * Custom web control to display all the available contracts
 * 
 */
public class CustomSubscriptionContractKeyWebControl extends KeyWebControlProxy
{

    public static final KeyWebControlOptionalValue DEFAULT = new KeyWebControlOptionalValue("--", "");


    public CustomSubscriptionContractKeyWebControl(final AbstractKeyWebControl keyWebControl)
    {
        super(keyWebControl);
    }


    @Override
    public void toWeb(Context ctx, PrintWriter out, String name, Object value)
    {
        Context subCtx = ctx.createSubContext();
        final Object obj = subCtx.get(AbstractWebControl.BEAN);
        if (obj instanceof Subscriber)
        {
            Subscriber sub = (Subscriber) obj;
            Home home = (Home) subCtx.get(SubscriptionContractTermHome.class);
            Or or = new Or();
            or.add(new EQ(SubscriptionContractTermXInfo.DISABLE, false));
            or.add(new EQ(SubscriptionContractTermXInfo.ID, (Long) value));
            And and = new And();
            and.add(or);
            Home crmSpidHome = (Home) ctx.get(CRMSpidHome.class);
            CRMSpid crmSpidBean = null;
            try
            {
                crmSpidBean = (CRMSpid) crmSpidHome.find(ctx, sub.getSpid());
            }
            catch (Exception e)
            {
            }
            if (crmSpidBean != null && crmSpidBean.isUseContractPricePlan() == true && ((Subscriber) sub).getPricePlan() > 0)
            {
                and.add(new EQ(SubscriptionContractTermXInfo.CONTRACT_PRICE_PLAN, ((Subscriber) sub).getPricePlan()));
            }
            if(sub.getPricePlan() < 0)
            {
            	 and.add(new EQ(SubscriptionContractTermXInfo.SPID, ((Subscriber) sub).getSpid()));
            }
            home = home.where(subCtx, and);
            subCtx.put(SubscriptionContractTermHome.class, home);
            super.toWeb(subCtx, out, name, value);
            long contractId = -1;
            boolean changed = false;
            long newContractId = ((Long) value).longValue();
            int mode = ctx.getInt("MODE", DISPLAY_MODE);
            SubscriptionContract contract = null;
            Date curDate = new Date();
            if (mode == EDIT_MODE
                    || mode == DISPLAY_MODE)
            {
                try
                {
                    contract = HomeSupportHelper.get(ctx).findBean(ctx, SubscriptionContract.class,
                            new EQ(SubscriptionContractXInfo.SUBSCRIPTION_ID, sub.getId()));
                    if (contract != null)
                    {
                        contractId = contract.getContractId();
                    }
                }
                catch (HomeException homeEx)
                {
                }
            }
            // Moving into different contract
            if (newContractId != AbstractSubscriber.DEFAULT_SUBSCRIPTIONCONTRACT
                    && newContractId != SubscriptionContractSupport.SUBSCRIPITON_CONTRACT_EMPTY_CONTRACT
                    && newContractId != SubscriptionContractSupport.SUBSCRIPTION_CONTRACT_NOT_INTIALIZED
                    && newContractId != contractId)
            {
                SubscriptionContractTerm term = getSubscriptionContractTerm(subCtx, newContractId);
                if (term != null)
                {
                    Date endDate = CalendarSupportHelper.get(ctx)
                            .findDateMonthsAfter(term.getContractLength(), curDate);
                    sub.setSubscriptionContractStartDate(curDate);
                    sub.setSubscriptionContractEndDate(endDate);
                    changed = true;
                }
            }
            else if ((newContractId == AbstractSubscriber.DEFAULT_SUBSCRIPTIONCONTRACT
                    || newContractId == SubscriptionContractSupport.SUBSCRIPITON_CONTRACT_EMPTY_CONTRACT || newContractId == SubscriptionContractSupport.SUBSCRIPTION_CONTRACT_NOT_INTIALIZED)
                    && newContractId != contractId)
            {
                // breaking out of contract, but not going into another contract
                sub.setSubscriptionContractStartDate(curDate);
                sub.setSubscriptionContractEndDate(curDate);
                changed = true;
            }
            else if (contract != null)
            {
                sub.setSubscriptionContractEndDate(contract.getContractEndDate());
                sub.setSubscriptionContractStartDate(contract.getContractStartDate());
            }
            if (!changed)
            {
                final Subscriber originalSub = (Subscriber) ctx.get(Subscriber.class);
                if (originalSub != null && originalSub.getPricePlan() != sub.getPricePlan())
                {
                    sub.setSubscriptionContractStartDate(curDate);
                    sub.setSubscriptionContractEndDate(curDate);
                    changed = true;
                }
            }
            try
            {
                if (mode == EDIT_MODE && contractId != SubscriptionContractSupport.SUBSCRIPTION_CONTRACT_NOT_INTIALIZED
                        && changed && !SubscriptionContractSupport.isDummyContract(ctx, contractId))
                {
                    out.print("<font size=\"1\" color=\"red\">Warning: Terminating contract may have penalty. Check the contract terms for the cancellation details</font>");
                }
            }
            catch (HomeException e)
            {
                LogSupport.minor(ctx, this, e);
            }
        }
    }


    public SubscriptionContractTerm getSubscriptionContractTerm(final Context ctx, long contractId)
    {
        try
        {
            SubscriptionContractTerm term = HomeSupportHelper.get(ctx).findBean(ctx, SubscriptionContractTerm.class,
                    new EQ(SubscriptionContractTermXInfo.ID, contractId));
            return term;
        }
        catch (HomeException homeEx)
        {
        }
        return null;
    }
}
