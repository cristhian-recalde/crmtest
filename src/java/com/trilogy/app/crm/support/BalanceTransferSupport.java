/**
 * @Filename : BalanceTransferSupport.java
 * @Author   : Daniel Zhang
 * @Date     : Jul 21, 2004
 * 
 *  Copyright (c) Redknee, 2004
 *        - all rights reserved
 */

package com.trilogy.app.crm.support;

import java.util.Date;

import com.trilogy.app.crm.bas.recharge.RecurRechargeRequest;
import com.trilogy.app.crm.bas.recharge.SubscriberMisConfigedException;
import com.trilogy.app.crm.bean.AbstractNote;
import com.trilogy.app.crm.bean.AdjustmentTypeEnum;
import com.trilogy.app.crm.bean.Note;
import com.trilogy.app.crm.bean.NoteHome;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberStateEnum;
import com.trilogy.app.crm.bean.SubscriberTypeEnum;
import com.trilogy.app.crm.bean.SystemNoteSubTypeEnum;
import com.trilogy.app.crm.bean.SystemNoteTypeEnum;
import com.trilogy.app.crm.bean.TransactionHome;
import com.trilogy.app.crm.bean.core.Transaction;
import com.trilogy.app.crm.calculation.service.CalculationService;
import com.trilogy.app.crm.calculation.service.CalculationServiceException;
import com.trilogy.app.crm.calculation.service.CalculationServiceInternalException;
import com.trilogy.app.crm.extension.subscriber.PPSMSupporteeSubExtension;
import com.trilogy.app.crm.log.ERLogger;
import com.trilogy.app.crm.xhome.home.OcgTransactionException;
import com.trilogy.framework.xhome.beans.XBeans;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.framework.xlog.log.MinorLogMsg;

/**
 * Class Description:
 */
public class BalanceTransferSupport {
	
