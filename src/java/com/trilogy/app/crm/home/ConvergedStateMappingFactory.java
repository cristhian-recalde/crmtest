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
package com.trilogy.app.crm.home;

import java.io.IOException;
import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.Map;

import com.trilogy.app.crm.bean.AccountStateEnum;
import com.trilogy.app.crm.bean.ConvergedStateEnum;
import com.trilogy.app.crm.bean.SubscriberStateEnum;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.Home;

/**
 * Creates a Map to for Account and Subscriber states to Converged search states
 * @author amedina
 *
 */
public class ConvergedStateMappingFactory implements PipelineFactory
{

    public ConvergedStateMappingFactory()
    {
        super();
    }

    public Home createPipeline(Context ctx, Context serverCtx)
            throws RemoteException, HomeException, IOException
    {
        Map state = new HashMap();

        //Subscriber State mapping
        state.put(SubscriberStateEnum.ACTIVE, ConvergedStateEnum.ACTIVE);
        state.put(SubscriberStateEnum.AVAILABLE, ConvergedStateEnum.AVAILABLE);
        state.put(SubscriberStateEnum.PROMISE_TO_PAY, ConvergedStateEnum.PROMISE_TO_PAY);
        state.put(SubscriberStateEnum.SUSPENDED, ConvergedStateEnum.SUSPENDED);
        state.put(SubscriberStateEnum.EXPIRED, ConvergedStateEnum.EXPIRED);
        state.put(SubscriberStateEnum.NON_PAYMENT_SUSPENDED, ConvergedStateEnum.NON_PAYMENT_SUSPENDED);
        state.put(SubscriberStateEnum.NON_PAYMENT_WARN, ConvergedStateEnum.NON_PAYMENT_WARN);
        state.put(SubscriberStateEnum.IN_ARREARS, ConvergedStateEnum.IN_ARREARS);
        state.put(SubscriberStateEnum.IN_COLLECTION, ConvergedStateEnum.IN_COLLECTION);
        state.put(SubscriberStateEnum.LOCKED, ConvergedStateEnum.LOCKED);
        state.put(SubscriberStateEnum.INACTIVE, ConvergedStateEnum.INACTIVE);
        state.put(SubscriberStateEnum.PENDING, ConvergedStateEnum.PENDING);
        state.put(SubscriberStateEnum.DORMANT, ConvergedStateEnum.DORMANT);

        //Account state mapping
        state.put(AccountStateEnum.ACTIVE, ConvergedStateEnum.ACTIVE);
        state.put(AccountStateEnum.PROMISE_TO_PAY, ConvergedStateEnum.PROMISE_TO_PAY);
        state.put(AccountStateEnum.SUSPENDED, ConvergedStateEnum.SUSPENDED);
        state.put(AccountStateEnum.NON_PAYMENT_SUSPENDED, ConvergedStateEnum.NON_PAYMENT_SUSPENDED);
        state.put(AccountStateEnum.NON_PAYMENT_WARN, ConvergedStateEnum.NON_PAYMENT_WARN);
        state.put(AccountStateEnum.IN_ARREARS, ConvergedStateEnum.IN_ARREARS);
        state.put(AccountStateEnum.IN_COLLECTION, ConvergedStateEnum.IN_COLLECTION);
        state.put(AccountStateEnum.INACTIVE, ConvergedStateEnum.INACTIVE);

        ctx.put(ConvergedStateEnum.class, state);

        return null;
    }

}
