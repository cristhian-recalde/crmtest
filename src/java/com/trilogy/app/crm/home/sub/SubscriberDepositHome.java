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
package com.trilogy.app.crm.home.sub;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.trilogy.app.crm.LicenseConstants;
import com.trilogy.app.crm.bas.tps.pipe.SubscriberCreditLimitUpdateAgent;
import com.trilogy.app.crm.bean.AdjustmentTypeEnum;
import com.trilogy.app.crm.bean.CRMSpid;
import com.trilogy.app.crm.bean.Deposit;
import com.trilogy.app.crm.bean.DepositReasonCode;
import com.trilogy.app.crm.bean.DepositReasonCodeXInfo;
import com.trilogy.app.crm.bean.DepositStatusEnum;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberStateEnum;
import com.trilogy.app.crm.bean.SubscriberTypeEnum;
import com.trilogy.app.crm.bean.core.AdjustmentType;
import com.trilogy.app.crm.bean.pipeline.PipelineAgentExtension;
import com.trilogy.app.crm.support.AdjustmentTypeSupportHelper;
import com.trilogy.app.crm.support.DepositReleaseException;
import com.trilogy.app.crm.support.HomeSupportHelper;
import com.trilogy.app.crm.support.LicensingSupportHelper;
import com.trilogy.app.crm.support.Lookup;
import com.trilogy.app.crm.support.SpidSupport;
import com.trilogy.app.crm.support.SubscriberSupport;
import com.trilogy.app.crm.support.TransactionSupport;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.And;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeInternalException;
import com.trilogy.framework.xhome.home.HomeProxy;
import com.trilogy.framework.xlog.log.LogSupport;


/**
 * This home managements subscriber's deposit in lifecycle. Provides the functionality to
 * automatically create a deposit payment for the initial deposit of a new subscriber.
 * Reset deposit on conversion,etc.
 *
 * @author joe.chen@redknee.com
 */
public class SubscriberDepositHome extends HomeProxy
{

    /**
     * Serial version UID.
     */
    private static final long serialVersionUID = 1L;


    /**
     * Create a new instance of <code>SubscriberDepositHome</code>.
     *
     * @param ctx
     *            The operating context.
     * @param delegate
     *            Delegate of this home in the pipeline.
     */
    public SubscriberDepositHome(final Context ctx, final Home delegate)
    {
        super(ctx, delegate);
    }
    
