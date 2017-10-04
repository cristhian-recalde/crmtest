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

import com.trilogy.app.crm.bean.ChargingCycleEnum;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.visitor.Visitor;
import com.trilogy.framework.xlog.log.LogSupport;


/**
 * Super class of all recharge visitors.
 * 
 * @author larry.xia@redknee.com
 */
public abstract class AbstractRechargeVisitor implements Visitor, RechargeConstants, RechargeVisitorCountable
{

    /**
     * Charge success OM name.
     */
    protected static final String OM_SUCCESS = "RecurringCharge_Success";

    /**
     * Charge failure OM name.
     */
    protected static final String OM_FAILURE = "RecurringCharge_Failure";

    /**
     * Charge attempt OM name.
     */
    protected static final String OM_ATTEMPT = "RecurringCharge_Attempt";

    /**
     * PM name for number of items visited.
     */
    protected static final String PM_CHARGE_ATTEMPT = "RecurringCharge_ItemVisited";


    /**
     * Create a new instance of <code>AbstractRechargeVisitor</code>.
     * 
     * @param billingDate
     *            Billing date.
     * @param agentName
     *            Agent name.
     * @param recurringPeriod
     *            Service period to charge.
     * @param preBilling
     *            Indicates whether or not to use pre billing.
     * @param proRated
     *            Indicates whether or not to proRate recharges.
     */
    public AbstractRechargeVisitor(final Date billingDate, final String agentName, final ChargingCycleEnum chargingCycle, final boolean recurringRecharge, final boolean proRated, final boolean preWarnNotificationOnly)
    {
        this.billingDate_ = billingDate;
        this.agentName_ = agentName;
        this.chargingCycle_ = chargingCycle;
        this.recurringRecharge_ = recurringRecharge;
        this.proRated_ = proRated;
        this.preWarnNotificationOnly_ = preWarnNotificationOnly;
    }


    /**
     * Create an OM for the recurring charge.
     * 
     * @param ctx
     *            The operating context.
     * @param moduleName
     *            OM module name.
     * @param success
     *            Whether it was a successful or failed charge.
     */
    public void createOM(final Context ctx, final String moduleName, final boolean success)
    {
        final String omName;
        if (success)
        {
            omName = OM_SUCCESS;
        }
        else
        {
            omName = OM_FAILURE;
        }
        LogSupport.om(ctx, moduleName, omName);
        LogSupport.om(ctx, moduleName, OM_ATTEMPT);
    }


    /**
     * {@inheritDoc}
     */
    public int getBundleCount()
    {
        return bundleCount_;
    }


    /**
     * Increments the bundle count.
     */
    public void incrementBundleCount()
    {
        this.bundleCount_++;
    }


    /**
     * {@inheritDoc}
     */
    public int getBundleCountFailed()
    {
        return bundleCountFailed_;
    }


    /**
     * Increments bundle failed counter.
     */
    public void incrementBundleCountFailed()
    {
        this.bundleCountFailed_++;
    }


    /**
     * {@inheritDoc}
     */
    public int getBundleCountSuccess()
    {
        return bundleCountSuccess_;
    }


    /**
     * Increments bundle success counter.
     */
    public void incrementBundleCountSuccess()
    {
        this.bundleCountSuccess_++;
    }


    /**
     * {@inheritDoc}
     */
    public int getBundleCountSuspend()
    {
        return bundleCountSuspend_;
    }


    /**
     * Increments bundle suspension counter.
     */
    public void incrementBundleCountSuspend()
    {
        this.bundleCountSuspend_++;
    }


    /**
     * {@inheritDoc}
     */
    public long getChargeAmount()
    {
        return chargeAmount_;
    }


    /**
     * Increments the accumulated charge amount by the provided amount.
     * 
     * @param chargeAmount
     *            Charge amount to add to total.
     */
    public void incrementChargeAmount(final long chargeAmount)
    {
        this.chargeAmount_ += chargeAmount;
    }


    /**
     * {@inheritDoc}
     */
    public long getChargeAmountFailed()
    {
        return chargeAmountFailed_;
    }


    /**
     * Increments the failed charge amount by the provided amount.
     * 
     * @param chargeAmountFailed
     *            Failed charge amount to add to total.
     */
    public void incrementChargeAmountFailed(final long chargeAmountFailed)
    {
        this.chargeAmountFailed_ += chargeAmountFailed;
    }


    /**
     * {@inheritDoc}
     */
    public long getChargeAmountSuccess()
    {
        return chargeAmountSuccess_;
    }


    /**
     * Increments the successful charge amount by the provided amount.
     * 
     * @param chargeAmountSuccess
     *            Successful charge amount to add to total.
     */
    public void incrementChargeAmountSuccess(final long chargeAmountSuccess)
    {
        this.chargeAmountSuccess_ += chargeAmountSuccess;
    }


