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
package com.trilogy.app.crm.transaction.task;

import com.trilogy.app.crm.Common;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.Transaction;
import com.trilogy.app.crm.bean.TransactionHome;
import com.trilogy.app.crm.bean.TransactionXInfo;
import com.trilogy.app.crm.lifecycle.LifecycleAgentScheduledTask;
import com.trilogy.app.crm.support.CoreTransactionSupportHelper;
import com.trilogy.app.crm.support.SubscriberSupport;
import com.trilogy.framework.lifecycle.LifecycleStateEnum;
import com.trilogy.framework.xhome.beans.XBeans;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.And;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.holder.LongHolder;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.visitor.AbortVisitException;
import com.trilogy.framework.xhome.visitor.Visitor;
import com.trilogy.framework.xlog.log.InfoLogMsg;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.product.s2100.ErrorCode;


/**
 * Visitor to process unapplied transactions
 * @author Marcio Marques
 * @since 9.1.3
 *
 */
public final class UnappliedTransactionProcessingVisitor implements Visitor
{
    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    
    private final LifecycleAgentScheduledTask agent_;


    public UnappliedTransactionProcessingVisitor(LifecycleAgentScheduledTask agent)
    {
        this.agent_ = agent;
    }


    public void visit(Context ctx, Object obj) throws AgentException,
    		AbortVisitException 
    		{
    	
    	try
    	{
            if (agent_ != null && !LifecycleStateEnum.RUNNING.equals(agent_.getState()))
            {
                String msg = "Lifecycle agent " + agent_.getAgentId() + " no longer running. Remaining unapplied transactions will be processed next time.";
                new InfoLogMsg(this, msg, null).log(ctx);
                throw new AbortVisitException(msg);
            }

            Transaction originalTransaction = (Transaction)obj;
       
            final Home unappliedTransactionHome = (Home) ctx.get(Common.UNAPPLIED_TRANSACTION_HOME);
            final Home transactionHome = (Home) ctx.get(TransactionHome.class);

            And andPredicate = new And();
            andPredicate.add(new EQ(TransactionXInfo.EXT_TRANSACTION_ID, originalTransaction
                    .getExtTransactionId()));
            andPredicate.add(new EQ(TransactionXInfo.AMOUNT, originalTransaction.getAmount()));

            Object transactionEntry = transactionHome.find(andPredicate);
            
            if( transactionEntry != null ) 
            {
            	
            	unappliedTransactionHome.remove(ctx, obj);
            	LogSupport.minor(ctx, this, "Transaction with External Transaction ID:" + originalTransaction.getExtTransactionId() + " and Amout: " + 
            			originalTransaction.getAmount() + " already present in the system. This transaction is duplicate and is deleted from UnappliedTransaction table.");
            	return;
            }
    		

            Transaction transaction = (Transaction) XBeans.copy(ctx, TransactionXInfo.instance(),
                    originalTransaction, TransactionXInfo.instance());
            transaction.setReceiptNum(Transaction.DEFAULT_RECEIPTNUM);
            
            						
            // if there's an issue creating a transaction(faulty data, etc.) log the error proceed to the next record.
            try 
            {
                //Retrieving the subscriber
                Subscriber subscriber = null;
                if (transaction.getSubscriberID() != null && !transaction.getSubscriberID().isEmpty())
                {
                    subscriber = SubscriberSupport.getSubscriber(ctx, transaction.getSubscriberID());
                }
                else if (transaction.getMSISDN()!=null)
                {
                    subscriber = SubscriberSupport.lookupSubscriberForMSISDN(ctx, transaction.getMSISDN(), transaction.getTransDate());
                }
            
                if (subscriber==null)
                {
                    throw new HomeException("Unable to retrieve subscription for unapplied transaction.");
                }
                
                // Forwarding transaction to OCG.
                LongHolder balance = new LongHolder();
                int result = CoreTransactionSupportHelper.get(ctx).forwardToOcg(ctx, subscriber.getMsisdn(),
                        transaction.getAmount(), subscriber.getSubscriptionType(ctx), subscriber.getCurrency(ctx),
                        subscriber.getSubscriberType(), this, (short) 0, false, balance);
                
                if (result == ErrorCode.NO_ERROR)
                {
                    transaction.setFromVRAPoller(true);
                    transaction.setBalance(balance.getValue());
                    Context subCtx = ctx.createSubContext();
                    subCtx.put(Subscriber.class, subscriber);
                    transaction = (Transaction) transactionHome.create(subCtx, transaction);
        			
                    // Removing unapplied transaction.
                    unappliedTransactionHome.remove(ctx, obj);
                }
                else
                {
                    LogSupport.major(ctx, this, "Unable to forward  unapplied transaction with receipt number '" + ((Transaction)obj).getReceiptNum() 
                            + "' to OCG (Return code = " + result +"). Proceeding to the next transaction.");
                }
            } 
            catch (HomeException t) 
            {
            	LogSupport.major(ctx, this, "Internal Error processing unapplied transaction with receipt number '" + ((Transaction)obj).getReceiptNum() 
            			+ "'. Proceeding to the next transaction: " + t.getMessage(), t);
            }
            
            
    	} 
    	catch (Exception e) 
    	{
    		LogSupport.major(ctx, this, "Error processing unapplied transaction with receipt number '" + ((Transaction)obj).getReceiptNum() + "': " + e.getMessage(), e);
    	}
    	
    }
}