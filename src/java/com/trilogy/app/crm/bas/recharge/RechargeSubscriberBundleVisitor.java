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

package com.trilogy.app.crm.bas.recharge;

import java.text.MessageFormat;
import java.util.Date;
import java.util.Map;

import com.trilogy.app.crm.bean.ChargedItemTypeEnum;
import com.trilogy.app.crm.bean.ChargingCycleEnum;
import com.trilogy.app.crm.bean.ChargeFailureActionEnum;
import com.trilogy.app.crm.bean.ServicePeriodEnum;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberHome;
import com.trilogy.app.crm.bean.SubscriberTypeEnum;
import com.trilogy.app.crm.bean.core.BundleFee;
import com.trilogy.app.crm.bean.core.BundleProfile;
import com.trilogy.app.crm.bean.core.Transaction;
import com.trilogy.app.crm.bean.service.HistoryEventTypeEnum;
import com.trilogy.app.crm.bundle.InvalidBundleApiException;
import com.trilogy.app.crm.bundle.SubscriberBundleSupport;
import com.trilogy.app.crm.bundle.exception.BundleDoesNotExistsException;
import com.trilogy.app.crm.service.MultiDayPeriodHandler;
import com.trilogy.app.crm.service.ServicePeriodHandler;
import com.trilogy.app.crm.subscriber.charge.ChargingConstants;
import com.trilogy.app.crm.subscriber.subscription.history.SubscriberSubscriptionHistorySupport;
import com.trilogy.app.crm.support.BundleSupportHelper;
import com.trilogy.app.crm.support.CoreTransactionSupportHelper;
import com.trilogy.app.crm.support.RecurringRechargeSupport;
import com.trilogy.app.crm.support.ServicePeriodSupportHelper;
import com.trilogy.app.crm.support.SubscriberAuxiliaryServiceSupport;
import com.trilogy.app.crm.support.SubscriptionNotificationSupport;
import com.trilogy.app.crm.support.SuspendedEntitySupport;
import com.trilogy.app.crm.xhome.home.OcgTransactionException;
import com.trilogy.framework.xhome.beans.SafetyUtil;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.framework.xlog.log.PMLogMsg;
import com.trilogy.util.snippet.log.Logger;

/**
 * Generates bundle recurring recharges for a subscriber.
 *
 * @author larry.xia@redknee.com
 */
public class RechargeSubscriberBundleVisitor extends AbstractRechargeItemVisitor
{

    /**
     * Serial version UID.
     */
    private static final long serialVersionUID = 1L;

    /**
     * Create a new instance of <code>RechargeSubscriberBundleVisitor</code>.
     *
     * @param billingDate      Billing date.
     * @param agentName        Name of the agent invoking this recurring charge.
     * @param servicePeriod    Service period to charge.
     * @param subscriber       The subscriber being visited.
     * @param suspendOnFailure Whether to suspend on failure.
     */
    public RechargeSubscriberBundleVisitor(final Date billingDate, final String agentName,
            final ChargingCycleEnum chargingPeriod, final Subscriber subscriber, final boolean suspendOnFailure,
            final Date startDate, final Date endDate, final double rate, final boolean preBilling, final boolean proRated, 
            final boolean preWarnNotificationOnly, final boolean allOrNothingChargingFirstPass)
    {
        super(billingDate, agentName, chargingPeriod, startDate, endDate, rate, preBilling, proRated, 
        		preWarnNotificationOnly);
        this.setSub(subscriber);
        this.setSuspendOnFailure(suspendOnFailure);
        this.setAllOrNothingChargingFirstPass(allOrNothingChargingFirstPass);
    }

    public RechargeSubscriberBundleVisitor(final Context context, final Date billingDate, final String agentName,
            final ChargingCycleEnum chargingPeriod, final Subscriber subscriber, final boolean suspendOnFailure,
            final double rate, final boolean preBilling, final boolean proRated, final boolean preWarnNotificationOnly, 
            final boolean allOrNothingCharging) throws HomeException
    {
        super(context, billingDate, agentName, chargingPeriod, subscriber.getAccount(context).getBillCycleDay(context),
                subscriber.getSpid(), rate, preBilling, proRated, preWarnNotificationOnly);
        this.setSub(subscriber);
        this.setSuspendOnFailure(suspendOnFailure);
    }

