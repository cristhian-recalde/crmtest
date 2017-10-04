/*
 * This code is a protected work and subject to domestic and international copyright
 * law(s). A complete listing of authors of this work is readily available. Additionally,
 * source code is, by its very nature, confidential information and inextricably contains
 * trade secrets and other information proprietary, valuable and sensitive to Redknee, no
 * unauthorised use, disclosure, manipulation or otherwise is permitted, and may only be
 * used in accordance with the terms of the licence agreement entered into with Redknee
 * Inc. and/or its subsidiaries.
 * 
 * Copyright &copy; Redknee Inc. and its subsidiaries. All Rights Reserved.
 */
package com.trilogy.app.crm.contract;

import java.util.Date;

import com.trilogy.app.crm.bean.PricePlanXInfo;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberHome;
import com.trilogy.app.crm.bean.SubscriberXInfo;
import com.trilogy.app.crm.bean.core.PricePlan;
import com.trilogy.app.crm.support.HomeSupportHelper;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.visitor.AbortVisitException;
import com.trilogy.framework.xhome.visitor.Visitor;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.framework.xlog.log.MinorLogMsg;


/**
 * @author
 * @since 2010-11-01
 */
public class SubscriptionContractEndUpdateProcessingVisitor implements Visitor
{

    private static final long serialVersionUID = 1L;


    public SubscriptionContractEndUpdateProcessingVisitor(Date billingDate)
    {
        endDate_ = billingDate;
    }


    /**
     * @param ctx
     * @param obj
     * @throws AgentException
     * @throws AbortVisitException
     * @see com.redknee.framework.xhome.visitor.Visitor#visit(com.redknee.framework.xhome.context.Context,
     *      java.lang.Object)
     */
    @Override
    public void visit(Context ctx, Object obj) throws AgentException, AbortVisitException
    {
        SubscriptionContract contract = (SubscriptionContract) obj;
        try
        {
            
            if(!SubscriptionContractSupport.isDummyContract(ctx, contract.getContractId()))
            {
                Home subHome = (Home) ctx.get(SubscriberHome.class);
                Subscriber sub = HomeSupportHelper.get(ctx).findBean(ctx, Subscriber.class,
                        new EQ(SubscriberXInfo.ID, contract.getSubscriptionId()));
                if (sub != null && (endDate_.getTime() >= contract.getBonusEndDate().getTime()))
                {
                    SubscriptionContractTerm term = HomeSupportHelper.get(ctx).findBean(ctx,
                            SubscriptionContractTerm.class,
                            new EQ(SubscriptionContractTermXInfo.ID, contract.getContractId()));
                    if (term != null)
                    {
                        PricePlan pp = HomeSupportHelper.get(ctx).findBean(ctx, PricePlan.class,
                                Long.valueOf(term.getCancelPricePlan()));
                        if (pp != null)
                        {
                            sub.setSubscriptionContract(SubscriptionContractSupport.SUBSCRIPITON_CONTRACT_EMPTY_CONTRACT);
                            sub.switchPricePlan(ctx, term.getCancelPricePlan());
                            subHome.store(ctx, sub);
                        }
                    }
                }
                else
                {
                    new MinorLogMsg(this, " Contract has not subscription associated to it subId["
                            + contract.getSubscriptionId() + "] .", null).log(ctx);
                }
            }
        }
        catch (HomeException exception)
        {
            LogSupport.minor(ctx, this, "Exception caught during subscription contract bonus end update processing",
                    exception);
            throw new AgentException(exception);
        }
    }
    private Date endDate_;
}
