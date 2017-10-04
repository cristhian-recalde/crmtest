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
package com.trilogy.app.crm.bas.tps;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.Set;

import com.trilogy.app.crm.Common;
import com.trilogy.app.crm.bas.tps.pipe.ConveragedAccountSubscriberLookupAgent;
import com.trilogy.app.crm.bas.tps.pipe.DuplicateMSISDNException;
import com.trilogy.app.crm.bas.tps.pipe.LastMsisdnHolderVisitor;
import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.AdjustmentTypeEnum;
import com.trilogy.app.crm.bean.GeneralConfig;
import com.trilogy.app.crm.bean.Invoice;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberInvoice;
import com.trilogy.app.crm.bean.SubscriberStateEnum;
import com.trilogy.app.crm.bean.SubscriberTypeEnum;
import com.trilogy.app.crm.bean.SubscriberXInfo;
import com.trilogy.app.crm.bean.SystemTransactionMethodsConstants;
import com.trilogy.app.crm.bean.Transaction;
import com.trilogy.app.crm.bean.payment.PaymentException;
import com.trilogy.app.crm.bean.payment.PaymentExceptionHome;
import com.trilogy.app.crm.bean.payment.PaymentFailureTypeEnum;
import com.trilogy.app.crm.bean.payment.PostpaidSubscribersVisitor;
import com.trilogy.app.crm.bean.payment.TotalOutstandingSubscriberVisitor;
import com.trilogy.app.crm.calculation.service.CalculationService;
import com.trilogy.app.crm.calculation.service.CalculationServiceException;
import com.trilogy.app.crm.home.calldetail.SubscriberNotFoundHomeException;
import com.trilogy.app.crm.invoice.AdjustmentTypeCategoryAccumulator;
import com.trilogy.app.crm.numbermgn.HistoryEventSupport;
import com.trilogy.app.crm.numbermgn.MsisdnMgmtHistory;
import com.trilogy.app.crm.numbermgn.MsisdnMgmtHistoryHome;
import com.trilogy.app.crm.numbermgn.MsisdnMgmtHistoryXInfo;
import com.trilogy.app.crm.state.InOneOfStatesPredicate;
import com.trilogy.app.crm.support.AccountSupport;
import com.trilogy.app.crm.support.CalendarSupportHelper;
import com.trilogy.app.crm.support.CollectionSupportHelper;
import com.trilogy.app.crm.support.CoreTransactionSupportHelper;
import com.trilogy.app.crm.support.PaymentExceptionSupport;
import com.trilogy.app.crm.support.SubscriberSupport;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.And;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.elang.LTE;
import com.trilogy.framework.xhome.elang.Or;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.visitor.Visitors;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.framework.xlog.log.MinorLogMsg;
import com.trilogy.framework.xlog.log.OMLogMsg;

/**
 * This Class was added when porting the new Account Level Payment Splitting Logic from 
 * CRM 7.3
 * @since 7.3, ported to 8.2, Sept 21, 2009. 
 * 
 * 
 * @author Larry Xia
 * @author Angie Li
 *
 */
public abstract class TPSSupport 
{

    /** 
     * return Account OBO
     * @param ctx
     * @param acct
     * @return
     * @throws CalculationServiceException 
     * @throws  
     */
    public static long getAccountOutstanddingBalanceOwing(Context ctx, Account acct)
    throws HomeException, CalculationServiceException
    {
        long owing = 0;
        
        CalculationService service = (CalculationService) ctx.get(CalculationService.class);
        
        Invoice invoice = service.getMostRecentInvoice(ctx, acct.getBAN());
        
        if ( invoice != null )
        {
            // Credit Transactions here is actually all transactions since the last invoice.
            // These are discarded after looking for all of the credit transactions.  Wasteful?
            // TODO: Figure out a way to use the calculation service to get the total credit amount...
            Collection transactions = getTransactionsForAccountHierachy(ctx, 
                    acct, 
                    invoice.getInvoiceDate(), 
                    CalendarSupportHelper.get(ctx).getRunningDate(ctx));
            
            long credit = getTotalCreditAmount(ctx, transactions);  
            owing =  service.getDueAmountForAccount(ctx, acct.getBAN(), CalendarSupportHelper.get(ctx).getRunningDate(ctx)) + credit; 
            if (owing < 0 )
            {
                owing = 0; 
            }
        }
        return owing;
    }
    
    
    public static long getSubscriberOutstandingBalanceOwing(Context ctx, String subscriberId) throws CalculationServiceException
    {
        long owing = 0; 
        
        CalculationService service = (CalculationService) ctx.get(CalculationService.class);
        
        SubscriberInvoice invoice = service.getMostRecentSubscriberInvoice(ctx, subscriberId);
        
        if (invoice != null)
        {
            // Credit Transactions here is actually all transactions since the last invoice.
            // These are discarded after looking for all of the credit transactions.  Wasteful?
            // TODO: Figure out a way to use the calculation service to get the total credit amount...
            Collection transactions = CoreTransactionSupportHelper.get(ctx).getTransactionsForSubscriberID(ctx, 
                    subscriberId, 
                    invoice.getInvoiceDate(), 
                    CalendarSupportHelper.get(ctx).getRunningDate(ctx));
            
            long credit = getTotalCreditAmount(ctx, transactions);
            owing = service.getLastInvoiceAmountForSubscriber(ctx, subscriberId) + credit;
        }
        
        return owing;
    }