    /**
     * {@inheritDoc}
     */
    public void visit(final Context ctx, final Object obj) throws AgentException
    {
        final BundleFee fee = (BundleFee) obj;
        final StringBuilder sb = new StringBuilder();
        sb.append("Visitor = ");
        sb.append(getClass().getSimpleName());
        sb.append(", RecurringPeriod = ");
        sb.append(getChargingCycle());
        sb.append(", Subscriber = ");
        sb.append(getSub().getId());
        sb.append(", Bundle = ");
        sb.append(fee.getId());
        final PMLogMsg pm = new PMLogMsg(RecurringRechargeSupport.getRecurringRechargePMModule(isPreWarnNotificationOnly()), "Bundle item", sb.toString());
        try
        {
        	if(getChargingCycle().equals(ChargingCycleEnum.MULTIDAY))
        	{
        		/*
        		 *  modify end date for multi day service , since for multi-day services , enddate is dynamic 
        		 *  i.e. two services starting at the same day can have different end date
        		 *  
        		 *  same goes with start date
        		 */
            	
        		ServicePeriodHandler handler = ServicePeriodSupportHelper.get(ctx).getHandler(ServicePeriodEnum.MULTIDAY);
            	
        		
        		Date startDate = handler.calculateCycleStartDate(ctx, getBillingDate(), -1, -1, getSub().getId(), fee);
                super.setStartDate(startDate);
            	
                /*
                 * For performance optimization, dont want calculateCycleEndDate to invoke calculateCycleStartDate again which it does internally 
                 * 	if CALCULATE_END_DATE_FROM_CYCLE_START is not set to false 
                 */
                Context subContext = ctx.createSubContext();
                subContext.put(MultiDayPeriodHandler.CALCULATE_END_DATE_FROM_CYCLE_START, false);
            	
            	Date endDate = handler.calculateCycleEndDate(subContext,  startDate, -1, -1, null,fee); 
    			super.setEndDate(endDate);
                super.setOrigServiceEndDate(endDate);
        	}
        	
            this.setChargeable(this.isChargeable(ctx, fee));

            if (this.isChargeable())
            {
                if (this.isPreWarnNotificationOnly())
                {
                    try
                    {
                        handlePreWarnNotification(ctx, fee, fee.getFee());
                    }
                    catch (Throwable t)
                    {
                        Logger.minor(ctx, this, "Fail to retrieve bundle fee related bundle profile (id=" + 
                                fee.getId() + 
                                ") for subscriber '" + getSub().getId() + "'. Not sending pre-warn sms notification for this item: " + t.getMessage(), t);
                    }
                }
                else
                {
                    handleBundleTransaction(ctx, fee);
                }
            }
            else
            {
                if (LogSupport.isDebugEnabled(ctx))
                {
                    LogSupport.debug(ctx, this, "Bundle :" + fee.getId() + " is not chargable to subscriber : "
                            + this.getSub().getId());
                }
            }
        } catch (Exception e) {
        	throw new AgentException(e);
        }
        finally
        {
            pm.log(ctx);
        }
    }

    private boolean dropZeroChargeAmount(Context ctx, BundleFee fee)
    {
        return fee.getFee()==0 && !isSubscriberSuspending() && getSub().isPrepaid() && (isDropZeroAmountTransactions(ctx) || isPreWarnNotificationOnly());
    }

