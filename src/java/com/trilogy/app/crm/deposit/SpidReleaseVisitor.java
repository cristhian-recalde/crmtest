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

package com.trilogy.app.crm.deposit;

import java.util.Date;

import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.ConstantContextFactory;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.SingletonContextFactory;
import com.trilogy.framework.xhome.msp.Spid;
import com.trilogy.framework.xhome.visitor.Visitor;

import com.trilogy.app.crm.bean.CRMSpid;
import com.trilogy.app.crm.factory.BeanAdaptingContextFactory;

/**
 * A visitor to CRMSpidHome to automatically release subscriber deposits according to the criteria(s) applicable.
 *
 * @author cindy.wong@redknee.com
 */
public class SpidReleaseVisitor implements Visitor
{

    /**
     * Serial version UID.
     */
    private static final long serialVersionUID = -4765257766088480660L;

    /**
     * The date to act upon.
     */
    private final Date activeDate_;

    /**
     * Creates a new <code>SpidReleaseVisitor</code>.
     *
     * @param activeDate
     *            The date to act upon.
     */
    public SpidReleaseVisitor(final Date activeDate)
    {
        activeDate_ = activeDate;
    }

    /**
     * If criteria are met, releases a portion of deposit of eligible subscribers for this service provider.
     *
     * @param context
     *            The operating context.
     * @param object
     *            The service provider.
     * @throws AgentException
     *             Thrown if there is a problem calculating the deposit release of this subscriber.
     * @see com.redknee.framework.xhome.visitor.Visitor#visit(com.redknee.framework.xhome.context.Context,
     *      java.lang.Object)
     */
    public final void visit(final Context context, final Object object) throws AgentException
    {
        final CRMSpid serviceProvider = (CRMSpid) object;
        final Context subContext = context.createSubContext();
        subContext.put(
                Spid.class, 
                new SingletonContextFactory(
                        new BeanAdaptingContextFactory<CRMSpid, Spid>(
                                CRMSpid.class, Spid.class, 
                                new ConstantContextFactory(serviceProvider))));
        new AutoDepositRelease(serviceProvider, activeDate_, new DefaultSubscriberReleaseVisitor(),
            DefaultDepositReleaseTransactionCreator.getInstance()).execute(subContext);
    }

}
