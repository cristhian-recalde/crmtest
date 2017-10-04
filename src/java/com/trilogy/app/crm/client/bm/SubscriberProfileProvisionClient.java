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

import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.product.bundle.manager.provision.common.param.Parameter;
import com.trilogy.product.bundle.manager.provision.v5_0.profile.BCDChangeRequestReturnParam;


/**
 * Provides the CRM+ internal interface for communication with BM for
 * provisioning of subscriber account and subscription information.
 * Specifically, this interface provides access to functionality related to
 * balances, credit limit, billing day, prepaid lifecycle (including
 * expiration), group pooling, and pool quotas.
 *
 * @author gary.anderson@redknee.com
 */
public interface SubscriberProfileProvisionClient
{
    public List<Parameters> queryAllSubscriptionsForSubscriber(final Context context, final String subscriberID)
    throws SubscriberProfileProvisionException, HomeException;
    
    /**
     * Adds a new BM subscriber profile that corresponds to the CRM account profile.
     *
     * @param context The operating context.
     * @param subscriberAccount The subscriber for which to add a profile.
     * @throws HomeException Thrown if there are problems accessing information in the Homes.
     * @throws SubscriberProfileProvisionException Thrown if there are problems
     * accessing the SubscriberProfileProvision service.
     */
    void addSubscriberAccountProfile(Context context, Account subscriberAccount)
        throws HomeException, SubscriberProfileProvisionException;


    /**
     * Queries the specified BM subscriber profile and returns a list of profile parameters.
     *
     * @param context The operating context.
     * @param subscriberAccount The subscriber for which to query the profile parameters.
     * @return The profile parameters of the specified subscriber profile if one exists; null otherwise.
     * @throws HomeException Thrown if there are problems accessing information in the Homes.
     * @throws SubscriberProfileProvisionException Thrown if there are problems
     * accessing the SubscriberProfileProvision service.
     */
    Parameters querySubscriberAccountProfile(Context context, Account subscriberAccount)
        throws HomeException, SubscriberProfileProvisionException;


    /**
     * Deletes the BM subscriber profile.
     *
     * @param context The operating context.
     * @param subscriberAccount The subscriber for which to remove a profile.
     * @throws HomeException Thrown if there are problems accessing information in the Homes.
     * @throws SubscriberProfileProvisionException Thrown if there are problems
     * accessing the SubscriberProfileProvision service.
     */
    void deleteSubscriberAccountProfile(Context context, Account subscriberAccount)
        throws HomeException, SubscriberProfileProvisionException;

    /**
     * Deletes the BM subscriber profile and returns the final subscription/parameter instance
     * @param context The operating context.
     * @param subscriberAccount The subscriber for which to remove a profile.
     * @return The profile parameters of the specified subscription profile if one exists; null otherwise.
     * @throws HomeException Thrown if there are problems accessing information in the Homes.
     * @throws SubscriberProfileProvisionException Thrown if there are problems
     * accessing the SubscriberProfileProvision service.
     */
    public Parameters removeSubscriptionProfile(Context context, Subscriber subscription)
    throws SubscriberProfileProvisionException;

    /**
     * Adds a new subscription. Assumes that the corresponding BM subscriber profile has already been created.
     *
     * @param context The operating context.
     * @param subscription The subscription for which to create a profile.
     * @throws HomeException Thrown if there are problems accessing information in the Homes.
     * @throws SubscriberProfileProvisionException Thrown if there are problems
     * accessing the SubscriberProfileProvision service.
     */
    void addSubscriptionProfile(Context context, Subscriber subscription)
        throws HomeException, SubscriberProfileProvisionException;


    /**
     * Queries the specified subscription profile and returns a list of profile parameters.
     *
     * @param context The operating context.
     * @param subscription The subscription for which to query the profile parameters.
     * @return The profile parameters of the specified subscription profile if one exists; null otherwise.
     * @throws HomeException Thrown if there are problems accessing information in the Homes.
     * @throws SubscriberProfileProvisionException Thrown if there are problems
     * accessing the SubscriberProfileProvision service.
     */
    Parameters querySubscriptionProfile(Context context, Subscriber subscription)
        throws HomeException, SubscriberProfileProvisionException;
    
