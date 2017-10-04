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

import java.util.Date;

import com.trilogy.framework.xhome.beans.SafetyUtil;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.framework.xlog.log.PMLogMsg;

import com.trilogy.app.crm.bean.AdjustmentTypeEnum;
import com.trilogy.app.crm.bean.ChargedItemTypeEnum;
import com.trilogy.app.crm.bean.ChargingCycleEnum;
import com.trilogy.app.crm.bean.ServicePackage;
import com.trilogy.app.crm.bean.ServicePackageFee;
import com.trilogy.app.crm.bean.ServicePeriodEnum;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberStateEnum;
import com.trilogy.app.crm.bean.SubscriberTypeEnum;
import com.trilogy.app.crm.bean.core.Transaction;
import com.trilogy.app.crm.bean.service.HistoryEventTypeEnum;
import com.trilogy.app.crm.service.ServicePeriodHandler;
import com.trilogy.app.crm.subscriber.subscription.history.SubscriberSubscriptionHistorySupport;
import com.trilogy.app.crm.support.CoreTransactionSupportHelper;
import com.trilogy.app.crm.support.RecurringRechargeSupport;
import com.trilogy.app.crm.support.ServicePackageSupportHelper;
import com.trilogy.app.crm.support.ServicePeriodSupportHelper;
import com.trilogy.app.crm.support.SubscriberAuxiliaryServiceSupport;
import com.trilogy.app.crm.support.SuspendedEntitySupport;
import com.trilogy.app.crm.xhome.home.OcgTransactionException;
import com.trilogy.util.snippet.log.Logger;


/**
 * Generate recurring recharge for service packages of a subscriber.
 *
 * @author larry.xia@redknee.com
 */
public class RechargeSubscriberServicePackageVisitor extends AbstractRechargeItemVisitor
{

    /**
     * Serial version UID.
     */
    private static final long serialVersionUID = 1L;


    /**
     * Create a new instance of <code>RechargeSubscriberServicePackageVisitor</code>.
     *
     * @param billingDate
     *            Billing date.
     * @param agentName
     *            Name of the agent invoking this recurring charge.
     * @param servicePeriod
     *            Service period to charge.
     * @param subscriber
     *            The subscriber to be charged.
     * @param suspendOnFailure
     *            Whether to suspend on failure to charge.
     */
    public RechargeSubscriberServicePackageVisitor(final Date billingDate, final String agentName,
        final ChargingCycleEnum chargingPeriod, final Subscriber subscriber, final boolean suspendOnFailure,
        final Date startDate, final Date endDate, final double rate, final boolean preBilling, final boolean proRated, final boolean preWarnNotificationOnly)
    {
        super(billingDate, agentName, chargingPeriod, startDate, endDate, rate, preBilling, proRated, preWarnNotificationOnly);
        this.setSub(subscriber);
        this.setSuspendOnFailure(suspendOnFailure);
    }

    public RechargeSubscriberServicePackageVisitor(final Context context, final Date billingDate, final String agentName,
            final ChargingCycleEnum chargingPeriod, final Subscriber subscriber, final boolean suspendOnFailure,
            final double rate, final boolean preBilling, final boolean proRated, final boolean preWarnNotificationOnly) throws HomeException
        {
            super(context, billingDate, agentName, chargingPeriod, subscriber.getAccount(context).getBillCycleDay(context), 
                    subscriber.getSpid(), rate, preBilling, proRated, preWarnNotificationOnly);
            this.setSub(subscriber);
            this.setSuspendOnFailure(suspendOnFailure);
        }

