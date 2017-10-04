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

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.BillCycle;
import com.trilogy.app.crm.bean.CRMSpid;
import com.trilogy.app.crm.bean.ChargedItemTypeEnum;
import com.trilogy.app.crm.bean.ChargingCycleEnum;
import com.trilogy.app.crm.bean.ServicePeriodEnum;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberTypeEnum;
import com.trilogy.app.crm.bean.SysFeatureCfg;
import com.trilogy.app.crm.bean.core.Transaction;
import com.trilogy.app.crm.home.PostpaidSupportMsisdnTransHome;
import com.trilogy.app.crm.service.ChargingCycleHandler;
import com.trilogy.app.crm.service.ServicePeriodHandler;
import com.trilogy.app.crm.support.CalendarSupportHelper;
import com.trilogy.app.crm.support.ChargingCycleSupportHelper;
import com.trilogy.app.crm.support.EnumStateSupportHelper;
import com.trilogy.app.crm.support.HomeSupportHelper;
import com.trilogy.app.crm.support.RecurringRechargeSupport;
import com.trilogy.app.crm.support.ServicePeriodSupportHelper;
import com.trilogy.app.crm.support.SpidSupport;
import com.trilogy.framework.xhome.beans.SafetyUtil;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xlog.log.LogSupport;


/**
 * Super class of RechargheSubscriberServiceVisitor, RechargeSubscriberPackageVisitor,
 * RechargeSubscriberBundleVisitor and RecahargeSubscriberAuxServiceVisitor.
 *
 * @author larry.xia@redknee.com
 */

public abstract class AbstractRechargeItemVisitor extends AbstractRechargeVisitor
{

    /**
     * Create a new instance of <code>AbstractRechargeItemVisitor</code>.
     *
     * @param billingDate
     *            Billing date.
     * @param agentName
     *            Agent name.
     * @param servicePeriod
     *            Service period to charge.
     */
    public AbstractRechargeItemVisitor(final Date billingDate, final String agentName,
            final ChargingCycleEnum chargingCycle, final Date startDate, final Date endDate, final double rate, final boolean recurringRecharge, final boolean proRated, final boolean smsNotificationOnly)
    {
        super(billingDate, agentName, chargingCycle, recurringRecharge, proRated,smsNotificationOnly);
        this.rate_ = rate;
        this.startDate_ = startDate;
        this.endDate_ = endDate;
        this.itemRate_ = rate;
        this.chargedItemsMap_ = new ArrayList<Object>();
        this.chargingFailedItemsMap_ = new ArrayList<Object>();
        this.transactionsIds_ = new HashSet<Long>();
    }

    public AbstractRechargeItemVisitor(final Context context, final Date billingDate, final String agentName,
            final ChargingCycleEnum chargingCycle, final int billCycleDay, final int spid, final double rate, final boolean preBilling, final boolean proRated, final boolean smsNotificationOnly) throws HomeException
    {
        super(billingDate, agentName, chargingCycle, preBilling, proRated, smsNotificationOnly);
        
        ChargingCycleHandler handler = ChargingCycleSupportHelper.get(context).getHandler(chargingCycle);
        
        this.rate_ = rate;
        this.startDate_ = handler.calculateCycleStartDate(context, billingDate, billCycleDay, spid);
        this.endDate_ = handler.calculateCycleEndDate(context, billingDate, billCycleDay, spid);
        this.itemRate_ = rate;
    }

    public void setStartDate(Date startDate)
    {
    	this.startDate_ = startDate;
    }
    
    public Date getStartDate()
    {
    	return this.startDate_;
    }
    
    public Date getEndDate()
    {
    	return this.endDate_;
    }
    
    public void setEndDate(Date endDate)
    {
    	this.endDate_ = endDate;
    }
    
    
    /**
     * Creates recharge error report.
     *
     * @param ctx
     *            The operating context.
     * @param trans
     *            Transaction triggering the error.
     * @param resultCode
     *            Result code to report.
     * @param ocgResultCode
     *            OCG result code.
     * @param reason
     *            Reason of the failure.
     * @param chargedItemId
     *            Identifier of the item being charged.
     * @param chargedItemType
     *            The type of the item being charged.
     */
    public void handleFailure(final Context ctx, final Transaction trans, final int resultCode,
            final int ocgResultCode, final String reason, final long chargedItemId,
            final ChargedItemTypeEnum chargedItemType)
    {
        RecurringRechargeSupport.handleRechargeFailure(ctx, getAgentName(), getBillingDate(), getSub(), trans,
                resultCode, ocgResultCode, reason, chargedItemId, chargedItemType);
    }