    /**
     * Returns if the bundle can be charged. The bundle should be not charged in the
     * following cases: 1. charged in service package, 2. suspended 3. charged in same
     * billing cycle.
     *
     * @param context The operating context.
     * @param fee     Bundle to be charged.
     * @return Whether the bundle should be charged.
     */
    public boolean isChargeable(final Context parentCtx, final BundleFee fee)
    {
        Context context = parentCtx.createSubContext();
        context.put(Subscriber.class, getSub());

        boolean ret = true;
        boolean isSuspending = false;
        try
        {
            final StringBuilder sb = new StringBuilder();
            sb.append("Not charging subscriber ");
            sb.append(getSub().getId());
            sb.append("for bundle ");
            sb.append(fee.getId());
            sb.append(" because ");

            if (!isItemChargeable(context, fee.getBundleProfile(context, getSub().getSpid()).isChargeableWhileSuspended()))
            {
                sb.append("the bundle is not chargeable while subscription is suspended/in arrears.");
                ret = false;
            }
            else if (dropZeroChargeAmount(context, fee))
            {
                sb.append("the service has 0 charge amount.");
                ret = false;
            }
            // do not charge for services that come from packages
            else if (isBundleInPackage(fee))
            {
                sb.append("it is part of a service package");
                ret = false;
            }
            else if (!isMatchingServicePeriod(fee))
            {
                sb.append("the charging period of this bundle is not " + getChargingCycle());
                ret = false;
            }
            else if (isBundleSuspended(context, fee))
            {
                sb.append("it is suspended.");
                ret = false;
            }
            else if (!getChargingCycle().equals(ChargingCycleEnum.MULTIDAY) && isSubscriberSuspending())
            {
                if(fee.getChargeFailureAction().getIndex() == ChargeFailureActionEnum.REMOVE_INDEX)
                {
                    sb.append("it is being removed.");
                    this.removeBundle(context, fee);
                    ret = false;
                }
                else
                {
                    /*
                     * Two multi-day bundles can have different cycle-start-date and cycle-end-date and thus suspension of one bundle should not cause the suspension of others. 
                     * Monthly , weekly , multi-monthly bundle would have same cycle-start-date and cycle-end-date and thus suspension of one would cause suspension of rest of the same type.
                     * 
                     * TT # 12051433017
                     * 
                     *  Another issue (TT# 12061157040) got introduced as a side effect of above fix : Two multi-Day services can have same cycle-start-date( i.e. same next-recurring-chargedate).
                        and in that case all of those should be suspended 
                     *      
                     *  See below fix - "else if (getChargingCycle().equals(ChargingCycleEnum.MULTIDAY) && isSubscriberSuspending())" 
                     *  Note : i did not move isSuspending() check after isChargeable() to fix this issue but only for the special case which is multi-day. 
                     */
                    
                    sb.append("it is being suspended.");
                    this.suspendBundle(context, fee);
                    ret = false;
                }
                isSuspending = true;
            }
            else if (!isChargeable(context, fee, fee.isAuxiliarySource()
                    ? ChargedItemTypeEnum.AUXBUNDLE
                    : ChargedItemTypeEnum.BUNDLE, this.getAdjustmentType(context, fee), fee.getFee(),
                    fee.getServicePeriod()))
            {
                sb.append("it is not chargeable (has been charged for the current billing cycle or in the future).");
                ret = false;
            }
            else if (getChargingCycle().equals(ChargingCycleEnum.MULTIDAY) && isSubscriberSuspending())
            {
                if(fee.getChargeFailureAction().getIndex() == ChargeFailureActionEnum.REMOVE_INDEX)
                {
                    sb.append("it is being removed.");
                    this.removeBundle(context, fee);
                    ret = false;
                }
                else
                {
                    /*
                     * TT # 12061157040
                     * Code execution is here that means Multi-day service is chargeable and thus should be suspended because some other multi-day service has indicated that 
                     * all the services which are chargeable should be suspended. ( e.g. - is primary Multi Day service is suspended because of low balance in CPS all of the remaining 
                     * services , which are next in line for re-charging, should be suspended without attempting to re-charge it. 
                     * 
                     */
                    sb.append("it is being suspended.");
                    this.suspendBundle(context, fee);
                    ret = false;
                }
                isSuspending = true;
            }
            else if (ServicePeriodSupportHelper.get(context).usesSpecialHandler(context, fee.getServicePeriod()) && isProRated())
            {
                ServicePeriodHandler handler = ServicePeriodSupportHelper.get(context).getHandler(fee.getServicePeriod());
                double rate = handler.calculateRate(context, getBillingDate(), getBillCycle(context).getDayOfMonth(), getCRMSpid(context).getSpid(), getSub().getId(), fee);
                setItemRate(rate);
            }
            else if (isProRated())
            {
                setItemRate(getRate());
            }

            // log the error message
            if (!ret && LogSupport.isDebugEnabled(context))
            {
                LogSupport.debug(context, this, sb.toString());
            }
            if(isSuspending && generateRechargeFailureErOnOCGBypass(context))
            {
            	createER(context, this.createTransaction(context, getAdjustmentType(context, fee), fee.getFee()), 
            			String.valueOf(fee.getId()), RECHARGE_FAIL_ABM_LOWBALANCE,
            			com.redknee.product.s2100.ErrorCode.NOT_ENOUGH_BAL);
            }
        }
        catch (final Throwable t)
        {
            LogSupport.minor(context, this, "fail to decide if the bundle " + fee.getId() + " for subscriber "
                    + getSub().getId() + " is suspended or charged ", t);
            /*
             * we charge the service if we can not decide if the service is suspended or
             * not
             */

        }

        return ret;
    }

