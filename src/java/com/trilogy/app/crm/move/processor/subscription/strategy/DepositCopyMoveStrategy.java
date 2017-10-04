/*
 * This code is a protected work and subject to domestic and international
 * copyright law(s). A complete listing of authors of this work is readily 
 * available. Additionally, source code is, by its very nature, confidential 
 * information and inextricably contains trade secrets and other information 
 * proprietary, valuable and sensitive to Redknee, no unauthorised use, 
 * disclosure, manipulation or otherwise is permitted, and may only be used 
 * in accordance with the terms of the licence agreement entered into with 
 * Redknee Inc. and/or its subsidiaries.
 *
 * Copyright � Redknee Inc. and its subsidiaries. All Rights Reserved. 
 */
package com.trilogy.app.crm.move.processor.subscription.strategy;

import java.util.Date;

import com.trilogy.framework.xhome.beans.CompoundIllegalStateException;
import com.trilogy.framework.xhome.beans.IllegalPropertyArgumentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xlog.log.DebugLogMsg;
import com.trilogy.framework.xlog.log.InfoLogMsg;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.framework.xlog.log.MajorLogMsg;
import com.trilogy.framework.xlog.log.MinorLogMsg;

import com.trilogy.app.crm.Common;
import com.trilogy.app.crm.LicenseConstants;
import com.trilogy.app.crm.bas.tps.pipe.SubscriberCreditLimitUpdateAgent;
import com.trilogy.app.crm.bean.core.AdjustmentType;
import com.trilogy.app.crm.bean.AdjustmentTypeActionEnum;
import com.trilogy.app.crm.bean.AdjustmentTypeEnum;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberHome;
import com.trilogy.app.crm.bean.UpdateReasonEnum;
import com.trilogy.app.crm.move.MoveConstants;
import com.trilogy.app.crm.move.MoveException;
import com.trilogy.app.crm.move.processor.strategy.CopyMoveStrategy;
import com.trilogy.app.crm.move.processor.strategy.CopyMoveStrategyProxy;
import com.trilogy.app.crm.move.request.ConvertSubscriptionBillingTypeRequest;
import com.trilogy.app.crm.move.request.ServiceBasedSubscriptionMoveRequest;
import com.trilogy.app.crm.move.request.SubscriptionMoveRequest;
import com.trilogy.app.crm.move.request.SubscriptionMoveRequestXInfo;
import com.trilogy.app.crm.move.support.SubscriptionMoveValidationSupport;
import com.trilogy.app.crm.support.AdjustmentTypeSupportHelper;
import com.trilogy.app.crm.support.LicensingSupportHelper;
import com.trilogy.app.crm.support.TransactionSupport;


/**
 * Responsible for releasing deposit from old subscription and applying
 * it to the new subscription properly for the given move request.
 * 
 * It performs validation required to complete its task successfully.
 *
 * @author Aaron Gourley
 * @since 8.1
 */
public class DepositCopyMoveStrategy<SMR extends SubscriptionMoveRequest> extends CopyMoveStrategyProxy<SMR>
{
	public static final String CONVERSION_REQUEST = "DepositCopyMoveStrategy.CONVERSION_REQUEST";
    public DepositCopyMoveStrategy(CopyMoveStrategy<SMR> delegate)
    {
        this(delegate, false);
    }
    
    public DepositCopyMoveStrategy(CopyMoveStrategy<SMR> delegate, boolean isAccountMove)
    {
    	super(delegate);
    	isAccountMove_ = isAccountMove;
    }

