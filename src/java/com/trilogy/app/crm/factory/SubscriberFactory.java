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

package com.trilogy.app.crm.factory;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import com.trilogy.app.crm.bean.ServiceFee2ID;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextFactory;
import com.trilogy.framework.xhome.context.ContextFactoryProxy;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.msp.Spid;
import com.trilogy.framework.xlog.log.DebugLogMsg;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.framework.xlog.log.MinorLogMsg;
import com.trilogy.framework.xlog.log.OMLogMsg;

import com.trilogy.app.crm.Common;
import com.trilogy.app.crm.LicenseConstants;
import com.trilogy.app.crm.agent.BeanInstall;
import com.trilogy.app.crm.bean.AbstractSubscriber;
import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.CRMSpid;
import com.trilogy.app.crm.bean.CRMSpidHome;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberTypeEnum;
import com.trilogy.app.crm.bean.account.SubscriptionType;
import com.trilogy.app.crm.bean.core.CreditCategory;
import com.trilogy.app.crm.bean.core.SubscriptionClass;
import com.trilogy.app.crm.bean.template.SubscriberTemplate;
import com.trilogy.app.crm.extension.account.GroupPricePlanExtension;
import com.trilogy.app.crm.extension.account.GroupPricePlanExtensionXInfo;
import com.trilogy.app.crm.support.CreditCategorySupport;
import com.trilogy.app.crm.support.ExtensionSupportHelper;
import com.trilogy.app.crm.support.LicensingSupportHelper;
import com.trilogy.app.crm.support.SubscriberSupport;
import com.trilogy.app.crm.technology.TechnologyEnum;

/**
 * Factory for creating Subscribers.
 * Sets the context and spid.
 */
public class SubscriberFactory extends ContextFactoryProxy implements PropertyChangeListener
{

    public SubscriberFactory()
    {
        this(new PrototypeContextFactory(SubscriberTemplate.class));
    }
    
    public SubscriberFactory(ContextFactory delegate)
    {
        super(delegate);
        createCount_ = 0;
    }

    @Override
    public Object create(final Context ctx)
    {
        if (createCount_ > 10000)
        {
            createCount_ = 0;
            createCountOM_.log(ctx);
        }

        final Subscriber sub = (Subscriber) super.create(ctx);

        if (sub != null)
        {
            ++createCount_;

            return initSubscriber(ctx, sub);
        }

        return null;
    }