    /**
     * Determines whether the charging period of bundle matches the service period being
     * charged.
     *
     * @param fee The bundle fee.
     * @return Returns whether the charging period of the bundle matches the service
     *         period being charged.
     */
    public boolean isMatchingServicePeriod(final BundleFee fee)
    {
        final boolean result = SafetyUtil.safeEquals(fee.getServicePeriod().getChargingCycle(), getChargingCycle());
        return result;
    }

    /**
     * Returns whether the bundle has been suspended.
     *
     * @param context The operating context.
     * @param fee     Bundle to be charged.
     * @return Whether the bundle has been suspended.
     * @throws HomeException Thrown if there are problems determining whether the bundle has been
     *                       suspended.
     */
    private boolean isBundleSuspended(final Context context, final BundleFee fee) throws HomeException
    {
        return SuspendedEntitySupport.isSuspendedEntity(context, getSub().getId(), fee.getId(), SubscriberAuxiliaryServiceSupport.SECONDARY_ID_NOT_USED, BundleFee.class);
    }

    /**
     * Returns whether the bundle is part of a service package.
     *
     * @param fee Bundle to be charged.
     * @return Whether the bundle is part of a service package.
     */
    private boolean isBundleInPackage(final BundleFee fee)
    {
        return fee.getSource() != null && fee.getSource().startsWith("Package");
    }