    /**
     * @{inheritDoc}
     */
    @Override
    public void initialize(Context ctx, SMR request)
    {
        super.initialize(ctx, request);
        
        
        AdjustmentTypeEnum depositMadeType = AdjustmentTypeEnum.DepositMade;
        try
        {
        	boolean isPrePaymentRequired = true; 
        	if(isAccountMove_ 
        			&& !ctx.getBoolean(CONVERSION_REQUEST, false))
        	{
        		try
        		{
        			isPrePaymentRequired = request.getNewSubscription(ctx).getAccount(ctx).getAccountCategory(ctx).getPrepaymentDepositRequired();
        		}catch(Exception t)
        		{
        			LogSupport.minor(ctx, this, "Exception occured while trying to get Account for Subscription : " + request.getNewSubscription(ctx).getId() 
        					+ ". This can be ignored." + t.getLocalizedMessage());
        		}
        	}
            if (isDepositChargeLicenseEnabled(ctx) && !isPrePaymentRequired)
            {
            	depositMadeType = AdjustmentTypeEnum.DepositCharge;
            }
            depositMadeAdj_ = AdjustmentTypeSupportHelper.get(ctx).getAdjustmentType(ctx, depositMadeType);
            
        }
        catch (HomeException e)
        {
            new MinorLogMsg(this, "Error retrieving " + depositMadeType + " adjustment type ' (" + depositMadeType.getIndex() + ").", e).log(ctx);
        }

        AdjustmentTypeEnum depositReleaseType = AdjustmentTypeEnum.DepositRelease;
        try
        {
            depositReleaseAdj_ = AdjustmentTypeSupportHelper.get(ctx).getAdjustmentType(ctx, depositReleaseType);
        }
        catch (HomeException e)
        {
            new MinorLogMsg(this, "Error retrieving " + depositReleaseType + " adjustment type ' (" + depositReleaseType.getIndex() + ").", e).log(ctx);
        }
    }

    /**
     * @{inheritDoc}
     */
    @Override
    public void validate(Context ctx, SMR request) throws IllegalStateException
    {
        CompoundIllegalStateException cise = new CompoundIllegalStateException();

        Subscriber oldSubscription = SubscriptionMoveValidationSupport.validateOldSubscriptionExists(ctx, request, cise);
        Subscriber newSubscription = SubscriptionMoveValidationSupport.validateNewSubscriptionExists(ctx, request, cise);
        if (oldSubscription != null && newSubscription != null
                && oldSubscription.isPrepaid()
                && newSubscription.isPrepaid())
        {
            cise.thrown(new IllegalPropertyArgumentException(
                    SubscriptionMoveRequestXInfo.OLD_SUBSCRIPTION_ID,
                    "Cannot apply deposit to prepaid subscription (ID=" + request.getOldSubscriptionId() + "."));
        }

        if (depositMadeAdj_ == null)
        {
            cise.thrown(new IllegalStateException("No adjustment type found with type '" + AdjustmentTypeEnum.DepositMade + "' (" + AdjustmentTypeEnum.DepositMade_INDEX + ")"));
        }

        if (depositReleaseAdj_ == null)
        {
            cise.thrown(new IllegalStateException("No adjustment type found with type '" + AdjustmentTypeEnum.DepositRelease + "' (" + AdjustmentTypeEnum.DepositRelease_INDEX + ")"));
        }

        cise.throwAll();

        // Don't bother letting the delegate validate unless this processor has passed.
        super.validate(ctx, request);
    }