    /**
     * Set initial values to a Subscriber. Used by AccountFactory also.
     */
    public static Subscriber initSubscriber(final Context ctx, final Subscriber sub)
    {
        final Spid spid = (Spid) ctx.get(Spid.class);

        sub.setContext(ctx.createSubContext());

        // only turn this on if you REALLY need this kind of debugging [PaulS]
        // sub.addPropertyChangeListener(this);

        // Set the Spid to be the same as the current Account if available
        final Account account = (Account) ctx.get(Account.class);
        if (account != null)
        {
        	sub.setSpid(account.getSpid());
        }
        sub.setMonthlySpendLimit(AbstractSubscriber.DEFAULT_MONTHLYSPENDLIMIT);

        setDetailsFromSpid(ctx, sub);

        //we would like to keep all these defaul value defined in model files
        sub.setDeposit(AbstractSubscriber.DEFAULT_DEPOSIT);
        sub.setCreditLimit(AbstractSubscriber.DEFAULT_CREDITLIMIT);
        sub.setPricePlan(AbstractSubscriber.DEFAULT_PRICEPLAN);
        sub.setPricePlanVersion(AbstractSubscriber.DEFAULT_PRICEPLANVERSION);
        sub.setBalanceRemaining(AbstractSubscriber.DEFAULT_BALANCEREMAINING);
        sub.setOverdraftBalance(AbstractSubscriber.DEFAULT_OVERDRAFTBALANCE);
        sub.setOverdraftDate(AbstractSubscriber.DEFAULT_OVERDRAFTDATE);
        sub.setRealTimeBalance(AbstractSubscriber.DEFAULT_REALTIMEBALANCE);
        sub.setAmountOwing(AbstractSubscriber.DEFAULT_AMOUNTOWING);
        sub.setAbmCreditLimit(AbstractSubscriber.DEFAULT_ABMCREDITLIMIT);
        sub.setMonthToDateBalance(AbstractSubscriber.DEFAULT_MONTHTODATEBALANCE);
        sub.setLastInvoiceAmount(AbstractSubscriber.DEFAULT_LASTINVOICEAMOUNT);
        sub.setPaymentSinceLastInvoice(AbstractSubscriber.DEFAULT_PAYMENTSINCELASTINVOICE);
        sub.setAdjustmentsSinceLastInvoice(AbstractSubscriber.DEFAULT_ADJUSTMENTSSINCELASTINVOICE);
        sub.resetPoolID();

        sub.resetProvisionedBundles();

        sub.resetBackupServices();

        sub.getTransientProvisionedServices().clear();
        sub.setServices(new HashSet<ServiceFee2ID>());

        // Must set those dates that should be initialized to "now".
        final Date now = new Date();
        final Date endDate = SubscriberSupport.getFutureEndDate(now);

        sub.setDateCreated(now);
        //sub.setStartDate(now);
        sub.setStartDate(null);
        sub.setSecondaryPricePlanStartDate(now);
        sub.setSecondaryPricePlanEndDate(endDate);


        sub.setExpiryDate(endDate);
        //TODO, do we need to set end date before other dates?
        //sub.setEndDate(endDate);
        sub.setEndDate(null);
        // TODO 2008-08-21 date of birth no longer part of subscriber
        //sub.setDateOfBirth(null);

        sub.setSecondaryPricePlanStartDate(endDate);
        sub.setSecondaryPricePlanEndDate(endDate);

        if (account != null)
        {
            sub.setSpid(account.getSpid());
            if ((SubscriberTypeEnum.POSTPAID.equals(account.getSystemType())
                    || SubscriberTypeEnum.HYBRID.equals(account.getSystemType()))
                    && LicensingSupportHelper.get(ctx).isLicensed(ctx, LicenseConstants.POSTPAID_LICENSE_KEY))
            {
                sub.setSubscriberType(SubscriberTypeEnum.POSTPAID);
            }
            else if (SubscriberTypeEnum.PREPAID.equals(account.getSystemType())
                    && LicensingSupportHelper.get(ctx).isLicensed(ctx, LicenseConstants.PREPAID_LICENSE_KEY))
            {
                sub.setSubscriberType(SubscriberTypeEnum.PREPAID);
            }

            String groupMSISDN = null;
            // Set group price plan as default if the extension is in the account
            // NOTE: Don't call account.getAccountExtensions() here because we don't want to lazy-load
            // all of the extensions during bean instantiation.
            if (account.getParentBAN() != null)
            {
                List<GroupPricePlanExtension> gppExtensions = ExtensionSupportHelper.get(ctx).getExtensions(
                        ctx, 
                        GroupPricePlanExtension.class, 
                        new EQ(GroupPricePlanExtensionXInfo.BAN, account.getParentBAN()));
                groupMSISDN = account.getGroupMSISDN(ctx, sub.getSubscriptionType());
                if (gppExtensions != null && gppExtensions.size() > 0)
                {
                    sub.setPricePlan(gppExtensions.iterator().next().getGroupPricePlan(sub));
                }
            }
            fillSubscriptionType(ctx, sub, account);
            fillTechnology(ctx, sub , account);

            if(groupMSISDN == null)
            {
                groupMSISDN = account.getGroupMSISDN(ctx, sub.getSubscriptionType());
            }
            boolean isPooled = groupMSISDN!=null && groupMSISDN.length()>0;

            if (LicensingSupportHelper.get(ctx).isLicensed(ctx, LicenseConstants.GROUP_MEMBER_QUOTA_LIMITS_KEY) && isPooled )
            {
                // Get the default quota limit values from the account type for pooled subscribers
                // Only do this if the feature is licensed.  Use the default values otherwise.
                sub.setQuotaType(account.getQuotaType(ctx));
                sub.setQuotaLimit(account.getQuotaLimit(ctx));
            }
            else
            {
                sub.setQuotaType(AbstractSubscriber.DEFAULT_QUOTATYPE);
                sub.setQuotaLimit(AbstractSubscriber.DEFAULT_QUOTALIMIT);
            }

                
            //It should only be set if it is postpaid and not pooled             
            if ( (!(account.getSystemType().equals(SubscriberTypeEnum.PREPAID)))  && (!isPooled))
            {
                try
            {
                    CreditCategory cat = CreditCategorySupport.findCreditCategory(ctx, account.getCreditCategory());
                    if (cat != null)
                    {
                        sub.setMonthlySpendLimit(cat.getMonthlySpendLimit());
                    }
            }
                catch (HomeException homeEx)
            {
                    new MinorLogMsg(SubscriberFactory.class, " Unable to find the credit category", homeEx).log(ctx);
                }
            }
        }
        else
        {
            //I found FW puts spid(0) in context, if agent spid is 0
            //TT6031732090
            if (spid!=null && spid.getId() > 0)
            {
                sub.setSpid(spid.getId());
                new MinorLogMsg(SubscriberFactory.class.getName(), "initSubscriber:: Subscriber [" + spid.getId() + "]", null).log(ctx);
            }
            sub.setQuotaType(AbstractSubscriber.DEFAULT_QUOTATYPE);
            sub.setQuotaLimit(AbstractSubscriber.DEFAULT_QUOTALIMIT);
            sub.setSubscriberType(AbstractSubscriber.DEFAULT_SUBSCRIBERTYPE);
        }
        new MinorLogMsg(SubscriberFactory.class.getName(), "initSubscriber:: Return the Subscriber [" + sub + "] successfully", null).log(ctx);
        return sub;
    }