    /**
     * Creates the transaction for bundle charge.
     *
     * @param ctx The operating context.
     * @param fee Bundle fee.
     * @return Whether the charge succeeded or failed.
     */
    public boolean handleBundleTransaction(final Context context, final BundleFee fee)
    {

        Transaction trans = null;
        int result = RECHARGE_ZERO_CHARGE;
        int ocgResult = OCG_RESULT_SUCCESS;
        final ChargedItemTypeEnum chargeItemType;
        Context ctx = context.createSubContext();
        ctx.put(RecurringRechargeSupport.RECURRING_RECHARGE_CHARGED_ITEM, fee);

        if (fee.isAuxiliarySource())
        {
            chargeItemType = ChargedItemTypeEnum.AUXBUNDLE;
        }
        else
        {
            chargeItemType = ChargedItemTypeEnum.BUNDLE;
        }

        try
        {
            long rawAmount = fee.getFee();
            Transaction resultTrans = null;
            try
            {
                BundleProfile bundle = fee.getBundleProfile(ctx, getSub().getSpid());
                if (fee.isAuxiliarySource() && rawAmount == 0)
                {
                    rawAmount = bundle.getAuxiliaryServiceCharge();
                }
                final PMLogMsg pm1 = new PMLogMsg(RecurringRechargeSupport.RECURRING_RECHARGE_PM_MODULE, "Bundle item - Create transaction", "");
                trans = this.createTransaction(ctx, getAdjustmentType(ctx, fee), rawAmount);
                
                if(LogSupport.isDebugEnabled(ctx))
                {
                    String msg = MessageFormat.format(
                        "Restrict Provisioning {0} for Bundle {1}, for subscriber {2}", 
                            new Object[]{bundle.getRestrictProvisioning()? "ON" : "OFF",
                                    Long.valueOf(fee.getId()), getSub().getId()});
                    LogSupport.debug(ctx, this, msg);
                }
                
                trans.setAllowCreditLimitOverride(fee.isMandatory() || !bundle.getRestrictProvisioning());
                
                if (fee.getServicePeriod().equals(ServicePeriodEnum.ONE_TIME) ) 
                {
                    // changed for Dory, but supposed to be accepted by all customers. 
                   trans.setServiceRevenueRecognizedDate(trans.getTransDate()); 
                } else
                {
                    trans.setServiceRevenueRecognizedDate(trans.getServiceEndDate());    
                }
                
                if(this.isAllOrNothingChargingFirstPass() && chargeItemType.equals(ChargedItemTypeEnum.BUNDLE))
                {
                	handleAllOrNothingChargeAccumulation(ctx, fee, fee.getFee());
                	result = RECHARGE_SUCCESS;
                }
                else
                {
		            resultTrans = CoreTransactionSupportHelper.get(ctx).createTransaction(ctx, trans, true);
		            pm1.log(ctx);
		
		            final PMLogMsg pm2 = new PMLogMsg(RecurringRechargeSupport.RECURRING_RECHARGE_PM_MODULE, "Service item - Create history", "");
		            SubscriberSubscriptionHistorySupport.addChargingHistory(ctx, fee, getSub(),
		                    HistoryEventTypeEnum.CHARGE, chargeItemType, trans.getAmount(),
		                    rawAmount, resultTrans, getBillingDate());
		            this.addTransactionId(Long.valueOf(resultTrans.getReceiptNum()));
		            pm2.log(ctx);
		
		            result = RECHARGE_SUCCESS;
                }
            }
            catch (final OcgTransactionException e)
            {
                ocgResult = e.getErrorCode();
                result = handleOCGError(ctx, ocgResult, fee);
                LogSupport.major(ctx, this, "Couldn't charge subscriber " + getSub().getId() + " for services "
                        + fee.getId(), e);
                handleFailure(ctx, trans, result, ocgResult, "OCG Failure", fee.getId(), chargeItemType);
            }
        }
        catch (final HomeException e)
        {
            LogSupport.major(ctx, this, "Couldn't charge subscriber " + getSub().getId() + " for services "
                    + fee.getId(), e);
            result = RECHARGE_FAIL_XHOME;
            handleFailure(ctx, trans, result, ocgResult, "Xhome Exception:" + e.getMessage(), fee.getId(),
                    chargeItemType);
        }
        catch (final Throwable t)
        {

            LogSupport.major(ctx, this, "Couldn't charge subscriber " + getSub().getId() + " for services "
                    + fee.getId(), t);
            result = RECHARGE_FAIL_UNKNOWN;
            handleFailure(ctx, trans, result, ocgResult, "Failure Unknown:" + t.getMessage(), fee.getId(),
                    chargeItemType);
        }
        finally
        {
        	if(!this.isAllOrNothingChargingFirstPass())
        	{
	            createER(ctx, trans, String.valueOf(fee.getId()), result, ocgResult);
	            createOM(ctx, getAgentName(), result == RECHARGE_SUCCESS);
	            this.accumulateForOne(result == RECHARGE_SUCCESS, fee.getFee());
	            if(result == RECHARGE_SUCCESS)
	            {
	            	handleRechargeNotification(ctx, fee, fee.getFee());
	            }
	            else
	            {
	            	handleRechargeFailureNotification(ctx, fee, fee.getFee());
	            }
        	}
        }

        return result == RECHARGE_SUCCESS;
    }