    /**
     * {@inheritDoc}
     */
    public void visit(final Context ctx, final Object obj)
    {
        final ServicePackageFee fee = (ServicePackageFee) obj;
        final StringBuilder sb = new StringBuilder();
        sb.append("Visitor = ");
        sb.append(getClass().getSimpleName());
        sb.append(", RecurringPeriod = ");
        sb.append(getChargingCycle());
        sb.append(", Subscriber = ");
        sb.append(getSub().getId());
        sb.append(", Service package = ");
        sb.append(fee.getPackageId());
        final PMLogMsg pm = new PMLogMsg(RecurringRechargeSupport.getRecurringRechargePMModule(isPreWarnNotificationOnly()), "Package item", sb.toString());

        try
        {

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
                        Logger.minor(ctx, this, "Fail to retrieve package fee related package (id=" + 
                                fee.getPackageId() + 
                                ") for subscriber '" + getSub().getId() + "'. Not sending pre-warn sms notification for this item: " + t.getMessage(), t);
                    }
                }
                else
                {
                    handleServiceTransaction(ctx, fee);
                }
            }
            else
            {
                if (LogSupport.isDebugEnabled(ctx))
                {
                    LogSupport.debug(ctx, this, "Service Package :" + fee.getPackageId()
                        + " is not chargable to subscriber : " + this.getSub().getId());
                }

            }
        }
        finally
        {
            pm.log(ctx);
        }
    }

    private boolean dropZeroChargeAmount(Context ctx, ServicePackageFee fee)
    {
        return fee.getFee()==0 && !isSubscriberSuspending() && getSub().isPrepaid() && (isDropZeroAmountTransactions(ctx) || isPreWarnNotificationOnly());
    }

    /**
     * Returns if the service package can be charged. The service package should not be
     * charged in the following cases: 1. charged in service package, 2. suspended 3.
     * charged in same billing cycle
     *
     * @param context
     *            The operating context.
     * @param fee
     *            Service package.
     * @return Whether the service package should be charged.
     */
    public boolean isChargeable(final Context parentCtx, final ServicePackageFee fee)
    {
        Context context = parentCtx.createSubContext();
        context.put(Subscriber.class, getSub());

        boolean ret = true;
        try
        {
            final StringBuilder sb = new StringBuilder();
            sb.append("Not charging subscriber ");
            sb.append(getSub().getId());
            sb.append("for package ");
            sb.append(fee.getPackageId());
            sb.append(" version ");
            sb.append(fee.getPackageVersionId());
            sb.append(" because ");

            // Packages do not support chargeable while suspended flag.
            if (!isItemChargeable(context, false))
            {
                sb.append("the service package is not chargeable while subscription is suspended/in arrears.");
                ret = false;
            }
            else if (dropZeroChargeAmount(context, fee))
            {
                sb.append("the service package has 0 charge amount.");
                ret = false;
            }
            else
            {
                final ServicePackage pack = getPack(context, fee.getPackageId());
                if (!isMatchingServicePeriod(fee))
                {
                    sb.append("the charging period of this service package is not " + getChargingCycle());
                    ret = false;
                }
                else if (isPackageSuspended(context, pack))
                {
                    sb.append("it is suspended.");
                    ret = false;
                }
                else if (isSubscriberSuspending())
                {
                    sb.append("it is being suspended.");
                    this.suspendServicePackage(context, fee);
                    if(generateRechargeFailureErOnOCGBypass(context))
                    {
                    	createER(context, null, String.valueOf(fee.getPackageId()), RECHARGE_FAIL_ABM_LOWBALANCE, 
                    		com.redknee.product.s2100.ErrorCode.NOT_ENOUGH_BAL);
                    }
                    ret = false;
                }
                else if (!isChargeable(context, fee,
                        ChargedItemTypeEnum.SERVICEPACKAGE, pack.getAdjustmentCode(), fee.getFee(), fee.getServicePeriod()))
                {
                    sb.append("it is not chargeable (it was charged for the current billing cycle or a future date).");
                    ret = false;
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

            }

            // log the error message
            if (!ret && LogSupport.isDebugEnabled(context))
            {
                LogSupport.debug(context, this, sb.toString());
            }
        }
        catch (final Throwable t)
        {
            LogSupport.minor(context, this, "fail to decide if the service pacakge " + fee.getPackageId()
                + " for subscriber " + getSub().getId() + " is suspended or charged ", t);
            /*
             * we charge the service if we can not decide if the service is suspended or
             * not
             */

        }

        return ret;

    }


    /**
     * Determines whether the charging period of service package matches the service
     * period being charged.
     *
     * @param fee
     *            The service package fee.
     * @return Returns whether the charging period of the service package matches the
     *         service period being charged.
     */
    public boolean isMatchingServicePeriod(final ServicePackageFee fee)
    {
        final boolean result = SafetyUtil.safeEquals(fee.getServicePeriod().getChargingCycle(), getChargingCycle());
        return result;
    }


    /**
     * Returns whether the service package has been suspended.
     *
     * @param context
     *            The operating context.
     * @param pack
     *            Service package to be charged.
     * @return Whether the service package has been suspended.
     * @throws HomeException
     *             Thrown if there are problems determining whether the service package
     *             has been suspended.
     */
    private boolean isPackageSuspended(final Context context, final ServicePackage pack) throws HomeException
    {
        return SuspendedEntitySupport.isSuspendedEntity(context, getSub().getId(), pack.getIdentifier(),SubscriberAuxiliaryServiceSupport.SECONDARY_ID_NOT_USED, 
            ServicePackage.class);
    }


    /**
     * Creates a transaction for service package.
     *
     * @param ctx
     *            The operating context.
     * @param fee
     *            Service package.
     * @return Whether the charge succeeded or failed.
     */
    public boolean handleServiceTransaction(final Context context, final ServicePackageFee fee)
    {
        Transaction trans = null;
        int result = RECHARGE_ZERO_CHARGE;
        int ocgResult = OCG_RESULT_SUCCESS;
        Context ctx = context.createSubContext();
        ctx.put(RecurringRechargeSupport.RECURRING_RECHARGE_CHARGED_ITEM, fee);

        try
        {
            final ServicePackage pack = getPack(ctx, fee.getPackageId());
            try
            {
                final PMLogMsg pm1 = new PMLogMsg(RecurringRechargeSupport.RECURRING_RECHARGE_PM_MODULE, "Service package item - Create transaction", "");
                trans = this.createTransaction(ctx, pack.getAdjustmentCode(), fee.getFee());
                
                if (fee.getServicePeriod().equals(ServicePeriodEnum.ONE_TIME) ) 
                {
                    // changed for Dory, but supposed to be accepted by all customers. 
                   trans.setServiceRevenueRecognizedDate(trans.getTransDate()); 
                } else
                {
                    trans.setServiceRevenueRecognizedDate(trans.getServiceEndDate());    
                }
                
                Transaction resultTrans = CoreTransactionSupportHelper.get(ctx).createTransaction(ctx, trans, true);
                pm1.log(ctx);
                
                final PMLogMsg pm2 = new PMLogMsg(RecurringRechargeSupport.RECURRING_RECHARGE_PM_MODULE, "Service package item - Create history", "");
                SubscriberSubscriptionHistorySupport.addChargingHistory(ctx, fee, getSub(), 
                        HistoryEventTypeEnum.CHARGE, ChargedItemTypeEnum.SERVICEPACKAGE, resultTrans.getAmount(), 
                        fee.getFee(), resultTrans, getBillingDate());
                this.addTransactionId(resultTrans.getReceiptNum());
                pm2.log(ctx);
                
                result = RECHARGE_SUCCESS;
            }
            catch (final OcgTransactionException e)
            {
                ocgResult = e.getErrorCode();
                result = handleOCGError(ctx, ocgResult, fee);
                LogSupport.major(ctx, this, "Couldn't charge subscriber " + getSub().getId() + " for service package "
                    + fee.getPackageId(), e);
                handleFailure(ctx, trans, result, ocgResult, "OCG Failure", fee.getPackageId(),
                    ChargedItemTypeEnum.SERVICEPACKAGE);
            }
            
        }
        catch (final HomeException e)
        {
            LogSupport.major(ctx, this, "Couldn't charge subscriber " + getSub().getId() + " for service package "
                + fee.getPackageId(), e);
            result = RECHARGE_FAIL_XHOME;
            handleFailure(ctx, trans, result, ocgResult, "Xhome Exception:" + e.getMessage(), fee.getPackageId(),
                ChargedItemTypeEnum.SERVICEPACKAGE);
        }
        catch (final Throwable t)
        {
            LogSupport.major(ctx, this, "Couldn't charge subscriber " + getSub().getId() + " for service package"
                + fee.getPackageId(), t);
            result = RECHARGE_FAIL_UNKNOWN;
            handleFailure(ctx, trans, result, ocgResult, "Failure Unknown:" + t.getMessage(), fee.getPackageId(),
                ChargedItemTypeEnum.SERVICEPACKAGE);
        }
        finally
        {
            createER(ctx, trans, String.valueOf(fee.getPackageId()), result, ocgResult);
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
        return result == RECHARGE_SUCCESS;
    }


    /**
     * Handles OCG error, suspend service package and mapping the result code.
     *
     * @param ctx
     *            The operating context.
     * @param code
     *            OCG error code.
     * @param fee
     *            Service package.
     * @return Recurring recharge result code.
     */
    public int handleOCGError(final Context ctx, final int code, final ServicePackageFee fee)
    {
        int ret = RECHARGE_FAIL_OCG_UNKNOWN;

        switch (code)
        {
            case com.redknee.product.s2100.ErrorCode.BAL_EXPIRED:
                ret = RECHARGE_FAIL_ABM_EXPIRED;
                suspendServicePackage(ctx, fee);
                break;
            case com.redknee.product.s2100.ErrorCode.BALANCE_INSUFFICIENT:
            case com.redknee.product.s2100.ErrorCode.NOT_ENOUGH_BAL:
                ret = RECHARGE_FAIL_ABM_LOWBALANCE;
                suspendServicePackage(ctx, fee);
                break;
            case com.redknee.product.s2100.ErrorCode.INVALID_ACCT_NUM:
                ret = RECHARGE_FAIL_ABM_INVALIDPROFILE;
                suspendServicePackage(ctx, fee);
                break;
            default:

        }

        return ret;
    }


    /**
     * Suspend the service package.
     *
     * @param ctx
     *            The operating context.
     * @param fee
     *            Service package.
     */
    public void suspendServicePackage(final Context ctx, final ServicePackageFee fee)
    {
        if (isSuspendOnFailure() && getSub().getSubscriberType() == SubscriberTypeEnum.PREPAID)
        {
            LogSupport.info(ctx, this, "Suspending service package " + fee.getPackageId() + " for subscriber "
                + getSub().getId());
            try
            {
                getSub().insertSuspendedPackage(ctx, package_);
                getSub().setSuspendingEntities(true);
                this.incrementPacakgeCountSuspend();
            }
            catch (final HomeException e)
            {
                LogSupport.major(ctx, this, "fail to suspend service package " + fee.getPackageId()
                    + " for subscriber " + getSub().getId(), e);
            }
        }
    }


    /**
     * Finds the service package.
     *
     * @param context
     *            The operating context.
     * @param id
     *            Service package ID.
     * @return The service package.
     * @throws HomeException
     *             Thrown if there are problems retrieving the service package.
     */
    public ServicePackage getPack(final Context context, final int id) throws HomeException
    {
        if (package_ == null || package_.getId() != id)
        {
            package_ = ServicePackageSupportHelper.get(context).getServicePackage(context, id);
        }
        return package_;
    }


    /**
     * Accumulate charge and count for service package.
     *
     * @param success
     *            Whether it was a successful recharge.
     * @param amount
     *            Amount being charged (or attempted to be charged).
     */
    @Override
    protected synchronized void accumulateForOne(final boolean success, final long amount)
    {
        if (success)
        {
            this.incrementPackagesSuccessCount();
            this.incrementChargeAmountSuccess(amount);
        }
        else
        {
            this.incrementPackagesFailedCount();
            this.incrementChargeAmountFailed(amount);
        }
        this.incrementPackagesCount();
        this.incrementChargeAmount(amount);
    }

    /**
     * Service package being recharged.
     */
    private ServicePackage package_;
}