    public static Transaction createDebitTransaction(final Context ctx, final Transaction creditTransaction,
            final Subscriber supportedSubscriber, final Subscriber supporterSubscriber, boolean recurringRecharge) throws HomeException
    {
        Transaction debitTransaction;
        boolean success = true;
        
        try
        {
            debitTransaction = (Transaction) XBeans.instantiate(Transaction.class, ctx);
        }
        catch (Exception exception)
        {
            debitTransaction = new Transaction();
            LogSupport.minor(ctx, BalanceTransferSupport.class, "Unable to instantiate Transaction object using XBeans. Using an empty one.");
        }

        final long amount = creditTransaction.getAmount();
        try        
        {
            final Context subContext = ctx.createSubContext();
            subContext.put(Subscriber.class, supporterSubscriber);
            // for PoP
            debitTransaction.setBAN(supporterSubscriber.getBAN());
            debitTransaction.setResponsibleBAN(supporterSubscriber.getAccount(ctx).getResponsibleBAN());
            debitTransaction.setMSISDN(supporterSubscriber.getMSISDN());
            debitTransaction.setSubscriberID(supporterSubscriber.getId());
            debitTransaction.setSpid(supporterSubscriber.getSpid());
             //TODO: what adjustment type should be: Balance Transfer
            debitTransaction.setAdjustmentType(AdjustmentTypeSupportHelper.get(subContext).getAdjustmentTypeCodeByAdjustmentTypeEnum(subContext,
                AdjustmentTypeEnum.BalanceTransferDebit));
            debitTransaction.setAmount(-amount);
            debitTransaction.setReceiveDate(creditTransaction.getReceiveDate());
            debitTransaction.setTaxPaid(0);
            debitTransaction.setAgent(creditTransaction.getAgent());
            debitTransaction.setTransactionMethod(creditTransaction.getTransactionMethod());
            debitTransaction.setSubscriptionCharge(false);
            debitTransaction.setSubscriberType(SubscriberTypeEnum.POSTPAID);
            debitTransaction.setInternal();
            debitTransaction.setGLCode(creditTransaction.getGLCode());
            debitTransaction.setReasonCode(creditTransaction.getReasonCode());
            debitTransaction.setSubscriptionTypeId(creditTransaction.getSubscriptionTypeId());
             //
             // save postpaid first
            final Home home = (Home) subContext.get(TransactionHome.class);
            
            debitTransaction = (Transaction) home.create(subContext, debitTransaction);
            
            return debitTransaction;
        }
        catch (final OcgTransactionException ocge)
        {
            ERLogger.genAccountAdjustmentER(ctx, debitTransaction, ocge.getErrorCode(),
                    RecurRechargeRequest.FAIL_OTHERS);
            success = false;
            throw ocge;
        }
        finally
        {
            if (!success)
            {
                SystemNoteTypeEnum typeEnum;
                
                if (recurringRecharge)
                {
                    typeEnum = SystemNoteTypeEnum.RECURRINGCHARGE;
                }
                else
                {
                    typeEnum = SystemNoteTypeEnum.ADJUSTMENT;
                }
                
                BalanceTransferSupport.addSubscriberNote(ctx, supportedSubscriber, debitTransaction.getAdjustmentType(),
                        "Unable to debit PPSM supporter subscriber.", typeEnum,
                        SystemNoteSubTypeEnum.BALANCETRANSFER_FAIL_PoD);
            }
        }        
    }

    
	public static Subscriber validateSubscribers(
			Context ctx, 
			Subscriber subscriber) 
	throws HomeException
	{
        // get supporting subscriber
		PPSMSupporteeSubExtension ppsmExtension = PPSMSupporteeSubExtension.getPPSMSupporteeSubscriberExtension(ctx, subscriber.getId());
   	    if(ppsmExtension==null)
   	    {
            throw new SubscriberMisConfigedException("Subscriber '" + subscriber.getId() + "' (MSISDN="
                    + subscriber.getMSISDN()
                    + ") does not have PPSM Supportee extension but has ordered a transfer service.", null);
   	    }
   	    
        String postpaidMsisdn = ppsmExtension.getSupportMSISDN();
   	      	    
   	   // Service svc=request.getService();
   	    
        Subscriber supporterSubscriber = SubscriberSupport.lookupSubscriberForMSISDN(ctx, postpaidMsisdn);
   	    
        if(supporterSubscriber==null)
   	    {
   	    	throw new SubscriberMisConfigedException
			("Prepaid subscriber :"+subscriber.getMSISDN()+ 
					" has an invalid support msisdn", null);
   	    }
   	    if(subscriber.getSpid() != supporterSubscriber.getSpid())
   	    {
   	    	throw new SubscriberMisConfigedException
			("Subscriber '" + subscriber.getId() + "' (MSISDN="
                    + subscriber.getMSISDN()
                    + ") has a support MSISDN not in the same SPID.", null);
   	    }
   	    if(!(supporterSubscriber.getSubscriberType().equals(SubscriberTypeEnum.POSTPAID)))
   	    {
            throw new SubscriberMisConfigedException
            ("Subscriber '" + subscriber.getId() + "' (MSISDN="
                    + subscriber.getMSISDN()
                    + ") have a support MSISDN that is not POSTPAID.",null);
   	    }

   	    if((supporterSubscriber.getState().equals(SubscriberStateEnum.INACTIVE)))
	    {
            throw new SubscriberMisConfigedException
            ("Subscriber '" + subscriber.getId() + "' (MSISDN="
                    + subscriber.getMSISDN()
                    + ") have a support MSISDN that is DEACTIVATED.",null);
	    }

   	    return supporterSubscriber;
	}
	
    
    /**
     * Creates subacriber notes for the failed transaction
     *
     * @param ctx The  Context.
     * @param sub The  subscriber.
     *
     * @exception HomeException Thrown if there is a problem accessing data in
     * the given context.
     */
    public static void addSubscriberNote(Context ctx, Subscriber sub, 
    		int adjType, String msg, SystemNoteTypeEnum notetype, 
			SystemNoteSubTypeEnum notesubtype)
        throws HomeException
    {
        final Home home = (Home)ctx.get(NoteHome.class);

        if (home == null)
        {
            throw new HomeException("System error: no NoteHome found in context.");
        }

        //Subscriber note.
        {
            final Note note = new Note();
            note.setIdIdentifier(sub.getId());
            note.setAgent(SystemSupport.getAgent(ctx));
            note.setCreated(new Date());
            note.setType(notetype.getDescription());
            note.setSubType(notesubtype.getDescription());
            String notemsg="Balance transfer for subscriber ["+ sub.getMSISDN()+"] for adjustment type "+
            	adjType+ " failed. " + msg;
            if(notemsg.length()> AbstractNote.NOTE_WIDTH)
            {
            	notemsg=notemsg.substring(0,AbstractNote.NOTE_WIDTH);
            }
            note.setNote(notemsg);           

            try
            {
                home.create(ctx,note);
            }
            catch (final HomeException exception)
            {
                new MinorLogMsg(
                    "BalanceTransferSupport.addSubscriberNote",
                    "Failed to create subscriber note for balance Transfer.",
                    exception).log(ctx);
            }
        }
    }
    