    private static long getTotalCreditAmount(Context ctx, Collection<Transaction> transactions)
    {
        AdjustmentTypeCategoryAccumulator payments = new AdjustmentTypeCategoryAccumulator(
                ctx, AdjustmentTypeEnum.StandardPayments);

        AdjustmentTypeCategoryAccumulator otherCharges = new AdjustmentTypeCategoryAccumulator(
                ctx, AdjustmentTypeEnum.Other);


        Iterator transactionIterator = transactions.iterator();
        while (transactionIterator.hasNext())
        {
            final Transaction transaction = (Transaction) transactionIterator.next();
            payments.accumulate(transaction);
            if (transaction.getAmount() < 0)
            {     
                otherCharges.accumulate(transaction);
            }     
        }

        return payments.getAmount() + otherCharges.getAmount();

    }


    private static Collection getTransactionsForAccountHierachy(
            Context context,
            Account account,
            Date start,
            Date end) throws HomeException
            {
        Iterator it = AccountSupport.getNonResponsibleAccounts(context, account).iterator();
        ArrayList allTrans = new ArrayList();
        while (it.hasNext())
        {
            Account subAccount = (Account)it.next();
            Collection subList = CoreTransactionSupportHelper.get(context).getTransactionsForAccount(context, subAccount.getBAN(), start,end);
            allTrans.addAll(subList);
        }

        return allTrans;

            }    

    public static Subscriber getFirstSubscriber(final Context ctx, 
            final int spid, 
            final Collection subs)
    throws HomeException
    {
    	Subscriber sub = null; 
    	
        Iterator sub_itr = subs.iterator();
       
        while (sub_itr.hasNext())
        {
        	Subscriber nextSub = (Subscriber) sub_itr.next();
        	
        	if (sub == null || nextSub.getStartDate().before(sub.getStartDate()))
            {
        		sub = nextSub;  
            }
        }

        if ( sub == null)
        {	
        	throw new HomeException("No postpaid subscriber found under the account");
        }
        
        return sub; 
    }


    /**
     * The Collection of payable Subscribers includes INACTIVE subscriptions for now, 
     * to be filtered later.
     * @param ctx
     * @param acct
     * @return
     * @throws HomeException
     */
    public static Collection getPayableSubscribers(Context ctx, Account acct)
    throws HomeException
    {
        return getPostpaidSubscribers(ctx, acct, 
                SubscriberStateEnum.IN_ARREARS,  
                SubscriberStateEnum.NON_PAYMENT_WARN,
                SubscriberStateEnum.IN_COLLECTION,
                SubscriberStateEnum.PROMISE_TO_PAY,
                SubscriberStateEnum.SUSPENDED,
                SubscriberStateEnum.INACTIVE,
                SubscriberStateEnum.NON_PAYMENT_SUSPENDED,
                SubscriberStateEnum.ACTIVE); 
    }


    /**
     * Remove INACTIVE subscriptions from the given collection.
     * @param subs
     * @return
     */
    public static Collection removeDeactiveSubscriber(Collection subs)
    {
        Collection ret = new ArrayList(); 
        if (subs == null)
        {
            return ret; 
        }

        for(Iterator i = subs.iterator(); i.hasNext(); )
        {
            Subscriber sub = (Subscriber)i.next();
            if (!sub.getState().equals(SubscriberStateEnum.INACTIVE))
            {
                ret.add(sub); 
            }
        }

        return ret; 
    }


