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
package com.trilogy.app.crm.home;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.Date;

import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.Adjustment82;
import com.trilogy.app.crm.bean.PayeeEnum;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberHome;
import com.trilogy.app.crm.bean.SubscriberXInfo;
import com.trilogy.app.crm.bean.core.SubscriptionType;
import com.trilogy.app.crm.bean.core.Transaction;
import com.trilogy.app.crm.support.AccountSupport;
import com.trilogy.app.crm.support.CalendarSupportHelper;
import com.trilogy.app.crm.support.SubscriberSupport;
import com.trilogy.app.crm.support.TransactionSupport;
import com.trilogy.framework.xhome.beans.XBeans;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextAware;
import com.trilogy.framework.xhome.elang.And;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.elang.LTE;
import com.trilogy.framework.xhome.home.Adapter;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.visitor.MaxVisitor;
import com.trilogy.framework.xlog.log.InfoLogMsg;
import com.trilogy.framework.xlog.log.LogSupport;


/**
 * Adapt a Transaction bean to a Adjustment bean.
 *
 * @author jimmy.ng@redknee.com
 */
public class TransactionToAdjustmentAdapter
    implements Adapter, ContextAware
{
	public static final String DATE_FORMAT_STRING = "MM/dd/yy HH:mm:ss";

    /**
     * Creates a new TransactionToAdjustmentAdapter.
     *
     * @param ctx The operating context.
     */
    public TransactionToAdjustmentAdapter(Context ctx)
    {
        setContext(ctx);
    }
    
    
    /**
     * @see com.redknee.framework.xhome.context.ContextAware#getContext()
     */
    @Override
    public Context getContext()
    {
        return ctx_;
    }
    
    
    /**
     * @see com.redknee.framework.xhome.context.ContextAware#setContext(com.redknee.framework.xhome.context.Context)
     */
    @Override
    public void setContext(Context context)
    {
        ctx_ = context;
    }

    /**
     * @see com.redknee.framework.xhome.home.Adapter#adapt(com.redknee.framework.xhome.context.Context, java.lang.Object)
     */
    @Override
    public Object adapt(Context ctx,Object obj) throws HomeException
    {
        if (obj == null)
        {
            return null;
        }
        
        final Transaction transaction = (Transaction) obj;
        Adjustment82 adjustment = null;
        
        try 
        {
            adjustment =(Adjustment82) XBeans.instantiate(Adjustment82.class, getContext());
        } catch(Exception e)
        {
            throw new HomeException("Failed to instantiate Adjustment", e);
        }
        
		SimpleDateFormat format = new SimpleDateFormat(DATE_FORMAT_STRING);
        
        adjustment.setAcctNum(transaction.getAcctNum());
        adjustment.setMSISDN(transaction.getMSISDN());
        adjustment.setAdjustmentType(transaction.getAdjustmentType());
        adjustment.setAmount(transaction.getAmount());
        adjustment.setPaymentAgency(transaction.getPaymentAgency());
        adjustment.setLocationCode(transaction.getLocationCode());
        adjustment.setExtTransactionId(transaction.getExtTransactionId());
        adjustment.setPaymentDetails(transaction.getPaymentDetails());
        adjustment.setCSRInput(transaction.getCSRInput());
        adjustment.setReasonCode(transaction.getReasonCode());
        adjustment.setTransactionMethod(transaction.getTransactionMethod());
        adjustment.setCreditCardNumber(transaction.getCreditCardNumber());
        adjustment.setExpDate(transaction.getExpDate());
		adjustment.setTransDate(format.format(transaction.getTransDate()));
        adjustment.setExpiryDaysExt(transaction.getExpiryDaysExt());
        /*
		adjustment.setPMethodBankID(transaction.getPMethodBankID());
        adjustment.setPMethodCardTypeId(transaction.getPMethodCardTypeId());
        adjustment.setExpDate(transaction.getExpDate());
        adjustment.setBankTransit(transaction.getBankTransit());
        adjustment.setBankAccount(transaction.getBankAccount());
        adjustment.setHolderName(transaction.getHolderName());
        */
        return adjustment;
    }

    /**
     * @see com.redknee.framework.xhome.home.Adapter#unAdapt(com.redknee.framework.xhome.context.Context, java.lang.Object)
     */
    @Override
    public Object unAdapt(Context ctx,Object obj) throws HomeException
    {
        if (obj == null)
        {
            return null;
        }
        

        final Adjustment82 adjustment = (Adjustment82) obj;
        final Transaction transaction = new Transaction();

		SimpleDateFormat format = new SimpleDateFormat(DATE_FORMAT_STRING);
		Date transDate = null;

		if (adjustment.getTransDate()!=null && !adjustment.getTransDate().trim().isEmpty())
		{
    		try
    		{
    			transDate = format.parse(adjustment.getTransDate());
    		}
    		catch (ParseException e)
    		{
    			throw new HomeException(
    			    "TransDate of the adjustment is not in the proper format of "
    			        + DATE_FORMAT_STRING, e);
    		}
		}
		else
		{
		    transDate = CalendarSupportHelper.get(ctx).getRunningDate(ctx);
		}

		transaction.setAcctNum(adjustment.getAcctNum());
        transaction.setMSISDN(adjustment.getMSISDN());
        transaction.setAdjustmentType(adjustment.getAdjustmentType());
        transaction.setAmount(adjustment.getAmount());
        transaction.setPaymentAgency(adjustment.getPaymentAgency());
        transaction.setLocationCode(adjustment.getLocationCode());
        transaction.setExtTransactionId(adjustment.getExtTransactionId());
        transaction.setPaymentDetails(adjustment.getPaymentDetails());
        transaction.setCSRInput(adjustment.getCSRInput());
        transaction.setReasonCode(adjustment.getReasonCode());
        transaction.setTransactionMethod(adjustment.getTransactionMethod());
        transaction.setCreditCardNumber(adjustment.getCreditCardNumber());
        transaction.setExpDate(adjustment.getExpDate());
		transaction.setTransDate(transDate);
        transaction.setSubscriptionTypeId(SubscriptionType.getINSubscriptionType(ctx).getId());
        transaction.setExpiryDaysExt(adjustment.getExpiryDaysExt());
        /*
		transaction.setPMethodBankID(adjustment.getPMethodBankID());
        transaction.setPMethodCardTypeId(adjustment.getPMethodCardTypeId());
        transaction.setExpDate(adjustment.getExpDate());
        transaction.setBankTransit(adjustment.getBankTransit());
        transaction.setBankAccount(adjustment.getBankAccount());
        transaction.setHolderName(adjustment.getHolderName());
		*/

        // Default payee type of a transaction is Subscriber, and must be
        // set to Account for account payments/adjustments.
        final String acctNum = transaction.getAcctNum().trim();
        final String msisdn = transaction.getMSISDN().trim();
        if (acctNum.length() != 0)
        {
            ctx.put(Account.class, AccountSupport.getAccount(ctx, acctNum));
            if(msisdn.length() == 0)
            {
                transaction.setPayee(PayeeEnum.Account);
                
            } else
            {
                Subscriber sub = getSubsriber(ctx, acctNum, msisdn, transaction.getTransDate());
                ctx.put(Subscriber.class, sub);
                transaction.setSubscriberID(sub.getId());
            }
            
        } else 
        {
            TransactionSupport.setSubscriberID(ctx, transaction);
        	ctx.put(Account.class, AccountSupport.getAccountByMsisdn(ctx, msisdn));
        }
        
        return transaction;
    }
    
    private Subscriber getSubsriber(Context ctx, String ban, String msisdn, Date date) throws HomeException
    {
        Subscriber sub = SubscriberSupport.lookupSubscriberForMSISDN(ctx, msisdn, CalendarSupportHelper.get(ctx).getDayAfter(date));
        if(null != sub && sub.getBAN().equals(ban) )
        {
            return sub;
        }
        else
        {
            if(LogSupport.isDebugEnabled(ctx))
            {
                new InfoLogMsg(this,"Current Subscriber found with MSISDN [" + msisdn + "] does not belong to Account-BAN ["+ ban + "]. Looking for possible deactivated Subscribers in the account",null).log(ctx);
            }
            MaxVisitor maxVisitor = new MaxVisitor(new Comparator<Subscriber>() 
            {
                @Override
                public int compare(Subscriber sub1, Subscriber sub2) 
                {
                    return sub1.getLastModified().before(sub2.getLastModified())?-1:1;
                }
            });
            ((Home)ctx.get(SubscriberHome.class)).forEach(ctx, maxVisitor, new And().add( new EQ(SubscriberXInfo.BAN,ban)).add(new LTE(SubscriberXInfo.START_DATE, date)));
            // don't check for end - date because transactions can come to deactivated subscribers.
            // TT#
            sub = (Subscriber)maxVisitor.getValue();
        }
        
        if(null == sub)
        {
            throw new HomeException("No Active or Deactivated Subscriber(s) found of Account-BAN ["+ ban + "] with MSISDN [" + msisdn + "]. ");
        }
        return sub;
    }
    
    
    protected Context ctx_ = null;
}