    /**
     * Mapping OCG result code to a recharge result code and suspend bundle as well.
     *
     * @param ctx  The operating context.
     * @param code OCG result code.
     * @param fee  Bundle being charged.
     * @return Recurring recharge result code.
     */
    public int handleOCGError(final Context ctx, final int code, final BundleFee fee)
    {
        int ret = RECHARGE_FAIL_OCG_UNKNOWN;

        switch (code)
        {
            case com.redknee.product.s2100.ErrorCode.BAL_EXPIRED:
                ret = RECHARGE_FAIL_ABM_EXPIRED;
                suspendBundle(ctx, fee);
                break;
            case com.redknee.product.s2100.ErrorCode.BALANCE_INSUFFICIENT:
            case com.redknee.product.s2100.ErrorCode.NOT_ENOUGH_BAL:
                ret = RECHARGE_FAIL_ABM_LOWBALANCE;
                performChargeFailureAction(ctx, fee);
                break;
            case com.redknee.product.s2100.ErrorCode.INVALID_ACCT_NUM:
                ret = RECHARGE_FAIL_ABM_INVALIDPROFILE;
                suspendBundle(ctx, fee);
                break;
            default:
        }

        return ret;
    }

    /**
     * Suspends bundle. This is only applicable to prepaid subscriber.
     *
     * @param ctx The operating context.
     * @param fee Bundle to be suspended.
     */
    public void suspendBundle(final Context ctx, final BundleFee fee)
    {
        boolean restrictProvisioningForPostpaid = false;
        try
        {
            restrictProvisioningForPostpaid = fee.getBundleProfile(ctx, getSub().getSpid()).isRestrictProvisioning()  
                && getSub().getSubscriberType() == SubscriberTypeEnum.POSTPAID;
        } 
        catch (Exception e1)
        {
            if(LogSupport.isDebugEnabled(ctx))
                LogSupport.debug(ctx, this, 
                        "Could not fetch Bundle-profile for the BundleFee: "+ 
                        fee.getId(), e1);
            restrictProvisioningForPostpaid = false;
        }
        
        
        if (
                restrictProvisioningForPostpaid || 
                (isSuspendOnFailure() && getSub().getSubscriberType() == SubscriberTypeEnum.PREPAID))
        {
            LogSupport.info(ctx, this, "Suspending bundle " + fee.getId() + " for subscriber " + getSub().getId());

            try
            {
                getSub().insertSuspendedBundles(ctx, fee);
                getSub().setSuspendingEntities(true);
                this.incrementBundleCountSuspend();
            }
            catch (final HomeException e)
            {
                LogSupport.major(ctx, this, "fail to suspend bundle " + fee.getId() + " for subscriber "
                        + getSub().getId(), e);
            }
        }
    }


    /**
     * Returns the bundle adjustment type.
     *
     * @param context The operating context.
     * @param fee     Bundle to be charged.
     * @return The adjustment type of the bundle.
     * @throws InvalidBundleApiException Thrown if the bundle profile API object cannot be found.
     * @throws HomeException             Thrown if there are problems retrieving the bundle profile API object.
     */
    private int getAdjustmentType(final Context context, final BundleFee fee) throws InvalidBundleApiException,
            HomeException
    {
        try
        {
            final BundleProfile bundle = fee.getBundleProfile(context, getSub().getSpid());
            return fee.isAuxiliarySource() ? bundle.getAuxiliaryAdjustmentType() : bundle.getAdjustmentType();
        }
        catch (BundleDoesNotExistsException e)
        {
            LogSupport.info(context, BundleSupportHelper.class.getName(), "Bundle does not exist:" + e.getMessage());
            throw new InvalidBundleApiException("Error getting bundle adjustment type. Bundle does not exist: " + e.getMessage());
        }
        catch (Exception e)
        {
            LogSupport.major(context, BundleSupportHelper.class.getName(), "Error getting bundle adjustment type: " + e.getMessage(), e);
            throw new InvalidBundleApiException("Error getting bundle adjustment type: " + e.getMessage());
        }
    }