    public SubscriberDepositHome(Context ctx, Home delegate, PipelineAgentExtension<Home> extension)
    {
        this(ctx, delegate);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public Object create(final Context ctx, final Object obj) throws HomeException
    {
    	LogSupport.debug(ctx, this, "SubscriberPipeline[create].....");
    	Subscriber subscriber = (Subscriber) obj;
        subscriber = (Subscriber) super.create(ctx, subscriber);

        /*final long deposit = subscriber.getDeposit(ctx);

        //Only 
        if (!isDepositChargeLicenseEnabled(ctx))
        {
            subscriber.setDeposit(0);
        }*/

        /*
          We should only attempt to create a deposit payment if the deposit originally
         * specified for the subscriber is non-zero.
         
        if (deposit != 0)
        // && subscriber.getSubscriberType().equals(SubscriberTypeEnum.POSTPAID)
        {

            //**
           * Setting subscriber deposit amount to zero
           * TT#13022128031
           * Due to this amount, subscription is getting updated wrong from
           * transaction pipeline. refer:-SubscriberDepositSettingAgent code fragment " sub.setDeposit(deposit + trans.getAmount());"
           * Due to which subscription's deposit amount was getting updated to double of actual deposit amount which is wrong and causes
           * issues in invoice .
           * For example , deposit amount = $10 , it creates a transaction of $10 but updates the subscription again with
           * deposit amount = $20. To avoid this issue, here its being set to zero before txn creation.
           
            
           subscriber.setDeposit(0l);
           
            if (LogSupport.isDebugEnabled(ctx))
            {
                new DebugLogMsg(this, "Initial deposit value is: " + deposit, null).log(ctx);
            }
            
            if ( isDepositChargeLicenseEnabled(ctx))
            {
            	 //**
                 * Added for feature F-0001282 - Deferring Deposit Payments to First Invoice.
                 * Refer BSS.SgR.2696
                 
                if(!subscriber.getAccount(ctx).getAccountCategory(ctx).getPrepaymentDepositRequired())
                {
                    depositChargeTransaction(ctx, subscriber, deposit);
                }
                else
                {
                    depositTransaction(ctx, subscriber, deposit);
                }
            }
            else
            {
                depositTransaction(ctx, subscriber, deposit);
                
            }
            
              transaction home will update deposit for us, that is not good though. we
             * still need to fake returning to web now. JoeC
             
            subscriber.setDeposit(deposit);
        }*/

        return subscriber;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public Object store(final Context ctx, final Object obj) throws HomeException, HomeInternalException
    {
    	LogSupport.debug(ctx, this, "SubscriberPipeline[store].....");
    	Subscriber newSub = (Subscriber) obj;
        final Subscriber oldSub = (Subscriber) ctx.get(Lookup.OLDSUBSCRIBER);

        Subscriber preStoredSub = null;

        // do conversion
        if (!oldSub.getSubscriberType().equals(newSub.getSubscriberType()))
        {

            // reset deposit to zero
            if (oldSub.getSubscriberType() == SubscriberTypeEnum.POSTPAID && oldSub.getDeposit(ctx) != 0)
            {

                preStoredSub = (Subscriber) getDelegate().store(ctx, newSub);

                //depositReleaseTransaction(ctx, oldSub, oldSub.getDeposit(ctx));
            }

            // Create a new deposit for the new subscriber
           //preStoredSub = createTransactions(ctx, oldSub, newSub);

            if (preStoredSub != null)
            {
                newSub = preStoredSub;
            }

        }
        else
        {
            if (newSub.getSubscriberType() == SubscriberTypeEnum.POSTPAID
                && newSub.getState() == SubscriberStateEnum.INACTIVE)
            {
                preStoredSub = (Subscriber) getDelegate().store(ctx, newSub);
                if(SubscriberSupport.subscriberDepositReleasePostTermination(ctx, newSub))
                {
                	List<DepositStatusEnum> statusList = new ArrayList();
                		statusList.add(DepositStatusEnum.ACTIVE);
                		statusList.add(DepositStatusEnum.ONHOLD);
                	
                	Map parameters = new HashMap();
                	 parameters.put("BAN", preStoredSub.getBAN());
                	 parameters.put("SUBSCRIPTION_ID",preStoredSub.getId());
                	 parameters.put("CALCULATE_INTREST",true);
                	 
                	 parameters.put("REASON_CODE",getReasonCode(ctx,preStoredSub.getSpid()));
                	
                	 try {
						List<Deposit> depositList = com.redknee.app.crm.support.DepositSupport.getDeposits(ctx, parameters);
	        			com.redknee.app.crm.support.ReleaseDeposit deposit = com.redknee.app.crm.support.DepositReleaseFactory.releaseDeposit(2);
	        			int result = deposit.releaseDeposit(ctx, parameters, statusList, depositList);
						if(result == 0){
							LogSupport.info(ctx,this,"Deposit successfully release for "+ preStoredSub.getId());
						}else if(result == 2){
							LogSupport.major(ctx,this,"Error: Deposit partially release for "+ preStoredSub.getId()+" Please check log for more details");
							throw new HomeException("Error: Deposit partially release for "+ preStoredSub.getId());
						}else if(result == 1){
							LogSupport.major(ctx,this,"Error: Fail to release deposit for "+ preStoredSub.getId());
							throw new HomeException("Error: Fail to release deposit for "+ preStoredSub.getId());
						}else if(result == 3){
							LogSupport.info(ctx,this,"Can not found any deposit record for "+ preStoredSub.getId());
						}
					} catch (DepositReleaseException e) {
						LogSupport.major(ctx,this,"Error while release deposit for "+ preStoredSub.getId());
						throw new HomeException("Error while release deposit for "+ preStoredSub.getId());
				   }
                 //final long deposit = newSub.getDeposit(ctx);
                 //depositReleaseTransaction(ctx, newSub, deposit);
                }
            }
        }

        if (preStoredSub != null)
        {
            return preStoredSub;
        }
        return super.store(ctx, newSub);
    }

    private int getReasonCode(Context ctx, int spid) {
    	int reasonCode = -1;
    	CRMSpid spidBean = null;
         try{
        	 spidBean = SpidSupport.getCRMSpid(ctx, spid);
         }
         catch (HomeException e){
             LogSupport.minor(ctx, this, "Exception occurred while fetching spid for subscriber with id : "+spid, e);
         }
        if(spidBean!=null){
        	DepositReasonCode depositReasonCodeXDB = null;
        	try {
    			And filter = new And();
    			filter.add(new EQ(DepositReasonCodeXInfo.SPID, spid));
    			filter.add(new EQ(DepositReasonCodeXInfo.IDENTIFIER,spidBean.getReasonCodeSubcriberDeactivation()));
    			depositReasonCodeXDB = (DepositReasonCode) HomeSupportHelper.get(
    					ctx).findBean(ctx, DepositReasonCode.class, filter);
    			if(depositReasonCodeXDB!=null){
    				reasonCode = depositReasonCodeXDB.getDepositReasonCode();    				
    			}
    		} catch (Exception e) {
    			LogSupport.minor(ctx, this, "Cannot find DepositReasonCode " + e);
    			reasonCode = -1;
    		}
        }
        return reasonCode;
	}
    
    

	/**
     * Create a deposit transaction. On Transaction home, it will call
     * SubscriberHome.store again, so be sure to call home method before this method.
     *
     * @param ctx
     *            The operating context.
     * @param subscriber
     *            The subscriber to create deposit transaction for.
     * @param deposit
     *            Deposit amount.
     * @throws HomeException
     *             Thrown if there are problems creating the transaction.
     */
    protected void depositTransaction(final Context ctx, final Subscriber subscriber, final long deposit)
        throws HomeException
    {
        final Context subCtx = ctx.createSubContext();
        subCtx.put(SubscriberCreditLimitUpdateAgent.ENABLE_PROCESSING, false);
        final AdjustmentType type = AdjustmentTypeSupportHelper.get(subCtx).getAdjustmentType(subCtx, AdjustmentTypeEnum.DepositMade);
        TransactionSupport.createTransaction(subCtx, subscriber, -deposit, type);
    }


    /**
     * Create a deposit transaction. On Transaction home, it will call
     * SubscriberHome.store again, so be sure to call home method before this method.
     *
     * @param ctx
     *            The operating context.
     * @param subscriber
     *            The subscriber to create deposit transaction for.
     * @param deposit
     *            Deposit amount.
     * @throws HomeException
     *             Thrown if there are problems creating the transaction.
     */
    protected void depositChargeTransaction(final Context ctx, final Subscriber subscriber, final long deposit)
        throws HomeException
    {
        final Context subCtx = ctx.createSubContext();
        subCtx.put(SubscriberCreditLimitUpdateAgent.ENABLE_PROCESSING, false);
        final AdjustmentType type = AdjustmentTypeSupportHelper.get(subCtx).getAdjustmentType(subCtx, AdjustmentTypeEnum.DepositCharge);
        TransactionSupport.createTransaction(subCtx, subscriber, deposit, type);
    }

    
    /**
     * Create a deposit release transaction. On Transaction home, it will call
     * SubscriberHome.store again, so be sure to call home method before this method.
     *
     * @param ctx
     *            The operating context.
     * @param subscriber
     *            The subscriber to create deposit transaction for.
     * @param deposit
     *            Deposit amount.
     * @throws HomeException
     *             Thrown if there are problems creating the transaction.
     */
    protected void depositReleaseTransaction(final Context ctx, final Subscriber subscriber, final long deposit)
        throws HomeException
    {
        final Context subCtx = ctx.createSubContext();
        subCtx.put(SubscriberCreditLimitUpdateAgent.ENABLE_PROCESSING, false);
        final AdjustmentType type = AdjustmentTypeSupportHelper.get(subCtx).getAdjustmentType(subCtx, AdjustmentTypeEnum.DepositRelease);
        TransactionSupport.createTransaction(subCtx, subscriber, deposit, type);
    }


    /**
     * Create balance transactions. The new subscriber will be pushed down the subscriber
     * pipeline and stored first before the transactions are created.
     *
     * @param ctx
     *            The operating context.
     * @param oldSub
     *            The old subscriber.
     * @param newSub
     *            The new subscriber.
     * @return The resulting subscriber.
     * @throws HomeException
     *             Thrown if there are problems creating the transactions.
     */
    protected Subscriber createTransactions(final Context ctx, final Subscriber oldSub, final Subscriber newSub)
        throws HomeException
    {
        Subscriber preStoredSub = null;

        if (oldSub.getSubscriberType() == SubscriberTypeEnum.PREPAID
            && newSub.getSubscriberType() == SubscriberTypeEnum.POSTPAID)
        {
            /*
             * Resetting the deposit amount since the deposit will be taking care of when
             * creating the amount.
             */
            final long deposit = newSub.getDeposit(ctx);
            newSub.setDeposit(0);
            preStoredSub = (Subscriber) getDelegate().store(ctx, newSub);
            depositTransaction(ctx, newSub, deposit);
            if (oldSub.getRealTimeBalance() < 0)
            {
                final long abmBalance = oldSub.getRealTimeBalance();
                debitBalanceTransaction(ctx, oldSub, abmBalance);
                creditBalanceTransaction(ctx, newSub, abmBalance);
            }
        }
        return preStoredSub;
    }


    /**
     * Create a credit balance transfer transaction.
     *
     * @param ctx
     *            The operating context.
     * @param subscriber
     *            The new subscriber.
     * @param abmBalance
     *            The ABM balance.
     * @throws HomeException
     *             Thrown if there are problems creating the transaction.
     */
    protected void creditBalanceTransaction(final Context ctx, final Subscriber subscriber, final long abmBalance)
        throws HomeException
    {
        final Context subCtx = ctx.createSubContext();
        subCtx.put(SubscriberCreditLimitUpdateAgent.ENABLE_PROCESSING, false);
        final AdjustmentType type = AdjustmentTypeSupportHelper.get(subCtx).getAdjustmentType(subCtx,
            AdjustmentTypeEnum.ConversionBalanceTransfer);
        TransactionSupport.createTransaction(subCtx, subscriber, abmBalance, type);

    }


    /**
     * Create a debit balance transfer transaction.
     *
     * @param ctx
     *            The operating context.
     * @param subscriber
     *            The old subscriber.
     * @param abmBalance
     *            The ABM balance.
     * @throws HomeException
     *             Thrown if there are problems creating the transaction.
     */
    protected void debitBalanceTransaction(final Context ctx, final Subscriber subscriber, final long abmBalance)
        throws HomeException
    {
        final Context subCtx = ctx.createSubContext();
        subCtx.put(SubscriberCreditLimitUpdateAgent.ENABLE_PROCESSING, false);
        final AdjustmentType type = AdjustmentTypeSupportHelper.get(subCtx).getAdjustmentType(subCtx, AdjustmentTypeEnum.DebitBalance);
        TransactionSupport.createTransaction(subCtx, subscriber, abmBalance, type);
    }
    
    private boolean isDepositChargeLicenseEnabled(final Context ctx)
    {
        return LicensingSupportHelper.get(ctx).isLicensed(ctx, LicenseConstants.DEPOSIT_CHARGE_ADJUSTMENT);
    }

}