    /**
     * Check whether or not to drop zero amount transactions.
     * @param ctx
     * @return
     */
    public boolean isDropZeroAmountTransactions(final Context ctx)
    {
        SysFeatureCfg sysCfg = (SysFeatureCfg) ctx.get(SysFeatureCfg.class);
        return sysCfg.isDropZeroAmountTransaction();     
    }
    
    public boolean generateRechargeFailureErOnOCGBypass(final Context ctx)
    {
        SysFeatureCfg sysCfg = (SysFeatureCfg) ctx.get(SysFeatureCfg.class);
        return sysCfg.isGenerateRechargeFailureEr();     
    }
    

    /**
     * Creates an ER for the recurring charge.
     *
     * @param ctx
     *            The operating context.
     * @param trans
     *            Recurring charge transaction.
     * @param id
     *            Identifier of the service being charged.
     * @param resultCode
     *            Result code.
     * @param ocgResultCode
     *            OCG result code.
     */
    public void createER(final Context ctx, final Transaction trans, final String id, final int resultCode,
            final int ocgResultCode)
    {
        RecurringRechargeSupport.createRechargeER(ctx, trans, getSub(), id, resultCode, ocgResultCode,
                sub_.getSubscriptionType());
    }


    /**
     * Create a transaction object with the provided recurring recharge detail. The
     * created transaction is not actually added to transaction home.
     *
     * @param context
     *            The operating context.
     * @param adj
     *            Adjustment type.
     * @param fee
     *            The full monthly fee to be charged.
     * @return A transaction with the provided recurring recharge detail.
     * @throws HomeException
     *             Thrown if there are problems calculating the pro-rated ratio.
     */
    public Transaction createTransaction(final Context context, final int adj, final long fee) throws HomeException
    {
        // TODO 2009-11-17 if this method is only used during recurring recharge then the rate will always be 1
        return RecurringRechargeSupport.createRechargeTransaction(context, getSub(), itemRate_, adj, fee, getAgentName(),
                getRealBillingDate(context), origServiceEndDate_, sub_.getSubscriptionType());
    }
    
    /**
     * Create a transaction object with the provided recurring recharge detail. The
     * created transaction is not actually added to transaction home.
     *
     * @param context
     *            The operating context.
     * @param adj
     *            Adjustment type.
     * @param fee
     *            The full monthly fee to be charged.
     * @return A transaction with the provided recurring recharge detail.
     * @throws HomeException
     *             Thrown if there are problems calculating the pro-rated ratio.
     */
    public Transaction createCUGOwnerTransaction(final Context context, final int adj, final long fee, Subscriber sub) throws HomeException
    {
        // TODO 2009-11-17 if this method is only used during recurring recharge then the rate will always be 1
        return RecurringRechargeSupport.createCUGOwnerRechargeTransaction(context, sub, itemRate_, adj, fee, getAgentName(),
                getRealBillingDate(context), origServiceEndDate_, sub.getSubscriptionType(), getSub().getBAN());
    }


    
    public boolean isItemChargeable(Context ctx, boolean itemIsCharbeableWhileSuspended) throws HomeException
    {
        // No need to check if subscriber is in rechargeable state as it was already checked in the query. We only care here whether
        // the subscriber is not SUSPENDED/IN ARREARS, or if he/she is, that the item is chargeable while suspended and that is supported
        // by the spid.
        return EnumStateSupportHelper.get(ctx).isNotOneOfStates(getSub().getState().getIndex(), RechargeConstants.RECHARGE_SUBSCRIBER_STATES_SUSPENDED) 
            || (itemIsCharbeableWhileSuspended && getCRMSpid(ctx).isApplyRecurringChargeForSuspendedSubscribers());
    }