    /*
     *  This fix is provided to humming bird : TT# 9022300052 : Misleading msg error when sub owns more money then the credit limit and doing a bal transfer.
     *  Before balance transfer , need to check whether transfer amount is gretter than creditLimit - amountOwedBySubscriber.
     *  TT: 9022300052
     *  
     */
    public static String validateAmount(Subscriber prepaidSub, Transaction transaction,Context ctx) 
    		throws SubscriberMisConfigedException
    		{
    	
    	 		CalculationService service = (CalculationService) ctx.get(CalculationService.class);
    	 		Subscriber postpaidSubscriber = null;
    	 		
    	 		 long amountOwedBySubscriber	= 0;
    	 		 long creditLimit 				= 0;        	     
        		 long amount 					= transaction.getAmount();    			
    		     long remainingBalance 			= 0;
    		     String creditChkStr			= "0"; 
    		    
				try {
					
					postpaidSubscriber = SubscriberSupport.getSubscriber(ctx, transaction.getSubscriberID());
					amountOwedBySubscriber = service.getAmountOwedBySubscriber(ctx,transaction.getSubscriberID());
					creditLimit = postpaidSubscriber.getCreditLimit(ctx);
					
				} catch (CalculationServiceException e) {					
					 new MinorLogMsg(BalanceTransferSupport.class.getSimpleName(), "Error retrieving Amount Owed By Subscriber" + ": " + e.getMessage(), e).log(ctx);
				} catch (final HomeException e1) {
					 new MinorLogMsg(BalanceTransferSupport.class.getSimpleName(), e1.getMessage(), e1).log(ctx);
				}
        		
    	   	    if(amount >= 0)
    		    {
    		    	throw new SubscriberMisConfigedException
    				("Invalid balance transfer amount for prepaid subscriber '" + prepaidSub.getId() +"' (" + 
    				        prepaidSub.getMSISDN() + "). Amount must be negative.", null);
    		    }
    	   	    
    	   	    creditChkStr = "amount:::"+amount+":::amountOwedBySubscriber:::"+amountOwedBySubscriber+":::creditLimit"+creditLimit;
    	   	 
    	   	    if(creditLimit > amountOwedBySubscriber)
    	   	    {
    	   	    	return creditChkStr;
    	   	    }
    	   	    
    	   	    remainingBalance = creditLimit - amountOwedBySubscriber ;
    	   	    
    	   	     creditChkStr = creditChkStr+"::remainingBalance::"+remainingBalance;
    	   	 
    	   	    //Amount is negative and we need to compare over positive balance
    	   	    if ((remainingBalance < 0) || //Owes money 
    	   	    		(-amount > remainingBalance))
    	   	    {
    		    	throw new SubscriberMisConfigedException
    				("Balance transfer amount exceeds the remaining credit limit of postpaid subscriber '" + 
    						postpaidSubscriber.getId() + "' (" + postpaidSubscriber.getMSISDN() + ").", null);
    	   	    }  	   	    
    	   	    
    	   	    return creditChkStr;
    		}
}