    /**
     * Take some initial values from CRMSpid and set into Subscriber.
     */
    public static void setDetailsFromSpid(final Context ctx, final Subscriber sub)
    {
        try
        {
            final Home home = (Home) ctx.get(CRMSpidHome.class);
            final CRMSpid spid = (CRMSpid) home.find(ctx, Integer.valueOf(sub.getSpid()));

            if (spid != null)
            {
                sub.setBillingOption(spid.getBillingOption());
                sub.setHlrId((short) spid.getDefaultHlrId());
                
                if (spid.getDealer()!= CRMSpid.DEFAULT_DEALER)
                {
                    sub.setDealerCode(spid.getDealer());
                }
                
                if (spid.getCurrency()!= CRMSpid.DEFAULT_CURRENCY)
                {
                    sub.setCurrency(spid.getCurrency());
                }
            }
            else
            {
                new MinorLogMsg(SubscriberFactory.class,
                        "Unable to determine default billing option. No SPID found with identifier " + sub.getSpid(),
                        null).log(ctx);
            }
        }
        catch (HomeException exception)
        {
            new MinorLogMsg(SubscriberFactory.class, "Unable to determine default billing option.", exception).log(ctx);
        }
    }

    /**
     * Receives property change events on a generated Subscriber.  Used for debugging.
     *
     * @see java.beans.PropertyChangeListener#propertyChange(java.beans.PropertyChangeEvent)
     */
    @Override
    public void propertyChange(final PropertyChangeEvent e)
    {
        System.out.println("\n Property name : " + e.getPropertyName());
        System.out.println("Old Value : " + e.getOldValue());
        System.out.println("New Value : " + e.getNewValue());
    }
    
    private static Subscriber fillSubscriptionType(Context ctx, Subscriber subscriber, Account subscriberAccount)
    {
        final SubscriptionType subType = SubscriptionClass.getSubscriptionTypeForClass(SubscriptionClass.filterHomeOnBillingType(ctx, subscriberAccount.getSystemType()), subscriber.getSubscriptionClass(),
                AbstractSubscriber.DEFAULT_SUBSCRIPTIONCLASS);
        if(null != subType)
        {
            subscriber.setSubscriptionType(subType.getId());
            if (LogSupport.isDebugEnabled(ctx))
            {
                new DebugLogMsg(SubscriberFactory.class.getName(), "filling subscription-type to [ +  "
                        + String.valueOf(subType) + "] for Subscriber [" + subscriber.getId() + "]", null).log(ctx);
            }
        } else
        {
            new MinorLogMsg(SubscriberFactory.class.getName(), "Could not intilaize Subscirption-Type for Subscriber [" + subscriber.getId() + "]", null).log(ctx);
        }
        return subscriber;
    }
    
    private static Subscriber fillTechnology(Context ctx, Subscriber subscriber, Account subscriberAccount)
    {
        final TechnologyEnum technology = SubscriptionClass.getTechnologyForSubscriptionClass(SubscriptionClass.filterHomeOnBillingType(ctx, subscriberAccount.getSystemType()), subscriber.getSubscriptionClass(),
                AbstractSubscriber.DEFAULT_SUBSCRIPTIONCLASS);
        if (null != technology)
        {
            new DebugLogMsg(SubscriberFactory.class.getName(), "filling technology-type to [ +  "
                    + String.valueOf(technology) + "] for Subscriber [" + subscriber.getId() + "]", null).log(ctx);
        } else
        {
            new MinorLogMsg(SubscriberFactory.class.getName(), "Could not intilaize Technology-Type for Subscriber [" + subscriber.getId() + "]", null).log(ctx);
        }
        return subscriber;
    }

    private final OMLogMsg createCountOM_ = new OMLogMsg(Common.OM_MODULE, "OM_SUBSCRIBER_FACTORY_CREATE");
    private int createCount_;
}