    /**
     * {@inheritDoc}
     */
    public int getPackageCountSuspend()
    {
        return packageCountSuspend_;
    }


    /**
     * Increments the service package suspension counter.
     */
    public void incrementPacakgeCountSuspend()
    {
        this.packageCountSuspend_++;
    }


    /**
     * {@inheritDoc}
     */
    public int getPackagesFailedCount()
    {
        return packagesFailedCount_;
    }


    /**
     * Increments the service package failure counter.
     */
    public void incrementPackagesFailedCount()
    {
        this.packagesFailedCount_++;
    }


    /**
     * {@inheritDoc}
     */
    public int getPackagesCount()
    {
        return packagesCount_;
    }


    /**
     * Increments the service package counter.
     */
    public void incrementPackagesCount()
    {
        this.packagesCount_++;
    }


    /**
     * {@inheritDoc}
     */
    public int gePackagesSuccessCount()
    {
        return packagesSuccessCount_;
    }


    /**
     * Increments the service package success counter.
     */
    public void incrementPackagesSuccessCount()
    {
        this.packagesSuccessCount_++;
    }


    /**
     * {@inheritDoc}
     */
    public int getServiceCountSuspend()
    {
        return serviceCountSuspend_;
    }


    /**
     * Increments the service suspension counter.
     */
    public void incrementServiceCountSuspend()
    {
        this.serviceCountSuspend_++;
    }


    /**
     * {@inheritDoc}
     */
    public int getServicesCount()
    {
        return servicesCount_;
    }


    /**
     * Increments the service counter.
     */
    public void incrementServicesCount()
    {
        this.servicesCount_++;
    }


    /**
     * {@inheritDoc}
     */
    public int getServicesCountFailed()
    {
        return servicesCountFailed_;
    }


    /**
     * Increments the failed service counter.
     */
    public void incrementServicesCountFailed()
    {
        this.servicesCountFailed_++;
    }


    /**
     * {@inheritDoc}
     */
    public int getServicesCountSuccess()
    {
        return servicesCountSuccess_;
    }


    /**
     * Increments the successful service counter.
     */
    public void incrementServicesCountSuccess()
    {
        this.servicesCountSuccess_++;
    }


    /**
     * Whether to suspend on failure.
     * 
     * @return Whether to suspend on failure.
     */
    public boolean isSuspendOnFailure()
    {
        return suspendOnFailure_;
    }


    /**
     * Sets whether to suspend on failure.
     * 
     * @param suspendOnFailure
     *            Whether to suspend on failure.
     */
    public void setSuspendOnFailure(final boolean suspendOnFailure)
    {
        this.suspendOnFailure_ = suspendOnFailure;
    }


    /**
     * Returns the service period.
     * 
     * @return Service period.
     */
    public ChargingCycleEnum getChargingCycle()
    {
        return chargingCycle_;
    }


    /**
     * Returns the billing date.
     * 
     * @return Billing date.
     */
    public Date getBillingDate()
    {
        return billingDate_;
    }


    /**
     * {@inheritDoc}
     */
    public int getAccountCount()
    {
        return accountCount_;
    }


    /**
     * Increments the account counter.
     */
    public void incrementAccountCount()
    {
        this.accountCount_++;
    }


    /**
     * {@inheritDoc}
     */
    public int getAccountFailCount()
    {
        return accountFailCount_;
    }


    /**
     * Increments the account failure counter.
     */
    public void incrementAccountFailCount()
    {
        this.accountFailCount_++;
    }


    /**
     * {@inheritDoc}
     */
    public int getAccountSuccessCount()
    {
        return accountSuccessCount_;
    }


    /**
     * Increments the account success counter.
     */
    public void incrementAccountSuccessCount()
    {
        this.accountSuccessCount_++;
    }


    /**
     * {@inheritDoc}
     */
    public int getSubscriberCount()
    {
        return subscriberCount_;
    }


    /**
     * Increments the subscriber count.
     */
    public void incrementSubscriberCount()
    {
        this.subscriberCount_++;
    }


    /**
     * {@inheritDoc}
     */
    public int getSubscriberFailCount()
    {
        return subscriberFailCount_;
    }


    /**
     * Increments subscriber failure count.
     */
    public void incrementSubscriberFailCount()
    {
        this.subscriberFailCount_++;
    }


    /**
     * {@inheritDoc}
     */
    public int getSubscriberSuccessCount()
    {
        return subscriberSuccessCount_;
    }


    /**
     * Increments subscriber success count.
     */
    public void incrementSubscriberSuccessCount()
    {
        this.subscriberSuccessCount_++;
    }


    /**
     * Returns the name of agent running this recurring charge.
     * 
     * @return the name of agent running this recurring charge.
     */
    public String getAgentName()
    {
        return this.agentName_;
    }