    public static boolean isAmountSmallerThanLowestCurrencyUnit(final Context ctx,
            final long acctPayment, 
            final int activeSubs)
    {
        final GeneralConfig config = (GeneralConfig) ctx.get(GeneralConfig.class); 
        final long currencyPrecision = (config.getCurrencyPrecision());
        final long currentCurrency = acctPayment/activeSubs;
        return  currentCurrency <= currencyPrecision;
    }



    public static Collection<Subscriber> getPostpaidSubscribers(final Context ctx,
            final Account acct,
            final SubscriberStateEnum... states)
            throws HomeException
    {
        Collection<Subscriber> cl = AccountSupport.getNonResponsibleSubscribers(ctx, acct);

        cl = CollectionSupportHelper.get(ctx).findAll(ctx, cl, new EQ(SubscriberXInfo.SUBSCRIBER_TYPE, SubscriberTypeEnum.POSTPAID));
        if (states != null && states.length > 0)
        {
            cl = CollectionSupportHelper.get(ctx).findAll(ctx, cl, new InOneOfStatesPredicate(states));
        }

        return cl;
    }   
    

   

    /**
     * Transforms the list of subscribers into a pipeline of subscirber ids in
     * one string
     * @param subs the array of subscribers to be tranformed
     * @return the array of subscribers in the format sudId1|subId2
     */
    public static String getSubscriberIdsAsString(final Collection subs)
    {
        final StringBuffer subCreator = new StringBuffer();
        Iterator iter = subs.iterator();
        while(iter.hasNext())
        {
            if (subCreator.toString().length() != 0)
            {
                subCreator.append("|");
            }
            subCreator.append(iter.next());
        }
        return subCreator.toString();
    }


    public static Collection getSubscribers(Context ctx, Collection subids)
    {
        ArrayList ret = new ArrayList();
        for(Iterator i= subids.iterator(); i.hasNext();)
        {
            try
            {
                ret.add(SubscriberSupport.lookupSubscriberForSubId(ctx, (String)i.next()));
            }catch (HomeException e)
            {

            }
        }

        return ret; 
    }


    public static PaymentException createOrUpdatePaymentExceptionRecord(
            final Context ctx, 
            final TPSRecord tps, 
            final PaymentFailureTypeEnum failureType,
            final String msisdn, 
            final int adjustmentType)
    {
        PaymentException record = PaymentExceptionSupport.getPaymentException(
                ctx, 
                tps.getAccountNum(),
                msisdn, 
                tps.getPaymentDate(),
                adjustmentType,
                failureType); 


        if (record == null)
        {
            return createPaymentException(ctx, tps, failureType, msisdn, adjustmentType); 
        }
        else
        {
            return upatePaymentException(ctx, tps, failureType, msisdn, adjustmentType, record); 
        }
    }        


    public static PaymentException createPaymentException(final Context ctx, 
            final TPSRecord tps, 
            final PaymentFailureTypeEnum failureType,
            final String msisdn, 
            final int adjustmentType)

    {
        PaymentException record = new PaymentException();
        record.setType(failureType);
        record.setBan(tps.getAccountNum());
        record.setMsisdn(msisdn);
        record.setAmount(tps.getAmount());

        record.setAgent(tps.getTpsFileName());
        record.setTpsFileName(tps.getTpsFileName());

        record.setAdjustmentType(adjustmentType);
        record.setTransDate(tps.getPaymentDate());
        // TODO: Transaction to External Trans??
        record.setExtTransactionId(tps.getTransactionNum());

        record.setLocationCode(tps.getLocationCode());
        record.setPaymentDetails(tps.getPaymentDetail());
        record.setTransactionMethod(getPaymentMethod(tps));

        try
        {
            Home home = (Home) ctx.get(PaymentExceptionHome.class); 
            record = (PaymentException) home.create(record);
            new OMLogMsg(Common.OM_MODULE, Common.OM_PAYMENT_EXCEPTION_CREATED, 1).log(ctx);
        }
        catch (HomeException e)
        {
            if (LogSupport.isDebugEnabled(ctx))
            {
                LogSupport.debug(ctx, 
                        "PaymentExcpetionSupport.createOrUpdatePaymentExceptionRecord", 
                        " Failed to create the PaymentException Record=" + record);
            }
        }            

        return record; 

    }