    /**
     * 
     * @param context
     * @param subscriberId
     * @return
     * @throws HomeException
     * @throws SubscriberProfileProvisionException
     */
    Parameters getSubscriptionProfile(final Context context, final String msisdn, int subscriptionType, final Parameter[] inParamSet)
    	throws HomeException, SubscriberProfileProvisionException;


    /**
     * Deletes the subscription.
     *
     * @param context The operating context.
     * @param subscription The subscription for which to delete a profile.
     * @throws HomeException Thrown if there are problems accessing information in the Homes.
     * @throws SubscriberProfileProvisionException Thrown if there are problems
     * accessing the SubscriberProfileProvision service.
     */
    void deleteSubscriptionProfile(Context context, Subscriber subscription)
        throws HomeException, SubscriberProfileProvisionException;


    /**
     * Updates the pooled group ID of the given subscriber.
     *
     * @param context The operating context.
     * @param subscription The subscription for which to update the group ID.
     * @param groupIdentifier The new group ID. May be blank to clear the group ID.
     * @param clearQuota True if updating the group ID should also clear the group quota and group usage.
     * @throws HomeException Thrown if there are problems accessing information in the Homes.
     * @throws SubscriberProfileProvisionException Thrown if there are problems
     * accessing the SubscriberProfileProvision service.
     */
    void updatePooledGroupID(Context context, Subscriber subscription, String groupIdentifier, boolean clearQuota)
        throws HomeException, SubscriberProfileProvisionException;


    /**
     * Updates the pooled group owner of the given subscription.
     *
     * @param context The operating context.
     * @param subscription The subscription for which to update the pooled group owner.
     * @param ownerIdentifier The new owner ID (e.g., MSISDN).
     * @throws HomeException Thrown if there are problems accessing information in the Homes.
     * @throws SubscriberProfileProvisionException Thrown if there are problems
     * accessing the SubscriberProfileProvision service.
     */
    void updatePooledGroupOwner(Context context, Subscriber subscription, String ownerIdentifier)
        throws HomeException, SubscriberProfileProvisionException;


    /**
     * Updates the price plan of the given subscription.
     *
     * @param context The operating context.
     * @param subscription The subscription for which to update the price plan.
     * @param pricePlan The new price plan ID.
     * @throws HomeException Thrown if there are problems accessing information in the Homes.
     * @throws SubscriberProfileProvisionException Thrown if there are problems
     * accessing the SubscriberProfileProvision service.
     */
    void updatePricePlan(Context context, Subscriber subscription, long pricePlan)
        throws HomeException, SubscriberProfileProvisionException;


    /**
     * Updates the billing language of the given subscription.
     *
     * @param context The operating context.
     * @param subscription The subscription for which to update the billing language.
     * @throws HomeException Thrown if there are problems accessing information in the Homes.
     * @throws SubscriberProfileProvisionException Thrown if there are problems
     * accessing the SubscriberProfileProvision service.
     */
    void updateBillingLanguage(Context context, Subscriber subscription, String lang)
        throws HomeException, SubscriberProfileProvisionException;


    /**
     * Updates the BAN of the given subscription.
     *
     * @param context The operating context.
     * @param subscription The subscription for which to update the BAN.
     * @param newBan The new BAN.
     * @throws HomeException Thrown if there are problems accessing information in the Homes.
     * @throws SubscriberProfileProvisionException Thrown if there are problems
     * accessing the SubscriberProfileProvision service.
     */
    void updateBAN(Context context, Subscriber subscription, String newBan)
        throws HomeException, SubscriberProfileProvisionException;