    /**
     * Check if the adjustment type is chargeable to the subscriber.
     *
     * @param context
     *            The operating context.
     * @param item
     *            Item being charged.
     * @param itemType
     *            Item type being charged.
     * @param adjustmentType
     *            Adjustment type.
     * @param amount
     *            Amount being charged.
     * @param servicePeriod
     *            The service period to be charged.
     * @return true if chargeable, false if not
     * @throws HomeException
     *             Thrown if there are problems determining whether the subscriber was
     *             already charged for the billing cycle.
     */
    public boolean isChargeable(final Context parentCtx, final Object item, final ChargedItemTypeEnum itemType,
            final int adjustmentType, final long amount, 
            final ServicePeriodEnum servicePeriod)
        throws HomeException
    {
        Context context = parentCtx.createSubContext();
        context.put(Subscriber.class, getSub());
        
        Date startDate = startDate_;
        
        if (ServicePeriodSupportHelper.get(context).usesSpecialHandler(context, servicePeriod))
        {
            ServicePeriodHandler handler = ServicePeriodSupportHelper.get(context).getHandler(servicePeriod);
            
            startDate = handler.calculateCycleStartDate(context, getBillingDate(), getBillCycle(context).getDayOfMonth(), getCRMSpid(context).getId(), getSub().getId(), item);
            origServiceEndDate_ = handler.calculateCycleEndDate(context, getBillingDate(), getBillCycle(context).getDayOfMonth(), getCRMSpid(context).getId(), getSub().getId(), item);
        }
        else
        {
            origServiceEndDate_ = endDate_;
        }
        
        
        boolean preBiling = spidUsesPreBilling(context) && ChargingCycleEnum.MONTHLY.equals(servicePeriod.getChargingCycle()) && !getSub().isPrepaid();

        return RecurringRechargeSupport.isSubscriberChargeable(context, getSub(), item, itemType, adjustmentType,
                amount, getAgentName(), getBillingDate(), 
                startDate, endDate_, preBiling);        
        
    }
    

    /**
     * Returns the real billing date, taking prebilled into account.
     *
     * @param context
     *            THe operating context.
     * @return the real billing date, taking prebilled into account.
     */
    public Date getRealBillingDate(final Context context)
    {
        Date billingDate = getBillingDate();

        // Only monthly recharge needs prebill
        if (isRecurringRecharge() && SafetyUtil.safeEquals(getChargingCycle(), ChargingCycleEnum.MONTHLY) && !getSub().isPrepaid())
        {
            boolean isPreBilled = false;
            
            try
            {
                if (isPreBilled(context))
                {
                    billingDate = CalendarSupportHelper.get(context).getDayBefore(billingDate);
                }
            }
            catch (final Throwable t)
            {
                LogSupport.minor(context, this, "invalid SPID");
                /*
                 * the chance for such such exception is really low, we can afford to ignore
                 * it.
                 */
            }
            
        }
        if(getSub().isPrepaid())//The billing date is set in context only to modify receive date using bill date in PPSM transaction for prepaid. 
            context.put(PostpaidSupportMsisdnTransHome.PPSM_BILLING_DATE,billingDate.getTime()); 


        return billingDate;
    }
    
    protected abstract void accumulateForOne(final boolean success, final long amount);
 
    
    protected void handlePreWarnNotification(Context context, Object itemFee, long value) throws HomeException
    {
        if (getCRMSpid(context).isRecurringChargePrepaidNotification() && getSub().isPrepaid())
        {
            accumulateForOne(true, value);
            chargedItemsMap_.add(itemFee);
        }
    }
    
    protected void handleAllOrNothingChargeAccumulation(Context context, Object itemFee, long value) throws HomeException
    {
    	accumulateForOne(true, value);
    }
    
    private boolean isZeroChargeServiceNotifiable(Context ctx)
    {
    	try
    	{
    		CRMSpid spidBean = HomeSupportHelper.get(ctx).findBean(ctx, CRMSpid.class, ((Subscriber)ctx.get(Subscriber.class)).getSpid());
    		return spidBean.isZeroRechargeNotification();
    	}
    	catch(Throwable e)
    	{
    		LogSupport.minor(ctx, this, "Exception in isZeroChargeServiceNotifiable:" + e.getMessage());    		
    	}
    	return false;
    }
    
    protected void handleRechargeFailureNotification(Context context, Object itemFee, long fee)
    {
    	if(!isZeroChargeServiceNotifiable(context) && fee == 0)
    	{
    		return;
    	}
    	chargingFailedItemsMap_.add(itemFee);
    }
    
    protected void handleRechargeNotification(Context context, Object itemFee, long fee)
    {
    	if(!isZeroChargeServiceNotifiable(context) && fee == 0)
    	{
    		return;
    	}    	
    	chargedItemsMap_.add(itemFee);
    }
    