    public static PaymentException upatePaymentException(final Context ctx, 
            final TPSRecord tps, 
            final PaymentFailureTypeEnum failureType,
            final String msisdn, 
            final int adjustmentType, 
            PaymentException record)
    {
        record.setAttempts(record.getAttempts() + 1);
        record.setLastAttemptDate(new Date());
        record.setLastAttemptAgent(record.getAgent());

        try
        {
            Home home = (Home) ctx.get(PaymentExceptionHome.class); 
            record = (PaymentException) home.store(record);
            new OMLogMsg(Common.OM_MODULE, Common.OM_PAYMENT_EXCEPTION_UPDATED, 1).log(ctx);
        }
        catch (HomeException e)
        {
            if (LogSupport.isDebugEnabled(ctx))
            {
                LogSupport.debug(ctx, 
                        "PaymentExcpetionSupport.createOrUpdatePaymentExceptionRecord", 
                        " Failed to store the PaymentException Record=" + record);
            }
        }

        return record; 
    }


    public static int getPaymentMethod(TPSRecord tps)
    {

        int ret = SystemTransactionMethodsConstants.TRANSACTION_METHOD_CASH;

        if ( tps.getPaymentMethod() != null)
        {	
        	switch (tps.getPaymentMethod().getIndex())
        	{
        		case PaymentMethodEnum.CA_INDEX:
        			ret = SystemTransactionMethodsConstants.TRANSACTION_METHOD_CASH;
        			break;
        		case PaymentMethodEnum.CC_INDEX:
        			ret = SystemTransactionMethodsConstants.TRANSACTION_METHOD_CREDIT_CARD;
        			break;
        		case PaymentMethodEnum.CH_INDEX:
        			ret = SystemTransactionMethodsConstants.TRANSACTION_METHOD_CHEQUE;
        			break;
        		case PaymentMethodEnum.DC_INDEX:
        			ret = SystemTransactionMethodsConstants.TRANSACTION_METHOD_DEBIT_CARD;
        			break;
        		case PaymentMethodEnum.TB_INDEX:
        			ret = SystemTransactionMethodsConstants.TRANSACTION_METHOD_TELEANKING;
        			break;
        		default:
        	}		
        }

        return ret; 
    }    
   
    private static Collection<MsisdnMgmtHistory> retrieveMsisdnMgmtHistory(final Context ctx, final String msisdn, final Date date) throws HomeException
    {
        final Home msisdnHome = (Home) ctx.get(MsisdnMgmtHistoryHome.class);
        
        HistoryEventSupport historySupport = (HistoryEventSupport) ctx.get(HistoryEventSupport.class);
        And filter = new And();
        filter.add(new EQ(MsisdnMgmtHistoryXInfo.TERMINAL_ID,msisdn));
        filter.add(new Or()
            .add(new EQ(MsisdnMgmtHistoryXInfo.EVENT, Long.valueOf(historySupport.getSubIdModificationEvent(ctx).getId())))
            .add(new EQ(MsisdnMgmtHistoryXInfo.EVENT, Long.valueOf(historySupport.getCustomerSwapEvent(ctx).getId()))));
        filter.add(new LTE(MsisdnMgmtHistoryXInfo.TIMESTAMP, CalendarSupportHelper.get(ctx).getDayAfter(date)));

        
        return msisdnHome.where(ctx, filter).selectAll();
    }
    

    
    private static Subscriber getPostpaidSubscriberEntitledToReceivePayment(Context ctx, Collection<MsisdnMgmtHistory> msisdns, String msisdn, String ban,  Date date) throws HomeException
    {
        final PostpaidSubscribersVisitor postpaidVisitor = new PostpaidSubscribersVisitor(ban);
        
        try
        {
            Visitors.forEach(ctx, msisdns, postpaidVisitor);
        }
        catch (AgentException e)
        {
            throw new HomeException(e);
        }

        if (postpaidVisitor.getPostpaidSubscribers().size() == 0) // no subscriber
        {
            throw new SubscriberNotFoundHomeException("No Postpaid subscriber found for MSISDN = " + msisdn
                    + " and transaction date = " + date);

        }
        else if (postpaidVisitor.getPostpaidSubscribers().size() == 1) // 1 subscriber
        {
            return postpaidVisitor.getPostpaidSubscribers().iterator().next();
        }
        else
        {
            // More than one postpaid subscriber. Calculate the oustanding value for them.
            return getPostpaidSubscriberWithOutstandingBalance(ctx, msisdn, date, postpaidVisitor.getPostpaidSubscribers());
        }
    }
    
    
    
