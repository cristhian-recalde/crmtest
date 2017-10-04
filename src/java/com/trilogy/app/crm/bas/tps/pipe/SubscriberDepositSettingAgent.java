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
package com.trilogy.app.crm.bas.tps.pipe;

import java.util.Date;

import com.trilogy.framework.core.locale.Currency;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextAgent;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xlog.log.EntryLogMsg;
import com.trilogy.framework.xlog.log.LogSupport;

import com.trilogy.app.crm.Common;
import com.trilogy.app.crm.bean.AbstractSubscriber;
import com.trilogy.app.crm.bean.AdjustmentTypeEnum;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberHome;
import com.trilogy.app.crm.bean.SystemNoteSubTypeEnum;
import com.trilogy.app.crm.bean.SystemNoteTypeEnum;
import com.trilogy.app.crm.bean.TransactionHome;
import com.trilogy.app.crm.bean.core.AdjustmentType;
import com.trilogy.app.crm.bean.core.Transaction;
import com.trilogy.app.crm.log.ERLogger;
import com.trilogy.app.crm.report.ReportUtilities;
import com.trilogy.app.crm.support.AdjustmentTypeSupportHelper;
import com.trilogy.app.crm.support.CoreTransactionSupportHelper;
import com.trilogy.app.crm.support.NoteSupport;
import com.trilogy.app.crm.support.NoteSupportHelper;
import com.trilogy.app.crm.support.SpidSupport;
import com.trilogy.app.crm.support.TransactionSupport;
import com.trilogy.app.crm.tps.pipe.TPSPipeConstant;


/**
 * Reset the deposit of subscriber, save the subscriber to subscriber home.
 *
 * @author larry.xia@redknee.com
 */
public class SubscriberDepositSettingAgent extends PipelineAgent
{

    /**
     * Serial version UID.
     */
    private static final long serialVersionUID = -7735486914582133983L;

    /**
     * Prefix for deposit payment note.
     */
    private static final String DEPOSIT_PAYMENT = "Deposit Payment for ";


    /**
     * Creates a new instance of SubscriberDepositSettingAgent.
     *
     * @param delegate
     *            The delegate of this agent.
     */
    public SubscriberDepositSettingAgent(final ContextAgent delegate)
    {
        super(delegate);
    }