    protected List<Object> getChargedItems()
    {
        return chargedItemsMap_;
    }

    protected List<Object> getChargingFailedItems()
    {
    	return chargingFailedItemsMap_;
    }

    /**
     * Returns whether the subscriber is chargeable.
     *
     * @return Whether the subscriber is chargeable.
     */
    public final boolean isChargeable()
    {
        return this.chargeable_;
    }


    /**
     * Sets whether the subscriber is chargeable.
     *
     * @param chargeable
     *            Whether the subscriber is chargeable.
     */
    public final void setChargeable(final boolean chargeable)
    {
        this.chargeable_ = chargeable;
    }


    /**
     * Returns the subscriber to be charged.
     *
     * @return The subscriber to be charged.
     */
    public final Subscriber getSub()
    {
        return this.sub_;
    }


    /**
     * Sets the subscriber to be charged.
     *
     * @param sub
     *            The subscriber to be charged.
     */
    public final void setSub(final Subscriber sub)
    {
        this.sub_ = sub;
    }
    
    public double getItemRate()
    {
        return itemRate_;
    }

    
    public void setItemRate(double itemRate)
    {
        itemRate_ = itemRate;
    }

    
    public double getRate()
    {
        return rate_;
    }
    
    public CRMSpid getCRMSpid(Context ctx) throws HomeException
    {
        CRMSpid crmSpid = (CRMSpid) ctx.get(CRMSpid.class);
        if (crmSpid == null || crmSpid.getSpid()!=getSub().getSpid())
        {
            crmSpid = SpidSupport.getCRMSpid(ctx, getSub().getSpid());
            ctx.put(CRMSpid.class, crmSpid);
        }
        return crmSpid;
    }
    
    public boolean isPreBilled(Context ctx) throws HomeException
    {
        return getCRMSpid(ctx).isPrebilledRecurringChargeEnabled();
    }
    
    public BillCycle getBillCycle(Context ctx) throws HomeException
    {
        BillCycle billCycle = (BillCycle) ctx.get(BillCycle.class);
        Account account = (Account) ctx.get(Account.class);
        if (billCycle==null || account==null || !SafetyUtil.safeEquals(account.getBAN(),getSub().getBAN()) || 
                account.getBillCycleID()!=billCycle.getBillCycleID())
        {
            if (account==null)
            {
                account = getSub().getAccount(ctx);
                ctx.put(Account.class, account);
            }
            billCycle = account.getBillCycle(ctx);
            ctx.put(BillCycle.class, billCycle);
        }
        return billCycle;

    }
    
    public void setOrigServiceEndDate(Date origServiceEndDate)
    {
    	this.origServiceEndDate_ = origServiceEndDate;
    }
    public Date getOrigServiceEndDate()
    {
        return origServiceEndDate_;
    }

    public boolean spidUsesPreBilling(Context ctx) throws HomeException
    {
        return getCRMSpid(ctx).isPrebilledRecurringChargeEnabled();
    }


    /**
     * Determines whether the service should be suspended.
     *
     * @return Returns whether the service should suspended.
     */
    protected boolean isSubscriberSuspending()
    {
        boolean result = false;
        if (isSuspendOnFailure() && getSub().isSuspendingEntities()
            && getSub().getSubscriberType().equals(SubscriberTypeEnum.PREPAID))
        {
            result = true;
        }
        return result;
    }

    /**
     * 
     */
    public void addTransactionId(Long transId)
    {
        transactionsIds_.add(transId);
        
    }
    
    public Set<Long> getAllSuccessfulTransactionIds()
    {
        return transactionsIds_;
    }
    
    /**
     * Whether the subscriber is chargeable.
     */
    private boolean chargeable_;

    /**
     * Subscriber to be charged.
     */
    private Subscriber sub_;
    
    /**
     * Rate to be used for charging
     */
    private double rate_;
    
    /**
     * Billing cycle start date.
     */
    private Date startDate_;
    
    /**
     * Billing cycle end date.
     */
    private Date endDate_;
    
    private Date origServiceEndDate_;
    
    private double itemRate_;
    
    private List<Object> chargedItemsMap_;
    
    private List<Object> chargingFailedItemsMap_;
    
    private Set<Long> transactionsIds_;
    
}