    /**
     * @{inheritDoc}
     */
    @Override
    public void createNewEntity(Context ctx, SMR request) throws MoveException
    {
    	Subscriber newSubscription = request.getNewSubscription(ctx);
    	
    	try
    	{
    		boolean isPrePaymentRequired = true;
    		if(isAccountMove_ 
    				
    				&& !ctx.getBoolean(CONVERSION_REQUEST, false))
    		{
    			try
    			{
    				
    				isPrePaymentRequired = request.getNewSubscription(ctx).getAccount(ctx).getAccountCategory(ctx).getPrepaymentDepositRequired();
    			}catch(Exception t)
    			{
        			LogSupport.minor(ctx, this, "Exception occured while trying to get Account for Subscription : " + request.getNewSubscription(ctx).getId() 
        					+ ". This can be ignored." + t.getLocalizedMessage());
    			}
    		}
        	
    		if (isDepositChargeLicenseEnabled(ctx) && !isPrePaymentRequired)
    		{
    			newSubscription = request.getNewSubscription(ctx);
    			if (!newSubscription.isPrepaid())
    			{
    				long newDeposit;

    				Subscriber oldSubscription = request.getOriginalSubscription(ctx);
    				final long oldDeposit = oldSubscription.getDeposit(ctx);

    				if (request instanceof ServiceBasedSubscriptionMoveRequest)
    				{
    					newDeposit = ((ServiceBasedSubscriptionMoveRequest)request).getNewDepositAmount();
    					if (request instanceof ConvertSubscriptionBillingTypeRequest
    							&& newDeposit <= 0)
    					{
    						newDeposit = newSubscription.getDeposit(ctx);
    					}
    				}
    				else
    				{
    					newDeposit = oldDeposit;
    				}

    				if (oldDeposit != 0 || newDeposit != 0)
    				{
    					// Create deposit made adjustment
    					long deposit;
    					if (AdjustmentTypeActionEnum.EITHER.equals(depositMadeAdj_.getAction()))
    					{
    						deposit = -1 * newDeposit;
    					}
    					else
    					{
    						deposit = newDeposit;
    					}
    					newSubscription.setDeposit(deposit);
    				}
    			}
    		}
    	}
    	catch(Exception he)
    	{
    		throw new MoveException(request, 
                    "Error occurred applying new deposit to subscription (ID=" + newSubscription.getId() + "). Unable to retrieve Account category", he);
    	}
    	
        super.createNewEntity(ctx, request);

        newSubscription = request.getNewSubscription(ctx);
        if (!newSubscription.isPrepaid())
        {
            long newDeposit;
            
            Subscriber oldSubscription = request.getOriginalSubscription(ctx);
            final long oldDeposit = oldSubscription.getDeposit(ctx);

            if (request instanceof ServiceBasedSubscriptionMoveRequest)
            {
                newDeposit = ((ServiceBasedSubscriptionMoveRequest)request).getNewDepositAmount();
                if (request instanceof ConvertSubscriptionBillingTypeRequest
                        && newDeposit <= 0)
                {
                    newDeposit = newSubscription.getDeposit(ctx);
                }
            }
            else
            {
                newDeposit = oldDeposit;
            }

            if (oldDeposit != 0 || newDeposit != 0)
            {
                // Create deposit made adjustment
                long deposit;
                if (AdjustmentTypeActionEnum.EITHER.equals(depositMadeAdj_.getAction()))
                {
                    deposit = -1 * newDeposit;
                }
                else
                {
                    deposit = newDeposit;
                }
                
                /*
                 * Need to temporarily reset the update reason so that charges wouldn't be
                 * applied at this stage in case there is a price plan change which is updated
                 * further down. NOTE: This create transaction operation can trigger a
                 * subscriber store.
                 */
                UpdateReasonEnum originalUpdateReason = newSubscription.getUpdateReason();
                newSubscription.setUpdateReason(UpdateReasonEnum.NORMAL);
                try
                {
                    Context sCtx = ctx.createSubContext();

                    /*
                     * Put the delta of the new and old deposit into the context so the deposit
                     * made transaction will adjust the credit limit accordingly
                     */
                    sCtx.put(Common.SUBSCRIBER_MOVE_DEPOSIT_DELTA, Long.valueOf(newDeposit - oldDeposit));

                    // Make sure the right subscriber is in the context for the transaction home
                    sCtx.put(Subscriber.class, newSubscription);
                    sCtx.put(SubscriberHome.class, ctx.get(MoveConstants.CUSTOM_SUBSCRIPTION_HOME_CTX_KEY));
                    
                    new DebugLogMsg(this, "Creating deposit for subscription " + newSubscription.getId() + " for amount " + deposit + " old deposit " + oldDeposit, null).log(sCtx);
                    
                    TransactionSupport.createTransaction(sCtx, newSubscription, deposit, depositMadeAdj_);
                    
                    new InfoLogMsg(this, "Deposit created successfully for subscription " + newSubscription.getId() + " for amount " + deposit + " old deposit " + oldDeposit + " credit limit " + newSubscription.getCreditLimit(), null).log(sCtx);
                    
                }
                catch (HomeException e)
                {
                    throw new MoveException(request, 
                            "Error occurred applying new deposit (amount=" + deposit + ") "
                            + "to subscription (ID=" + newSubscription.getId() + ").", e);
                }
                finally
                {
                    // reset update reason
                    newSubscription.setUpdateReason(originalUpdateReason);
                }
            }
        }
    }