    /**
     * Updates the deposit in the subscriber profile.
     *
     * @param ctx
     *            A context.
     */
    @Override
    public void execute(final Context ctx)
    {
        // TODO 2007-06-08 navigating the context is BAD
        final Home home = (Home) ((Context) ctx.get("..")).get(SubscriberHome.class);
        final Subscriber sub = (Subscriber) ctx.get(Subscriber.class);
        final Transaction trans = (Transaction) ctx.get(Transaction.class);

        long oldSubDeposit = sub.getDeposit(ctx);
        Date oldSubDepositDate = sub.getDepositDate();
        boolean subscriberUpdated = Boolean.FALSE;
        
        try
        {
            if (sub.getContextInternal() == null)
            {
                sub.setContext(ctx);
                if (LogSupport.isDebugEnabled(ctx))
                {
                    LogSupport.debug(ctx, this, "Subscriber context is null!", new Exception());
                }
            }
            final long deposit = sub.getDeposit(ctx);

            if (deposit == AbstractSubscriber.DEFAULT_DEPOSIT)
            {
                sub.setDeposit(trans.getAmount());
            }
            else
            {
                sub.setDeposit(deposit + trans.getAmount());
            }
            
            // amedina: 6.0 sets the new deposit date for the change
            sub.setDepositDate(trans.getTransDate());
            
            /* Moving subscriber update before the payment transaction - TT # 12062659023
             * Subscriber pipeline validation (including MSL <= CL check) to happen before payment transaction is created
            */
            home.store(ctx, sub);
            subscriberUpdated = Boolean.TRUE;
        }
        catch (final Exception e)
        {
            ERLogger.genAccountAdjustmentER(ctx, trans, TPSPipeConstant.RESULT_CODE_UPS_RESULT_NOT_APPLY,
                TPSPipeConstant.FAIL_UPDATE_SUSCRIBER_TABLE);

            // send out alarm
            new EntryLogMsg(10534, this, "", "", new String[]
            {
                "Fail update credit limit and deposit fields of subscriber",
            }, e).log(ctx);
            fail(ctx, this, e.getMessage(), e, TPSPipeConstant.FAIL_UPDATE_SUSCRIBER_TABLE);
        }
        
            /*
             * [Cindy] 2007-11-12: Convert deposit release to payment if option is
             * enabled.
             */
        try 
        {
				if (subscriberUpdated && CoreTransactionSupportHelper.get(ctx).isDepositRelease(ctx, trans)
				    && SpidSupport.isAutoConvertingDepositToPaymentOnRelease(ctx, sub.getSpid()))
				{
				    long convertedPayment = 0;

				    /*
				     * [Cindy] 2008-01-18: The full deposit is converted into payment if this
				     * is not a subscriber move.
				     */
				    final Long delta = (Long) ctx.get(Common.SUBSCRIBER_MOVE_DEPOSIT_DELTA, null);
				    if (delta == null)
				    {
				        convertedPayment = trans.getAmount();
				    }
				    else
				    {
				        if (delta.longValue() > 0)
				        {
				            convertedPayment = -delta.longValue();
				        }
				    }

				    if (convertedPayment < 0)
				    {
				        final Context subCtx = ctx.createSubContext();
				        subCtx.put(SubscriberCreditLimitUpdateAgent.ENABLE_PROCESSING, false);
				        final int code = AdjustmentTypeSupportHelper.get(subCtx).getAdjustmentTypeCodeByAdjustmentTypeEnum(subCtx,
				            AdjustmentTypeEnum.PaymentConvertedFromDeposit);
				        final AdjustmentType type = AdjustmentTypeSupportHelper.get(subCtx).getAdjustmentTypeForRead(subCtx, code);
				        
				        /*
				         *  Original Transaction Home get from UpsForwardTransactionHome for calling from head of transaction pipeline
				         *  Here "orgTransactionHome" object is Original Transaction Home and "newTransactionHome" is in between of transaction pipeline inside 
				         *  UpsForwardTransactionHome
				         */
				        
				        Home orgTransactionHome = (Home)subCtx.get("ORG_TRANSACTION_HOME");
				        Home newTransactionHome = (Home)subCtx.get(TransactionHome.class);
				        
				        if(orgTransactionHome != null){
				            subCtx.put(TransactionHome.class, orgTransactionHome);
				        }
				        subCtx.put("IS_DEPOSIT_RELEASE_DONE", true);
				        TransactionSupport.createTransaction(subCtx, sub, (convertedPayment * -1), type);
				        
				        // restore the newTransactionHome to context for normal process
				        subCtx.put(TransactionHome.class, newTransactionHome);
				       
				    }
				}

            // TransactionSupport.createInterestPayment(ctx,sub,trans,lastDeposit);

            final Currency currency =
                ReportUtilities.getCurrency(ctx, sub.getCurrency(ctx));
            
            NoteSupportHelper.get(ctx).addSubscriberNote(ctx, sub.getId(), DEPOSIT_PAYMENT + currency.formatValue(trans.getAmount()), SystemNoteTypeEnum.ADJUSTMENT,
                SystemNoteSubTypeEnum.SUBUPDATE);

           // home.store(ctx, sub);
            if (subscriberUpdated)
            {
            	pass(ctx, this, "credit limit and deposit fields updated");
            }

        } 
        catch (final Exception e)
        {
        	
        	LogSupport.major(ctx, this, "Payment transaction failure encountered. Reverting the Subscriber Deposit update.", e);
        	
        	ERLogger.genAccountAdjustmentER(ctx, trans, TPSPipeConstant.RESULT_CODE_UPS_RESULT_NOT_APPLY,
                    TPSPipeConstant.FAIL_UPDATE_SUSCRIBER_TABLE);

                // send out alarm
                new EntryLogMsg(10534, this, "", "", new String[]
                {
                    "Fail update credit limit and deposit fields of subscriber",
                }, e).log(ctx);
                fail(ctx, this, e.getMessage(), e, TPSPipeConstant.FAIL_UPDATE_SUSCRIBER_TABLE);
        	
        	//TT # 12062659023 - revert the subscriber deposit update, if the payment transaction fails
        	sub.setDeposit(oldSubDeposit);
        	sub.setDepositDate(oldSubDepositDate);
        	
        	try 
        	{
				home.store(ctx, sub);
			}
        	catch (final Exception ex)
	        {
        		LogSupport.major(ctx, this, "Failure while reverting the Subscriber Deposit update.", ex);
	        }
            
        }

    }

}