    /**
     * Accumulate the total charges and count.
     *
     * @param success Whether the charge was a success.
     * @param amount  The amount charged (or intended to charge).
     */
    @Override
    protected synchronized void accumulateForOne(final boolean success, final long amount)
    {
        if (success)
        {
            this.incrementBundleCountSuccess();
            this.incrementChargeAmountSuccess(amount);
        }
        else
        {
            this.incrementBundleCountFailed();
            this.incrementChargeAmountFailed(amount);
        }
        this.incrementBundleCount();
        this.incrementChargeAmount(amount);
    }

    
    /**
     * Unprovision bundle.
     *
     * @param ctx The operating context.
     * @param Bundle fee to be unprovisioned.
     */
    private void removeBundle(final Context ctx, final BundleFee fee)
    {
            Context subCtx = ctx.createSubContext();
            LogSupport.info(subCtx, this, "Unprovisioning bundle " + fee.getId() + " for subscriber " + getSub().getId());
            getSub().bundleUnProvisioned(fee);
            Map<Long, BundleFee> subscribedBundles = SubscriberBundleSupport.getSubscribedBundles(subCtx, getSub());
            subscribedBundles.remove(fee.getId());
            
            getSub().setBundles(subscribedBundles);
            
            this.incrementBundleCountFailed();
            Home subHome = (Home)subCtx.get(SubscriberHome.class);
            try
            {
                subCtx.put(ChargingConstants.IS_RECURRING_RECHARGE, true);
                subHome.store(subCtx, getSub());
                getSub().setSuspendingEntities(true);
                SubscriptionNotificationSupport.sendPricePlanOptionRemovalNotification(subCtx, getSub(), null, fee);
            }
            catch (HomeException e)
            {
                LogSupport.major(subCtx, this, "fail to update subscriber while unprovisioning bundle " + fee.getId() + " for subscriber "
                        + getSub().getId(), e);
            }
            
    }

    /**
     * Takes action whether to remove or suspend the bundle.
     *
     * @param ctx
     *            The operating context.
     * @param fee
     *            Bundle being suspended/removed.
     */
    private void performChargeFailureAction(final Context ctx, final BundleFee fee)
    {
        String subscriberId = getSub().getId();
        try
        {
            long bundleId = fee.getBundleProfile(ctx).getBundleId();
            
            if(LogSupport.isDebugEnabled(ctx))
            {
                LogSupport.debug(ctx, this, "Looking up for Priceplan version level configuration for suspending/" +
                    "unprovisioning bundle " + bundleId + " for subscriber "
                    + subscriberId);
            }
            
            if(fee.getChargeFailureAction().getIndex() == ChargeFailureActionEnum.REMOVE_INDEX)
            {
                if(LogSupport.isDebugEnabled(ctx))
                {
                    LogSupport.debug(ctx, this, "Removing Bundle  " + bundleId + " for subscriber "
                            + subscriberId);
                }
                removeBundle(ctx, fee);
            }
            else if(fee.getChargeFailureAction().getIndex() == ChargeFailureActionEnum.SUSPEND_INDEX)
            {
                if(LogSupport.isDebugEnabled(ctx))
                {
                    LogSupport.debug(ctx, this, "Suspending Bundle  " + bundleId + " for subscriber "
                            + subscriberId);
                }
                suspendBundle(ctx, fee);
            }
        }
        catch (Exception e) 
        {
            LogSupport.minor(ctx, this, "Exception while fetching bundle id from bundle fee.");
        }
    }
    
    /**
	 * @return whether it's charge accumulation for first pass of all or nothing charging
	 */
	public boolean isAllOrNothingChargingFirstPass() {
		return allOrNothingChargingFirstPass_;
	}

	/**
	 * @param allOrNothingChargingFirstPass_ 
	 */
	public void setAllOrNothingChargingFirstPass(
			boolean allOrNothingChargingFirstPass_) {
		this.allOrNothingChargingFirstPass_ = allOrNothingChargingFirstPass_;
	}
    
	private boolean allOrNothingChargingFirstPass_;

    
}
