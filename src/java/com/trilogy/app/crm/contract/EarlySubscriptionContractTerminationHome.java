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

import java.util.Collection;
import java.util.Date;
import java.util.Iterator;

import com.trilogy.app.crm.bean.CRMSpid;
import com.trilogy.app.crm.bean.CRMSpidHome;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.support.HomeSupportHelper;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeInternalException;
import com.trilogy.framework.xhome.home.HomeProxy;


/**
 * Creates new adjustment types for new contracts, updates and look-ups existing
 * adjustment types for existing contracts.
 * 
 * @author kumaran.sivasubramaniam@redknee.com
 */
public class EarlySubscriptionContractTerminationHome extends HomeProxy
{

    /**
     * 
     */
    private static final long serialVersionUID = 1L;


    /**
     * Creates a new EarlySubscriptionContractTerminationHome.
     * 
     * @param delegate
     *            The home to which we delegate.
     */
    public EarlySubscriptionContractTerminationHome(final Home delegate)
    {
        super(delegate);
    }


    /**
     * Creates a new EarlySubscriptionContractTerminationHome.
     * 
     * @param context
     *            The operating context.
     * @param delegate
     *            The home to which we delegate.
     */
    public EarlySubscriptionContractTerminationHome(final Context context, final Home delegate)
    {
        super(delegate);
        setContext(context);
    }


    /**
     * remove from data store
     * 
     * @param ctx
     * @param obj
     * @exception HomeException
     * @exception HomeInternalException
     */
    public void remove(Context ctx, Object obj) throws HomeException, HomeInternalException
    {
        SubscriptionContract contract = (SubscriptionContract) obj;
        
        if(!SubscriptionContractSupport.isDummyContract(ctx, contract.getContractId()))
        {
            Subscriber sub = SubscriptionContractSupport.getSubscriber(ctx, contract);
            SubscriptionContractTerm term = SubscriptionContractSupport.getSubscriptionContractTerm(ctx, contract);
            SubscriptionContractSupport.applyEarlyTerminationPenalty(ctx, sub, contract, new Date(),
                    term.getProrateCancellationFees());
        }
        getDelegate(ctx).remove(ctx, obj);
    }


    /**
     * remove all data from data store
     * 
     * @param ctx
     * @param where
     *            Predicate
     * @exception HomeException
     * @exception HomeInternalException
     * @exception UnsupportedOperationException
     */
    public void removeAll(Context ctx, Object where) throws HomeException, HomeInternalException,
            UnsupportedOperationException
    {
        Collection<SubscriptionContract> list = HomeSupportHelper.get(ctx).getBeans(ctx, SubscriptionContract.class,
                where);
        for (Iterator<SubscriptionContract> iter = list.iterator(); iter.hasNext();)
        {
            SubscriptionContract contract = iter.next();
            if(!SubscriptionContractSupport.isDummyContract(ctx, contract.getContractId()))
            {
                SubscriptionContractTerm term = SubscriptionContractSupport.getSubscriptionContractTerm(ctx, contract);
                Subscriber sub = SubscriptionContractSupport.getSubscriber(ctx, contract);
                SubscriptionContractSupport.applyEarlyTerminationPenalty(ctx, sub, contract, new Date(), term.getProrateCancellationFees());
            }
        }
        getDelegate(ctx).removeAll(ctx, where);
    }

    public static final int PENALTY_ADJUSTMENT_ID = 123112;
}