    /**
     * 
     * @param context The operating context.
     * @param subscription The subscription for which to update the expiry date.
     * @param bmState The new state of the subscription.
     * @param expiryDate The new expiry date.
     * @throws HomeException Thrown if there are problems accessing information in the Homes.
     * @throws SubscriberProfileProvisionException Thrown if there are problems
     * accessing the SubscriberProfileProvision service.
     * 
     */
    void updateStateAndExpiryDate(Context context, Subscriber subscription, int bmState, Date expiryDate)
            throws HomeException, SubscriberProfileProvisionException;
    
    /**
     * Updates the expiry date of the given subscription.
     *
     * @param context The operating context.
     * @param subscription The subscription for which to update the expiry date.
     * @param expiryDate The new expiry date.
     * @throws HomeException Thrown if there are problems accessing information in the Homes.
     * @throws SubscriberProfileProvisionException Thrown if there are problems
     * accessing the SubscriberProfileProvision service.
     */
    void updateExpiryDate(Context context, Subscriber subscription, Date expiryDate)
        throws HomeException, SubscriberProfileProvisionException;


    /**
     * Updates the balance of the given subscription.
     *
     * @param context The operating context.
     * @param subscription The subscription for which to update the balance.
     * @param balance The new balance of the subscription.
     * @throws HomeException Thrown if there are problems accessing information in the Homes.
     * @throws SubscriberProfileProvisionException Thrown if there are problems
     * accessing the SubscriberProfileProvision service.
     */
    void updateBalance(Context context, Subscriber subscription, long balance)
        throws HomeException, SubscriberProfileProvisionException;


    /**
     * Updates the state of the given subscription.
     *
     * @param context The operating context.
     * @param subscription The subscription for which to update the balance.
     * @param bmState The new state of the subscription.
     * @throws HomeException Thrown if there are problems accessing information in the Homes.
     * @throws SubscriberProfileProvisionException Thrown if there are problems
     * accessing the SubscriberProfileProvision service.
     */
    void updateState(Context context, Subscriber subscription, int bmState)
        throws HomeException, SubscriberProfileProvisionException;

    /**
     * Updates the billing day (of the month) of the subscriber.
     *
     * @param context The operating context.
     * @param subscriberAccount The subscriber for which to update the billing day.
     * @param day The day of the month, in the range of [1-28].
     * @throws HomeException Thrown if there are problems accessing information in the Homes.
     * @throws SubscriberProfileProvisionException Thrown if there are problems
     * accessing the SubscriberProfileProvision service.
     */
    void updateBillingDay(Context context, Account subscriberAccount, int day)
        throws HomeException, SubscriberProfileProvisionException;



    /**
     * Updates the birth day of the subscriber.
     *
     * @param context The operating context.
     * @param subscriberAccount The subscriber for which to update the birth day.
     * @throws HomeException Thrown if there are problems accessing information in the Homes.
     * @throws SubscriberProfileProvisionException Thrown if there are problems
     * accessing the SubscriberProfileProvision service.
     */
    void updateBirthDay(Context context, Account subscriberAccount)
        throws HomeException, SubscriberProfileProvisionException;


    /**
     * Updates the mobile number of a single subscription.
     *
     * @param context The operating context.
     * @param subscription The subscription for which to update the mobile number, which must already
     * reference the new mobile number.
     * @param oldMobileNumber The old mobile number of the subscription.
     * @throws HomeException Thrown if there are problems accessing information in the Homes.
     * @throws SubscriberProfileProvisionException Thrown if there are problems
     * accessing the SubscriberProfileProvision service.
     */
    void updateMobileNumber(Context context, Subscriber subscription, String oldMobileNumber)
        throws HomeException, SubscriberProfileProvisionException;

    
    /**
     * Updates the IMSI of a subscription.
     * 
     * @param context
     * @param subscripition
     * @param IMSI
     * @throws HomeException
     * @throws SubscriberProfileProvisionException
     */
    void updateIMSI(Context context, Subscriber subscripition, String IMSI)
        throws HomeException, SubscriberProfileProvisionException;