    /**
     * @{inheritDoc}
     */
    @Override
    public void removeOldEntity(Context ctx, SMR request) throws MoveException
    {
        Subscriber oldSubscription = request.getOldSubscription(ctx);
        if (!oldSubscription.isPrepaid())
        {
            // Make deposit release adjustment
            final long oldDeposit = oldSubscription.getDeposit(ctx);

            long newDeposit = 0;
            if ( request instanceof ConvertSubscriptionBillingTypeRequest)
            {
                // Release the full deposit
                newDeposit = 0;
            }
            else if (request instanceof ServiceBasedSubscriptionMoveRequest)
            {
                newDeposit = ((ServiceBasedSubscriptionMoveRequest)request).getNewDepositAmount();
            }
            else
            {
                // Release the full deposit
                newDeposit = oldDeposit;
            }
            
            if (oldDeposit > 0)
            {
                try
                {
                    final Context sCtx = ctx.createSubContext();
                    
                    sCtx.put(SubscriberCreditLimitUpdateAgent.ENABLE_PROCESSING, false);

                    long depositDelta = 0 ;
                    
                    boolean isPrePaymentRequired = true;
                    if(isAccountMove_ 
                    		
                    		&& !ctx.getBoolean(CONVERSION_REQUEST, false))
                    {
                    	
                    	
                    	try
                    	{
                    		isPrePaymentRequired = request.getOldSubscription(ctx).getAccount(ctx).getAccountCategory(ctx).getPrepaymentDepositRequired();
                    	}catch(Exception t)
                    	{
                			LogSupport.minor(ctx, this, "Exception occured while trying to get Account for Subscription : " + request.getOldSubscription(ctx).getId() 
                					+ ". This can be ignored." + t.getLocalizedMessage());
                    	}
                    }
                    
                    //TT#13040908014 : In case of deposit charge scenario, release the entire deposit.
                    if (isDepositChargeLicenseEnabled(ctx) && !isPrePaymentRequired)
            		{
                    	depositDelta = oldDeposit;
            		}
                    else
                    {
                    	depositDelta = oldDeposit - newDeposit;
                    
                    }
                    /*
                     * Put the delta into the context so the correct amount can be converted into payment.
                     * If old deposit > new deposit, the difference should be converted into payment.
                     */
                    sCtx.put(Common.SUBSCRIBER_MOVE_DEPOSIT_DELTA, Long.valueOf(depositDelta));

                    //Filtered pipeline
                    sCtx.put(SubscriberHome.class, ctx.get(MoveConstants.CUSTOM_SUBSCRIPTION_HOME_CTX_KEY));
                    
                    // Make sure the right subscriber is in the context for the transaction home
                    sCtx.put(Subscriber.class, oldSubscription);
                    
                    // Deposit release must have transaction time = move start time because by now the MSISDN
                    // is associated with the new subscription.
                    Date moveStartTime = (Date) sCtx.get(MoveConstants.MOVE_START_TIME_CTX_KEY, new Date());
                    
                    new DebugLogMsg(this, "Releasing deposit for subscription " + oldSubscription.getId() + " for amount " + oldDeposit, null).log(sCtx);
                    TransactionSupport.createTransaction(
                            sCtx, 
                            oldSubscription, oldDeposit, 
                            depositReleaseAdj_,
                            moveStartTime,
                            "Deposit release during move");
                    new InfoLogMsg(this, "Deposit released successfully for subscription " + oldSubscription.getId() + " for amount " + oldDeposit, null).log(sCtx);
                }
                catch (HomeException e)
                {
                    throw new MoveException(request, 
                            "Error occurred releasing deposit (amount=" + oldDeposit + ") "
                            + "from subscription (ID=" + oldSubscription.getId() + ").", e);
                }
            }
        } 
    
        super.removeOldEntity(ctx, request);
    }
    
    private boolean isDepositChargeLicenseEnabled(final Context ctx)
    {
        return LicensingSupportHelper.get(ctx).isLicensed(ctx, LicenseConstants.DEPOSIT_CHARGE_ADJUSTMENT);
    }
    
    private AdjustmentType depositMadeAdj_;
    private AdjustmentType depositReleaseAdj_;
    private boolean isAccountMove_ = false;
}
