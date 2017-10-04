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
import java.util.Collection;

import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.AdjustmentTypeActionEnum;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberRechargeRequest;
import com.trilogy.app.crm.bean.SubscriberTypeEnum;
import com.trilogy.app.crm.bean.Transaction;
import com.trilogy.app.crm.home.sub.SubscriberHomeFactory;
import com.trilogy.app.crm.support.AccountSupport;
import com.trilogy.app.crm.support.HomeSupportHelper;
import com.trilogy.app.crm.support.SubscriberSupport;
import com.trilogy.app.crm.util.SimpleLocks;
import com.trilogy.framework.xhome.beans.XBeans;
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
 * Retry recharging for postpaid services.
 * 
 * @author sbanerjee
 *
 */
public class TransactionMonitorPostpaidCreditHome extends HomeProxy
{

    /**
     * Serial version UID.
     */
    private static final long serialVersionUID = 1L;


    /**
     * 
     * @param delegate
     * @param visitor
     */
    public TransactionMonitorPostpaidCreditHome(final Home delegate, final Visitor visitor)
    {
        super(delegate);
        this.visitor = visitor;
    }


    /**
     * {@inheritDoc}
     */
    public Object create(final Context context, final Object object) throws HomeException, HomeInternalException
    {
        final Object result = super.create(context, object);
        
        if(!(result instanceof Transaction))
            return result;
        
        final Transaction txn = (Transaction) result;
        
        if (!(txn.getSubscriberType() == SubscriberTypeEnum.POSTPAID && txn.getAction() == AdjustmentTypeActionEnum.CREDIT))
        {
            if(LogSupport.isDebugEnabled(context))
            {
                String msg = MessageFormat.format(
                    "Retry Recharge: Skipping TXN {0} as the TXN-Subscriber-Type: {1}, TXN-Action-Type: {2}, TXN-Amount: {3}", 
                        new Object[]{Long.valueOf(txn.getReceiptNum()), txn.getSubscriberType(), txn.getAction(), 
                            Long.valueOf(txn.getAmount())});
                LogSupport.debug(context, this, msg);
            }
            return result;
        }

        
        
        if(LogSupport.isDebugEnabled(context))
        {
            String msg = MessageFormat.format(
                "Retry Recharge: ON TXN {0} as the TXN-Subscriber-Type: {1}, TXN-Action-Type: {2}, TXN-Amount: {3}", 
                    new Object[]{Long.valueOf(txn.getReceiptNum()), txn.getSubscriberType(), txn.getAction(), 
                        Long.valueOf(txn.getAmount())});
            LogSupport.debug(context, this, msg);
        }
        
        final PMLogMsg pm = new PMLogMsg("Transaction", "Postpaid credit - Recurring recharge", txn.getSubscriberID());
        
        final SimpleLocks locker = (SimpleLocks) context.get(SubscriberHomeFactory.SUBCRIBER_LOCKER);
        if (locker != null && txn.getSubscriberID() != null)
        	locker.lock(txn.getSubscriberID()); 
        
        final Subscriber subscriber = SubscriberSupport.getSubscriber(context, txn.getSubscriberID());

        try
        {
            this.visitor.visit(context, subscriber);
            
            Collection<Subscriber> subscribersUnderPooledAcct = null;
            Account account = subscriber.getAccount(context);
            if(account.isPooled(context))
            {
                subscribersUnderPooledAcct =  AccountSupport.getAllSubscribers(context, subscriber.getAccount(context));
            }
            if(subscribersUnderPooledAcct != null 
                    && subscribersUnderPooledAcct.size() > 0)
            {
                for (Subscriber sub : subscribersUnderPooledAcct)
                {
                    if(!sub.getId().trim().equals(subscriber.getId().trim()) ) //Do not insert request for the dummy pooled subscription here
                    {
                        try
                        {
                            SubscriberRechargeRequest rechargeReq = XBeans.instantiate(SubscriberRechargeRequest.class, context);
                            rechargeReq.setBAN(sub.getBAN());
                            rechargeReq.setSpid(sub.getSpid());
                            rechargeReq.setSubscriberid(sub.getId());
                            rechargeReq.setTransactionReceiptNum(txn.getReceiptNum());
                            
                            HomeSupportHelper.get(context).createBean(context, rechargeReq);
                        }
                        catch (HomeException e) 
                        {
                            LogSupport.minor(context, this, "Problem occurred while creating recharge request for child subscribers" +
                            		" for pooled account BAN: "+subscriber.getBAN());
                        }
                    }
                }
            }
        }
        catch (final Throwable throwable)
        {
            new MajorLogMsg(this, "Problem occurred while visiting" + " Prepaid subscriber " + subscriber.getId()
                + " with after successful Credit transaction.", throwable).log(context);
        } 
        finally 
        {
            if (locker != null && txn.getSubscriberID() != null)
            	locker.unlock(txn.getSubscriberID());
        }
        
        pm.log(context);
        return txn;
    }

    /**
     * Visitor to apply to the subscriber owning this transaction if it should be charged.
     */
    Visitor visitor;
}