    /**
     * @return the preBilling_
     */
    public boolean isRecurringRecharge()
    {
        return recurringRecharge_;
    }


    
    /**
     * @param preBilling the preBilling_ to set
     */
    public void setRecurringRecharge(boolean recurringRecharge)
    {
        recurringRecharge_ = recurringRecharge;
    }


    
    /**
     * @return the proRated_
     */
    public boolean isProRated()
    {
        return proRated_;
    }


    /**
     * @return the proRated_
     */
    public boolean isPreWarnNotificationOnly()
    {
        return preWarnNotificationOnly_;
    }
    
    /**
     * @param proRated the proRated_ to set
     */
    public void setProRated(boolean proRated)
    {
        proRated_ = proRated;
    }

    /**
     * Accumulates all counters from the provided visitor.
     * 
     * @param visitor
     *            Recurring charge visitor.
     */
    public synchronized void accumulate(final RechargeVisitorCountable visitor)
    {
        if (visitor == null)
        {
            return;
        }

        chargeAmount_ += visitor.getChargeAmount();
        chargeAmountSuccess_ += visitor.getChargeAmountSuccess();
        chargeAmountFailed_ += visitor.getChargeAmountFailed();
        servicesCount_ += visitor.getServicesCount();
        servicesCountSuccess_ += visitor.getServicesCountSuccess();
        servicesCountFailed_ += visitor.getServicesCountFailed();
        packagesCount_ += visitor.getPackagesCount();
        packagesFailedCount_ += visitor.getPackagesFailedCount();
        packagesSuccessCount_ += visitor.gePackagesSuccessCount();
        bundleCount_ += visitor.getBundleCount();
        bundleCountSuccess_ += visitor.getBundleCountSuccess();
        bundleCountFailed_ += visitor.getBundleCountFailed();
        bundleCountSuspend_ += visitor.getBundleCountSuspend();
        serviceCountSuspend_ += visitor.getServiceCountSuspend();
        packageCountSuspend_ += visitor.getPackageCountSuspend();
        subscriberCount_ += visitor.getSubscriberCount();
        subscriberFailCount_ += visitor.getSubscriberFailCount();
        subscriberSuccessCount_ += visitor.getSubscriberSuccessCount();
    }

    /**
     * Total amount attempted to charge.
     */
    private long chargeAmount_;

    /**
     * Total amount successfully charged.
     */
    private long chargeAmountSuccess_;

    /**
     * Total amount failed to charge.
     */
    private long chargeAmountFailed_;

    /**
     * Total number of services attempted to charge.
     */
    private int servicesCount_;

    /**
     * Total number of services charged successfully.
     */
    private int servicesCountSuccess_;

    /**
     * Total number of services failed to charge.
     */
    private int servicesCountFailed_;

    /**
     * Total number of service packages attempted to charge.
     */
    private int packagesCount_;

    /**
     * Total number of service packages failed to charge.
     */
    private int packagesFailedCount_;

    /**
     * Total number of service packages successfully charged.
     */
    private int packagesSuccessCount_;

    /**
     * Total number of bundles attempted to charge.
     */
    private int bundleCount_;

    /**
     * Total number of bundles successfully charged.
     */
    private int bundleCountSuccess_;

    /**
     * Total number of bundles failed to charge.
     */
    private int bundleCountFailed_;

    /**
     * Total number of bundles suspended.
     */
    private int bundleCountSuspend_;

    /**
     * Total number of services suspended.
     */
    private int serviceCountSuspend_;

    /**
     * Total number of service packages suspended.
     */
    private int packageCountSuspend_;

    /**
     * Total number of accounts attempted to charge.
     */
    private int accountCount_;

    /**
     * Total number of accounts successfully charged.
     */
    private int accountSuccessCount_;

    /**
     * Total number of accounts failed to charge.
     */
    private int accountFailCount_;

    /**
     * Total number of subscribers attempted to charge.
     */
    private int subscriberCount_;

    /**
     * Total number of subscribers failed to charge.
     */
    private int subscriberFailCount_;

    /**
     * Total number of subscribers successfully charged.
     */
    private int subscriberSuccessCount_;

    /**
     * Whether to suspend on failure.
     */
    private boolean suspendOnFailure_;

    /**
     * Billing date.
     */
    private final Date billingDate_;

    /**
     * Name of the agent generating the recurring charge.
     */
    private final String agentName_;

    /**
     * Service period to charge.
     */
    private final ChargingCycleEnum chargingCycle_;
    
    /**
     * Indicates whether to use prebilling or not.
     */
    private boolean recurringRecharge_;
    
    /**
     * Indicates whether this charge is prorated or not.
     */
    private boolean proRated_;
    
    /**
     * Indicates whether items should be charged or task's running only for pre-warn notification purposes.
     */
    private boolean preWarnNotificationOnly_;

}