    /**
     * Updates the quota limit of a given subscription.
     *
     * @param context The operating context.
     * @param subscription The subscription for which to update the quota limit.
     * @param quota The new quota limit.
     * @throws HomeException Thrown if there are problems accessing information in the Homes.
     * @throws SubscriberProfileProvisionException Thrown if there are problems
     * accessing the SubscriberProfileProvision service.
     */
    void updateSubscriptionQuotaLimit(Context context, Subscriber subscription, long quota)
        throws HomeException, SubscriberProfileProvisionException;

    /**
     * Updates the monthly spend limit of a given subscription.
     *
     * @param context The operating context.
     * @param subscription The subscription for which to update the quota limit.
     * @param quota The new quota limit.
     * @throws HomeException Thrown if there are problems accessing information in the Homes.
     * @throws SubscriberProfileProvisionException Thrown if there are problems
     * accessing the SubscriberProfileProvision service.
     */
    void updateSubscriptionMonthlySpendLimit(Context context, Subscriber subscription, long spendLimit)
        throws HomeException, SubscriberProfileProvisionException;


    /**
     * Updates the PPSM supporter information for a given subscription.
     * @param context The operating context.
     * @param subscription The subscription for which to update the quota limit.
     * @param ppsmSupporter The new ppsm supporter.
     * @param ppsmScreeningTemplate the new ppsm screening template (-1 if no template).
     */
    void updatSubscriptionPPSMSupporter(final Context context, final Subscriber subscription,
            final String ppsmSupporterBan, final long ppsmScreeningTemplate) throws SubscriberProfileProvisionException;

    
    /**
     * Updates the credit limit of a given subscription.
     *
     * @param context The operating context.
     * @param subscription The subscription for which to update the credit limit.
     * @param amount The amount of the adjustment; positive values result in an increment
     * and negative values result in a decrement.
     * @param erReference A reference to be added to the resulting ERs for later reconciliation and/or tracking.
     * @throws HomeException Thrown if there are problems accessing information in the Homes.
     * @throws SubscriberProfileProvisionException Thrown if there are problems
     * accessing the SubscriberProfileProvision service.
     */
    long adjustCreditLimit(Context context, Subscriber subscription, long amount, String erReference)
        throws HomeException, SubscriberProfileProvisionException;
    

    /**
     * Updates the credit limit of a given subscription.
     *
     * @param context The operating context.
     * @param subscription The subscription for which to update the credit limit.
     * @param newCreditLimit The desired new credit limit
     * @param oldCreditLimit The existing credit limit
     * and negative values result in a decrement.
     * @param erReference A reference to be added to the resulting ERs for later reconciliation and/or tracking.
     * @return new-credit limit as returned from the balance manager
     * @throws HomeException Thrown if there are problems accessing information in the Homes.
     * @throws SubscriberProfileProvisionException Thrown if there are problems
     * accessing the SubscriberProfileProvision service.
     */
    long adjustCreditLimit(Context context, Subscriber subscription,long newCreditLimit, long oldCreditLimit, String erReference)
        throws HomeException, SubscriberProfileProvisionException;



    /**
     * Updates the group guota of a given subscription.
     *
     * @param context The operating context.
     * @param subscription The subscription for which to update the pool group quota.
     * @param amount The amount of the adjustment; positive values result in an  increment
     * and negative values result in a decrement.
     * @param erReference A reference to be added to the resulting ERs for later reconciliation and/or tracking.
     * @throws HomeException Thrown if there are problems accessing information in the Homes.
     * @throws SubscriberProfileProvisionException Thrown if there are problems
     * accessing the SubscriberProfileProvision service.
     */
    void adjustGroupQuota(Context context, Subscriber subscription, long amount, String erReference)
        throws HomeException, SubscriberProfileProvisionException;
    
    
    /**
     * Resets the subscription's monthly spend limit 
     *
     * @param context The operating context.
     * @param subscription The subscription for which to reset monthly spend limit.
     * @throws HomeException Thrown if there are problems accessing information in the Homes.
     * @throws SubscriberProfileProvisionException Thrown if there are problems
     * accessing the SubscriberProfileProvision service.
     */
    void resetMonthlySpendUsage(Context context, Subscriber subscription)
        throws SubscriberProfileProvisionException;
    
