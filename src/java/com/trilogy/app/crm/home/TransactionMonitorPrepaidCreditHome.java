/*
 * This code is a protected work and subject to domestic and international
 * copyright law(s). A complete listing of authors of this work is readily
 * available. Additionally, source code is, by its very nature, confidential
 * information and inextricably contains trade secrets and other information
 * proprietary, valuable and sensitive to Redknee.  No unauthorized use,
 * disclosure, manipulation or otherwise is permitted, and may only be used
 * in accordance with the terms of the license agreement entered into with
 * Redknee Inc. and/or its subsidiaries.
 *
 * Copyright (c) Redknee Inc. (Migreated for testing purposes) and its subsidiaries. All Rights Reserved.
 */
package com.trilogy.app.crm.home;

import java.text.MessageFormat;

import com.trilogy.app.crm.bean.AdjustmentTypeActionEnum;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberTypeEnum;
import com.trilogy.app.crm.bean.Transaction;
import com.trilogy.app.crm.home.sub.SubscriberHomeFactory;
import com.trilogy.app.crm.support.SubscriberSupport;
import com.trilogy.app.crm.util.SimpleLocks;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeInternalException;
import com.trilogy.framework.xhome.home.HomeProxy;
import com.trilogy.framework.xhome.visitor.Visitor;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.framework.xlog.log.MajorLogMsg;
import com.trilogy.framework.xlog.log.PMLogMsg;


/**
 * Monitor transaction creation and call the provided visitor if a credit transaction was
 * created successfully for a Prepaid subscriber.
 *
 * @author victor.stratan@redknee.com
 */
public class TransactionMonitorPrepaidCreditHome extends HomeProxy
{

    /**
     * Serial version UID.
     */
    private static final long serialVersionUID = 1L;


    /**
     * Create a new instance of <code>TransactionMonitorPrepaidCreditHome</code>.
     *
     * @param delegate
     *            Delegate of this home.
     * @param visitor
     *            Visitor to apply to the subscriber owning this transaction if it should
     *            be charged.
     */
    public TransactionMonitorPrepaidCreditHome(final Home delegate, final Visitor visitor)
    {
        super(delegate);
        visitor_ = visitor;
    }


    /**
     * {@inheritDoc}
     */
    public Object create(final Context context, final Object object) throws HomeException, HomeInternalException
    {
        final Object result = super.create(context, object);

        // exception was not thrown, Transaction was created ok
        // lets check we need to grab some money
        final Transaction t = (Transaction) result;
        if (t.getSubscriberType() == SubscriberTypeEnum.PREPAID && t.getAction() == AdjustmentTypeActionEnum.CREDIT)
        {
            final PMLogMsg pm = new PMLogMsg("Transaction", "Prepaid credit - Recurring recharge", t.getSubscriberID());
            
            if(LogSupport.isDebugEnabled(context))
            {
                String msg = MessageFormat.format(
                    "Retry Recharge: ON TXN {0} as the TXN-Subscriber-Type: {1}, TXN-Action-Type: {2}, TXN-Amount: {3}", 
                        new Object[]{Long.valueOf(t.getReceiptNum()), t.getSubscriberType(), t.getAction(), 
                            Long.valueOf(t.getAmount())});
                LogSupport.debug(context, this, msg);
            }
            
            final SimpleLocks locker = (SimpleLocks) context.get(SubscriberHomeFactory.SUBCRIBER_LOCKER);
            if (locker != null && t.getSubscriberID() != null)
            {
            	locker.lock(t.getSubscriberID()); 
            }
            final Subscriber subscriber = SubscriberSupport.getSubscriber(context, t.getSubscriberID());

            try
            {
                visitor_.visit(context, subscriber);
            }
            catch (final Throwable throwable)
            {
                new MajorLogMsg(this, "Problem occurred while visiting" + " Prepaid subscriber " + subscriber.getId()
                    + " with after successful Credit transaction.", throwable).log(context);
            } finally 
            {
                if (locker != null && t.getSubscriberID() != null)
                {
                	locker.unlock(t.getSubscriberID());
                }	
            }
            
            pm.log(context);
        }
        else if(LogSupport.isDebugEnabled(context))
        {
            String msg = MessageFormat.format(
                "Retry Recharge: Skipping TXN {0} as the TXN-Subscriber-Type: {1}, TXN-Action-Type: {2}, TXN-Amount: {3}", 
                    new Object[]{Long.valueOf(t.getReceiptNum()), t.getSubscriberType(), t.getAction(), 
                        Long.valueOf(t.getAmount())});
            LogSupport.debug(context, this, msg);
        }

        return result;
    }

    /**
     * Visitor to apply to the subscriber owning this transaction if it should be charged.
     */
    Visitor visitor_;
}
