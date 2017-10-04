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
package com.trilogy.app.crm.client.bm;

import java.util.Date;
import java.util.List;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.HomeException;

import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.product.bundle.manager.provision.common.param.Parameter;
import com.trilogy.product.bundle.manager.provision.v5_0.profile.BCDChangeRequestReturnParam;


/**
 * Provides a simple proxy for the SubscriberProfileProvisionClient.
 *
 * @author gary.anderson@redknee.com
 */
public class SubscriberProfileProvisionClientProxy
    implements SubscriberProfileProvisionClient
{

    
    public List<Parameters> queryAllSubscriptionsForSubscriber(final Context context, final String subscriberID)
    throws SubscriberProfileProvisionException, HomeException
    {
        return delegate_.queryAllSubscriptionsForSubscriber(context, subscriberID);
    }
    /**
     * Creates a new proxy for the given delegate.
     *
     * @param delegate The client to which this proxy delegates.
     */
    public SubscriberProfileProvisionClientProxy(final SubscriberProfileProvisionClient delegate)
    {
        delegate_ = delegate;
    }


    /**
     * Gets the delegate.
     *
     * @return The delegate.
     */
    public SubscriberProfileProvisionClient getDelegate()
    {
        return delegate_;
    }


    /**
     * Sets the delegate.
     *
     * @param delegate The delegate.
     */
    public void setDelegate(final SubscriberProfileProvisionClient delegate)
    {
        delegate_ = delegate;
    }


    // Delegate methods generated by Eclipse.


    /**
     * {@inheritDoc}
     */
    public void addSubscriberAccountProfile(final Context context, final Account subscriberAccount)
        throws HomeException, SubscriberProfileProvisionException
    {
        delegate_.addSubscriberAccountProfile(context, subscriberAccount);
    }


    /**
     * {@inheritDoc}
     */
    public void addSubscriptionProfile(final Context context, final Subscriber subscription)
        throws HomeException, SubscriberProfileProvisionException
    {
        delegate_.addSubscriptionProfile(context, subscription);
    }


    /**
     * {@inheritDoc}
     */
    public long adjustCreditLimit(final Context context, final Subscriber subscription, final long amount,
        final String erReference)
        throws HomeException, SubscriberProfileProvisionException
    {
        return delegate_.adjustCreditLimit(context, subscription, amount, erReference);
    }
    
    
    /**
     * {@inheritDoc}
     */
    public long adjustCreditLimit(final Context context, final Subscriber subscription, final long newCreditLimit, final long oldCreditLimit, final String erReference)
        throws HomeException, SubscriberProfileProvisionException
    {
        return delegate_.adjustCreditLimit(context, subscription, newCreditLimit, oldCreditLimit, erReference);
    }


    /**
     * {@inheritDoc}
     */
    public void adjustGroupQuota(final Context context, final Subscriber subscription, final long amount,
        final String erReference)
        throws HomeException, SubscriberProfileProvisionException
    {
        delegate_.adjustGroupQuota(context, subscription, amount, erReference);
    }


    /**
     * {@inheritDoc}
     */
    public void deleteSubscriberAccountProfile(final Context context, final Account subscriberAccount)
        throws HomeException, SubscriberProfileProvisionException
    {
        delegate_.deleteSubscriberAccountProfile(context, subscriberAccount);
    }


    /**
     * {@inheritDoc}
     */
    public void deleteSubscriptionProfile(final Context context, final Subscriber subscription)
        throws HomeException, SubscriberProfileProvisionException
    {
        delegate_.deleteSubscriptionProfile(context, subscription);
    }


    /**
     * {@inheritDoc}
     */
    public Parameters querySubscriberAccountProfile(final Context context, final Account subscriberAccount)
        throws HomeException, SubscriberProfileProvisionException
    {
        return delegate_.querySubscriberAccountProfile(context, subscriberAccount);
    }


    /**
     * {@inheritDoc}
     */
    public Parameters querySubscriptionProfile(final Context context, final Subscriber subscription)
        throws HomeException, SubscriberProfileProvisionException
    {
        return delegate_.querySubscriptionProfile(context, subscription);
    }


    /**
     * {@inheritDoc}
     */
    public void updateBalance(final Context context, final Subscriber subscription, final long balance)
        throws HomeException, SubscriberProfileProvisionException
    {
        delegate_.updateBalance(context, subscription, balance);
    }


    /**
     * {@inheritDoc}
     */
    public void updateBillingDay(final Context context, final Account subscriberAccount, final int day)
        throws HomeException, SubscriberProfileProvisionException
    {
        delegate_.updateBillingDay(context, subscriberAccount, day);
    }


    /**
     * {@inheritDoc}
     */
    public void updateBirthDay(final Context context, final Account subscriberAccount)
        throws HomeException, SubscriberProfileProvisionException
    {
        delegate_.updateBirthDay(context, subscriberAccount);
    }


    /**
     * @{inheritDoc}
     */
    public void updateBAN(Context context, Subscriber subscription, String newBan) throws HomeException,
            SubscriberProfileProvisionException
    {
        delegate_.updateBAN(context, subscription, newBan);
    }


    /**
     * {@inheritDoc}
     */
    public void updateExpiryDate(final Context context, final Subscriber subscription, final Date expiryDate)
        throws HomeException, SubscriberProfileProvisionException
    {
        delegate_.updateExpiryDate(context, subscription, expiryDate);
    }


    /**
     * {@inheritDoc}
     */
    public void updateMobileNumber(final Context context, final Subscriber subscription, final String oldMobileNumber)
        throws HomeException, SubscriberProfileProvisionException
    {
        delegate_.updateMobileNumber(context, subscription, oldMobileNumber);
    }


    /**
     * {@inheritDoc}
     */
    public void updatePooledGroupID(final Context context, final Subscriber subscription,
        final String groupIdentifier, final boolean clearQuota)
        throws HomeException, SubscriberProfileProvisionException
    {
        delegate_.updatePooledGroupID(context, subscription, groupIdentifier, clearQuota);
    }


    /**
     * {@inheritDoc}
     */
    public void updatePooledGroupOwner(final Context context, final Subscriber subscription, final String ownerIdentifier)
        throws HomeException, SubscriberProfileProvisionException
    {
        delegate_.updatePooledGroupOwner(context, subscription, ownerIdentifier);
    }


    /**
     * {@inheritDoc}
     */
    public void updatePricePlan(final Context context, final Subscriber subscription, final long pricePlan)
        throws HomeException, SubscriberProfileProvisionException
    {
        delegate_.updatePricePlan(context, subscription, pricePlan);
    }


    /**
     * {@inheritDoc}
     */
    public void updateSubscriptionQuotaLimit(final Context context, final Subscriber subscription, final long quota)
        throws HomeException, SubscriberProfileProvisionException
    {
        delegate_.updateSubscriptionQuotaLimit(context, subscription, quota);
    }

    /**
     * {@inheritDoc}
     */
    public void updateState(Context context, Subscriber subscription, int bmState) throws HomeException,
            SubscriberProfileProvisionException
    {
        delegate_.updateState(context, subscription, bmState);
    }
    
    /**
     * {@inheritDoc}
     */
    public Parameters removeSubscriptionProfile(Context context, Subscriber subscription)
            throws SubscriberProfileProvisionException
    {
        // TODO Auto-generated method stub
        return delegate_.removeSubscriptionProfile(context, subscription);
    }
    
    public void resetGroupUsage(Context context, Subscriber subscription)
            throws SubscriberProfileProvisionException
    {
        delegate_.resetGroupUsage(context, subscription);
    }
    
    public void resetMonthlySpendUsage(Context context, Subscriber subscription)
            throws SubscriberProfileProvisionException
    {
        delegate_.resetMonthlySpendUsage(context, subscription);
    }
    
    public void updateSubscriptionMonthlySpendLimit(Context context,
            Subscriber subscription, long spendLimit) throws HomeException, SubscriberProfileProvisionException
    {
        delegate_.updateSubscriptionMonthlySpendLimit(context, subscription, spendLimit);
    }
    
    public void updatSubscriptionPPSMSupporter(final Context context, final Subscriber subscription,
            final String ppsmSupporterBan, final long ppsmScreeningTemplate) throws SubscriberProfileProvisionException
    {
        delegate_.updatSubscriptionPPSMSupporter(context, subscription, ppsmSupporterBan, ppsmScreeningTemplate);
    }

    public void updateDualBalance(Context context, Subscriber subscription, boolean status)
            throws SubscriberProfileProvisionException
    {
        delegate_.updateDualBalance(context, subscription, status);
    }

    /**
     * {@inheritDoc}
     */
    public void updateBillingLanguage(Context context, Subscriber subscription, String lang) throws HomeException,
            SubscriberProfileProvisionException
    {
        delegate_.updateBillingLanguage(context, subscription, lang);
    }

    /**
     * {@inheritDoc}
     */
    public BCDChangeRequestReturnParam[] addUpdateBcdChangeRequest(Context context, String[] subscriptionIDs,
            int newBillCycleDay) throws SubscriberProfileProvisionCorbaException
    {
        return delegate_.addUpdateBcdChangeRequest(context, subscriptionIDs, newBillCycleDay);
    }
    /**
     * {@inheritDoc}
     */
    public BCDChangeRequestReturnParam[] removeBcdChangeRequest(Context context, String[] subscriptionIDs)
            throws SubscriberProfileProvisionCorbaException
    {
        return delegate_.removeBcdChangeRequest(context, subscriptionIDs);
    }
    
    @Override
    public void updateOverdraftBalanceLimit(Context context, Subscriber subscription, long limit)
            throws SubscriberProfileProvisionException
    {
        delegate_.updateOverdraftBalanceLimit(context, subscription, limit);
    }

    
    @Override
    public void updateIMSI(Context context, Subscriber subscripition, String IMSI) throws HomeException,
            SubscriberProfileProvisionException
    {
        delegate_.updateIMSI(context, subscripition, IMSI);
        
    }

    @Override
    public void updatePromOptOut(Context context, Subscriber subscription, boolean status)
            throws SubscriberProfileProvisionException
    {
        delegate_.updatePromOptOut(context, subscription, status);
    }

	@Override
	public void updateStateAndExpiryDate(Context context,
			Subscriber subscription, int bmState, Date expiryDate)
			throws HomeException, SubscriberProfileProvisionException {
		delegate_.updateStateAndExpiryDate(context, subscription, bmState, expiryDate);
		
	}
	
	@Override
	public Parameters getSubscriptionProfile(Context context, String msisdn,
			int subscriptionType, Parameter[] inParamSet) throws HomeException,
			SubscriberProfileProvisionException {
		return delegate_.getSubscriptionProfile(context, msisdn, subscriptionType, inParamSet);
	}

    /**
     * The SubscriberProfileProvisionClient to which this proxy delegates.
     */
    private SubscriberProfileProvisionClient delegate_;

    /**
     * {@inheritDoc}
     */
    public void updateBalanceThresholdAmount(Context context, Subscriber subscription,
            long balanceThreshold) throws HomeException, SubscriberProfileProvisionException
    {
        delegate_.updateBalanceThresholdAmount(context, subscription, balanceThreshold);
    }
    /**
     * {@inheritDoc}
     */
    public void updateEmailId(final Context context, final Account subscriberAccount, final String email)
        throws HomeException, SubscriberProfileProvisionException
    {
        delegate_.updateEmailId(context, subscriberAccount, email);
    }
    /**
     * {@inheritDoc}
     */
    public void updateNotificationType(final Context context, final Subscriber subscription, final int type)
        throws HomeException, SubscriberProfileProvisionException
    {
        delegate_.updateNotificationType(context, subscription, type);
    }
    
    public void updateGroupScreeningTemplateId(Context context, Subscriber subscription, long groupScreeningTemplateId)
                throws HomeException, SubscriberProfileProvisionException
    {
        delegate_.updateGroupScreeningTemplateId(context, subscription, groupScreeningTemplateId);
    }
}