    /**
     * Resets group usage
     *
     * @param context The operating context.
     * @param subscription The subscription for which to reset group usage.
     * @throws HomeException Thrown if there are problems accessing information in the Homes.
     * @throws SubscriberProfileProvisionException Thrown if there are problems
     * accessing the SubscriberProfileProvision service.
     */
    void resetGroupUsage(Context context, Subscriber subscription)
        throws SubscriberProfileProvisionException;
    
    /**
     * Updates overdraft balance limit.
     * @param context
     * @param subscription
     * @param limit
     * @throws SubscriberProfileProvisionException
     */
    void updateOverdraftBalanceLimit(Context context, Subscriber subscription, long limit)
            throws SubscriberProfileProvisionException;

    /**
     * Update dual balance status
     *
     * @param context The operating context.
     * @param subscription The subscription for which to reset group usage.
     * @param status => if the subscription has dual balance
     * @throws HomeException Thrown if there are problems accessing information in the Homes.
     * @throws SubscriberProfileProvisionException Thrown if there are problems
     * accessing the SubscriberProfileProvision service.
     */
    void updateDualBalance(Context context, Subscriber subscription, boolean status)
        throws SubscriberProfileProvisionException;

    /**
     * Adds or updates a Bill Cycle Change Date schedule
     * 
     * @param context The operating context.
     * @param subscriptionIDs All subscription identifiers that will be affected by the bill cycle change
     * @param newBillCycleDay New bill cycle day
     * @return Array of parameters containing subscription IDs and result codes for the bill cycle change schedule for each
     * @throws SubscriberProfileProvisionCorbaException
     */
    public BCDChangeRequestReturnParam[] addUpdateBcdChangeRequest(final Context context, String[] subscriptionIDs, int newBillCycleDay)
        throws SubscriberProfileProvisionCorbaException;

    /**
     * Removes a Bill Cycle Change Date schedule
     * 
     * @param context The operating context.
     * @param subscriptionIDs All subscription identifiers that are associated with the bill cycle change
     * @return Array of parameters containing subscription IDs and result codes for the removal of bill cycle change schedule for each
     * @throws SubscriberProfileProvisionCorbaException
     */
    public BCDChangeRequestReturnParam[] removeBcdChangeRequest(final Context context, String[] subscriptionIDs)
        throws SubscriberProfileProvisionCorbaException;
    
    
    /**
     * Updates the state of the given subscription.
     *
     * @param context The operating context.
     * @param subscription The subscription for which to update the balance.
     * @param bmState The new state of the subscription.
     * @throws HomeException Thrown if there are problems accessing information in the Homes.
     * @throws SubscriberProfileProvisionException Thrown if there are problems
     * accessing the SubscriberProfileProvision service.
     */
    void updatePromOptOut(Context context, Subscriber subscription, boolean status)
        throws  SubscriberProfileProvisionException;


    public void updateBalanceThresholdAmount(Context context, Subscriber subscription, long balanceThreshold)
            throws HomeException, SubscriberProfileProvisionException;
    /**
     * update email Id of given Account.
     * @param context
     * @param subscriberAccount
     * @param email
     * @throws HomeException
     * @throws SubscriberProfileProvisionException
     */
    public void updateEmailId(final Context context, final Account subscriberAccount, final String email)
            throws HomeException, SubscriberProfileProvisionException;
    /**
     * update notification type for a particular subscription.
     * @param context
     * @param subscription
     * @param type
     * @throws HomeException
     * @throws SubscriberProfileProvisionException
     */
    public void updateNotificationType(final Context context, final Subscriber subscription, final int type)
    		throws HomeException, SubscriberProfileProvisionException;
    public void updateGroupScreeningTemplateId(Context context, Subscriber subscription, long groupScreeningTemplateId)
            throws HomeException, SubscriberProfileProvisionException;
}