    private static Subscriber getPostpaidSubscriberWithOutstandingBalance(Context ctx, String msisdn, Date date, Set<Subscriber> postpaidSubscribers) throws HomeException
    {
        final TotalOutstandingSubscriberVisitor visitor = new TotalOutstandingSubscriberVisitor();

        try
        {
            Visitors.forEach(ctx, postpaidSubscribers, visitor);
        }
        catch (AgentException e)
        {
            throw new HomeException(e);
        }

        if (visitor.getOutstandingSubscribers().size() > 0) // subscribers with outstanding balance
        {
            return retrieveUniqueSubscriberFromSet(ctx, visitor.getOutstandingSubscribers(), msisdn, date, "have an outstanding balance");          
        }
        else if (visitor.getActiveSubscriber().size() > 0) // active subscribers
        {
            return retrieveUniqueSubscriberFromSet(ctx, visitor.getActiveSubscriber(), msisdn, date, "is in active state");          
        }
        else // no owing and no active subscriber, give over payment to anyone.
        {
            return SubscriberSupport.lookupSubscriberForSubId(ctx,
                    TPSSupport.getLastMsisdnHolderID(ctx, msisdn, visitor.getPostpaidSubscribersIDs()));
        }
    }
    
    
    
    private static Subscriber retrieveUniqueSubscriberFromSet(Context ctx, Set<Subscriber> subscribers, String msisdn, Date date, String multipleSubscribersErrorMessage) throws HomeException
    {
        if (subscribers.size() == 1) // one sub 
        {
            return subscribers.iterator().next();
        }
        else if (subscribers.size() > 1) // more than one. give to last msisdn owner.
        {
            final String subscriberList = getSubscriberIdsAsString(subscribers);
            throw new DuplicateMSISDNException("Payment rejected because more than 1 subscribers with msisdn=" + msisdn
                    + ",startDate<=" + date + "] " + multipleSubscribersErrorMessage + " [subscribers=" + subscriberList
                    + ". Please refer to the ER 1124 for more details", subscriberList);
        }
        else
        {
            return null;
        }
    }
	   

    public static Subscriber getSubscriber(Context ctx, String msisdn, String ban, Date transDate) throws HomeException
    {

        final Collection<MsisdnMgmtHistory> msisdns = retrieveMsisdnMgmtHistory(ctx, msisdn, transDate);

        if (msisdns.size() == 1)
        {
            Subscriber subscriber = SubscriberSupport.lookupSubscriberForSubId(ctx, (String) msisdns.iterator().next()
                    .getSubscriberId());
            if (subscriber == null || subscriber.isPrepaid())
            {
                throw new SubscriberNotFoundHomeException("No Postpaid subscriber found for MSISDN = " + msisdn
                        + " and transaction date = " + transDate);
            }
            else
            {
                return subscriber;
            }
        }
        else if (msisdns.size() > 1)
        {
            return getPostpaidSubscriberEntitledToReceivePayment(ctx, msisdns, msisdn, ban, transDate);
        }
        else
        {
            throw new SubscriberNotFoundHomeException("No Postpaid subscriber found for MSISDN = " + msisdn
                    + " and transaction date = " + transDate);
        }

    }	  

	   
	    public static String getLastMsisdnHolderID(Context ctx, String msisdn, Collection <String> subs)
	    {
	    	String  ret = null;
	    	Home home = (Home) ctx.get(MsisdnMgmtHistoryHome.class);
	    	Date lastDate = null; 
	    	
	    	for (String subId : subs)
	    	{
	    		try
	    		{
	    			And and = new And(); 
	    	        and.add(new EQ(MsisdnMgmtHistoryXInfo.TERMINAL_ID,  msisdn));
	    	        and.add(new EQ(MsisdnMgmtHistoryXInfo.SUBSCRIBER_ID, subId)); 
	    	        
	    	        LastMsisdnHolderVisitor visitor = new LastMsisdnHolderVisitor(); 
	    	        
	            	final Home filteredHome = home.where(ctx, and);
	                
	            	filteredHome.forEach(ctx, visitor);
	            	
	            	if (visitor.getLastTimestamp() != null)
	            	{
	            		if (lastDate == null)
	            		{
	            			ret = subId; 
	            			lastDate = visitor.getLastTimestamp();
	            		} else if ( visitor.getLastTimestamp().after(lastDate))
	            		{
	            			ret = subId; 
	            			lastDate = visitor.getLastTimestamp();
	            			
	            		}
	            		
	            	}

	    	        
	    		} catch (Exception e)
	    		{
	    			new MinorLogMsg(ConveragedAccountSubscriberLookupAgent.class, "failed to get msisdn history for subscriber " + subId + " and msisdn " + msisdn, e).log(ctx); 
	    		}
	    		   
	    		
	    	}
	    	
	    	
	    	
	    	return ret; 
	    }
}

