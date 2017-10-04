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

import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberStateEnum;
import com.trilogy.app.crm.bean.SubscriberXInfo;
import com.trilogy.app.crm.support.CalendarSupportHelper;
import com.trilogy.app.crm.support.EnumStateSupportHelper;
import com.trilogy.app.crm.support.HomeSupportHelper;
import com.trilogy.app.crm.support.Lookup;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeInternalException;
import com.trilogy.framework.xhome.home.HomeProxy;


/**
 * Charges and release of deposit
 * 
 * @author kumaran.sivasubramaniam@redknee.com
 */
public class SubscriptionContractDepositHome extends HomeProxy
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
    public SubscriptionContractDepositHome(final Home delegate)
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
    public SubscriptionContractDepositHome(final Context context, final Home delegate)
    {
        super(delegate);
        setContext(context);
    }


    /**
     * @param ctx
     * @param obj
     * @return Object
     * @exception HomeException
     * @exception HomeInternalException
     */
    public Object create(Context ctx, Object obj) throws HomeException, HomeInternalException
    {
        SubscriptionContract contract = (SubscriptionContract) obj;
        Date startDate = CalendarSupportHelper.get(ctx).getDateWithNoTimeOfDay(new Date());
        Subscriber sub = SubscriptionContractSupport.getSubscriber(ctx, contract);
        
        return getDelegate(ctx).create(ctx, obj);
    }


    /**
     * write to data store
     * 
     * @param ctx
     * @param obj
     * @exception HomeException
     * @exception HomeInternalException
     */
    public Object store(Context ctx, Object obj) throws HomeException, HomeInternalException
    {
        SubscriptionContract contract = (SubscriptionContract) obj;
        return getDelegate(ctx).store(ctx, obj);
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
            Date cancelDate = CalendarSupportHelper.get(ctx).getDateWithNoTimeOfDay(new Date());
            SubscriptionContractSupport.applySubscriptionContractRefund(ctx, sub, contract, cancelDate);
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
        Date cancelDate = CalendarSupportHelper.get(ctx).getDateWithNoTimeOfDay(new Date());
        for (Iterator<SubscriptionContract> iter = list.iterator(); iter.hasNext();)
        {
            SubscriptionContract contract = iter.next();
            if(!SubscriptionContractSupport.isDummyContract(ctx, contract.getContractId()))
            {
                Subscriber sub = SubscriptionContractSupport.getSubscriber(ctx, contract);
                SubscriptionContractSupport.applySubscriptionContractRefund(ctx, sub, contract, cancelDate);
            }
        }
        getDelegate(ctx).removeAll(ctx, where);
    }
}
